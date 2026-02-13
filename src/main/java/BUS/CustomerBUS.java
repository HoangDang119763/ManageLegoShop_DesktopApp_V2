package BUS;

import DAL.CustomerDAL;
import DTO.BUSResult;
import DTO.CustomerDTO;
import ENUM.BUSOperationResult;
import ENUM.Status;
import ENUM.StatusType;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class CustomerBUS extends BaseBUS<CustomerDTO, Integer> {
    private static final CustomerBUS INSTANCE = new CustomerBUS();
    // Secondary cache: ánh xạ số điện thoại -> CustomerDTO (tối ưu search by phone)
    private final HashMap<String, CustomerDTO> mapByPhone = new HashMap<>();

    private CustomerBUS() {
    }

    public static CustomerBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<CustomerDTO> getAll() {
        return CustomerDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(CustomerDTO obj) {
        return obj.getId();
    }

    /**
     * Override loadLocal() để cập nhật secondary cache
     */
    @Override
    public void loadLocal() {
        super.loadLocal();
        // Xây dựng lại secondary cache
        mapByPhone.clear();
        for (CustomerDTO cus : arrLocal) {
            if (cus.getPhone() != null && !cus.getPhone().isEmpty()) {
                mapByPhone.put(cus.getPhone(), cus);
            }
        }
    }

    /**
     * Tìm khách hàng theo số điện thoại
     * 
     * @param phone Số điện thoại cần tìm
     * @return CustomerDTO nếu tìm thấy, null nếu không
     */
    public CustomerDTO getByPhoneLocal(String phone) {
        if (phone == null || phone.isEmpty())
            return null;
        CustomerDTO cus = mapByPhone.get(phone);
        return cus != null ? new CustomerDTO(cus) : null;
    }

    public BUSResult delete(int id) {
        if (id <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // 1. Bảo vệ khách hàng hệ thống
        if (id == 1) {
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.CUSTOMER_CANNOT_DELETE_SYSTEM);
        }

        CustomerDTO targetCustomer = getByIdLocal(id);
        if (targetCustomer == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);

        // 2. Kiểm tra ràng buộc: Có hóa đơn nào liên kết không? (Bất kể trạng thái)
        // Sửa tên hàm thành isCustomerInAnyInvoice để an toàn khóa ngoại
        boolean hasInvoice = InvoiceBUS.getInstance().isCustomerInAnyInvoice(id);

        boolean success = hasInvoice
                ? CustomerDAL.getInstance().softDelete(targetCustomer) // Chuyển status sang INACTIVE
                : CustomerDAL.getInstance().delete(targetCustomer.getId()); // Xóa hẳn

        if (!success)
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        // 3. Đồng bộ Cache
        if (hasInvoice) {
            int inactiveStatusId = StatusBUS.getInstance()
                    .getByTypeAndStatusNameLocal(StatusType.CUSTOMER, Status.Customer.INACTIVE).getId();

            targetCustomer.setStatusId(inactiveStatusId);
            // mapLocal và mapByPhone thường trỏ cùng 1 object,
            // nhưng ghi đè lại cho chắc chắn và an toàn tham chiếu
            mapLocal.put(id, targetCustomer);
            mapByPhone.put(targetCustomer.getPhone(), targetCustomer);
        } else {
            // Xóa cứng: Xóa sạch dấu vết
            arrLocal.remove(targetCustomer);
            mapLocal.remove(id);
            mapByPhone.remove(targetCustomer.getPhone());
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.CUSTOMER_DELETE_SUCCESS);
    }

    public BUSResult insert(CustomerDTO obj) {
        if (obj == null) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setAddress(validate.convertEmptyStringToNull(obj.getAddress()));
        if (!isValidCustomerInput(obj)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // Đảm bảo trạng thái có id hợp lệ
        StatusBUS statusBus = StatusBUS.getInstance();
        if (!statusBus.isValidStatusIdForType(StatusType.CUSTOMER, obj.getStatusId()))
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.STATUS_IDForType_INVALID);

        if (isExistCustomer(obj)) {
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.CUSTOMER_ADD_DUPLICATE);

        }

        // validate khi chuyen xuong database

        // obj.setStatus(true);
        obj.setFirstName(validate.normalizeWhiteSpace(obj.getFirstName()));
        obj.setLastName(validate.normalizeWhiteSpace(obj.getLastName()));
        obj.setAddress(validate.normalizeWhiteSpace(obj.getAddress()));
        obj.setPhone(validate.normalizeWhiteSpace(obj.getPhone()));

        if (!CustomerDAL.getInstance().insert(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        // Cập nhật cache: thêm vào cả mapLocal (primary) và mapByPhone (secondary)
        CustomerDTO newCus = new CustomerDTO(obj);
        arrLocal.add(newCus);
        mapLocal.put(newCus.getId(), newCus);
        mapByPhone.put(newCus.getPhone(), newCus);

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.CUSTOMER_ADD_SUCCESS);
    }

    private void addToLocalCache(CustomerDTO obj) {
        // Deep copy để an toàn tham chiếu
        CustomerDTO newCus = new CustomerDTO(obj);
        // Thêm mới hoàn toàn vào Map và List
        mapLocal.put(newCus.getId(), newCus);
        arrLocal.add(newCus);
        mapByPhone.put(newCus.getPhone(), newCus);
    }

    // Khớp cả 5 trường dữ liệu (không tính status) thì là trùng

    public boolean isExistCustomer(CustomerDTO obj) {
        if (obj.getFirstName() == null || obj.getLastName() == null || obj.getPhone() == null)
            return false;

        for (CustomerDTO customer : arrLocal) {
            // 1. Bỏ qua nếu trùng ID (đang sửa chính mình)
            if (customer.getId() == obj.getId())
                continue;

            // 2. So sánh các trường bắt buộc (đã đảm bảo không null)
            boolean isMatchBasic = customer.getFirstName().trim().equalsIgnoreCase(obj.getFirstName().trim()) &&
                    customer.getLastName().trim().equalsIgnoreCase(obj.getLastName().trim()) &&
                    customer.getPhone().trim().equals(obj.getPhone().trim());

            if (!isMatchBasic)
                continue;

            // 3. So sánh Ngày sinh (Dùng Objects.equals để an toàn với null)
            boolean isMatchDOB = Objects.equals(customer.getDateOfBirth(), obj.getDateOfBirth());

            // 4. So sánh Địa chỉ (Cần xử lý trim() cẩn thận vì có thể null)
            String addr1 = (customer.getAddress() == null) ? "" : customer.getAddress().trim();
            String addr2 = (obj.getAddress() == null) ? "" : obj.getAddress().trim();
            boolean isMatchAddress = addr1.equalsIgnoreCase(addr2);

            // Nếu khớp tất cả thì trả về true
            if (isMatchDOB && isMatchAddress) {
                return true;
            }
        }
        return false;
    }

    // Kiểm tra đầu vào Từ UI gửi xuống BUS (insert/update)
    private boolean isValidCustomerInput(CustomerDTO obj) {
        // 1. Kiểm tra các trường bắt buộc (Không null và không rỗng)
        if (obj.getFirstName() == null || obj.getFirstName().trim().isEmpty() ||
                obj.getLastName() == null || obj.getLastName().trim().isEmpty() ||
                obj.getPhone() == null || obj.getPhone().trim().isEmpty()) {
            return false;
        }

        ValidationUtils validator = ValidationUtils.getInstance();

        // 3. Kiểm tra tính hợp lệ của Địa chỉ (Nếu có)
        if (obj.getAddress() != null && !validator.validateVietnameseText255(obj.getAddress())) {
            return false;
        }

        // 4. Kiểm tra tính hợp lệ của Ngày sinh (Nếu có)
        // Giữ nguyên giá trị cũ của ngày sinh, chỉ kiểm tra logic
        if (obj.getDateOfBirth() != null && !validator.validateDateOfBirth(obj.getDateOfBirth())) {
            return false;
        }

        // 5. Kiểm tra các trường chính bằng Validator
        return validator.validateVietnameseText100(obj.getFirstName().trim()) &&
                validator.validateVietnameseText100(obj.getLastName().trim()) &&
                validator.validateVietnamesePhoneNumber(obj.getPhone().trim());
    }

    public BUSResult update(CustomerDTO obj) {
        if (obj == null)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // 5. Kiểm tra Status
        StatusBUS statusBus = StatusBUS.getInstance();
        if (!statusBus.isValidStatusIdForType(StatusType.CUSTOMER, obj.getStatusId()))
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.STATUS_IDForType_INVALID);

        // 1. Chuẩn hóa dữ liệu NGAY LẬP TỨC để các bước check sau chính xác 100%
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setAddress(validate.convertEmptyStringToNull(obj.getAddress()));

        // 2. Validate định dạng
        if (!isValidCustomerInput(obj))
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);

        // 3. Kiểm tra không thay đổi dữ liệu (Tránh ghi DB thừa)
        if (isDataUnchanged(obj))
            return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.CUSTOMER_UPDATE_SUCCESS);

        // 4. Kiểm tra xem dữ liệu mới có bị trùng với "khách hàng khác" không (5
        // trường)
        if (isExistCustomer(obj))
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.CUSTOMER_UPDATE_DUPLICATE);

        obj.setFirstName(validate.normalizeWhiteSpace(obj.getFirstName()));
        obj.setLastName(validate.normalizeWhiteSpace(obj.getLastName()));
        obj.setAddress(validate.normalizeWhiteSpace(obj.getAddress()));
        obj.setPhone(validate.normalizeWhiteSpace(obj.getPhone()));
        // 6. Ghi vào Database
        if (!CustomerDAL.getInstance().update(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        // 7. Cập nhật Cache an toàn
        updateLocalCache(obj);
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.CUSTOMER_UPDATE_SUCCESS);
    }

    private void updateLocalCache(CustomerDTO obj) {
        CustomerDTO oldCus = mapLocal.get(obj.getId());
        CustomerDTO newCus = new CustomerDTO(obj);

        // XỬ LÝ LỖI SECONDARY INDEX: Nếu đổi SĐT thì phải xóa Key cũ
        if (oldCus != null && !Objects.equals(oldCus.getPhone(), newCus.getPhone())) {
            mapByPhone.remove(oldCus.getPhone());
        }

        // Update các Map
        mapLocal.put(obj.getId(), newCus);
        if (newCus.getPhone() != null) {
            mapByPhone.put(newCus.getPhone(), newCus);
        }

        // Update List arrLocal (Dùng vòng lặp là chấp nhận được nếu cần giữ thứ tự)
        for (int i = 0; i < arrLocal.size(); i++) {
            if (arrLocal.get(i).getId() == obj.getId()) {
                arrLocal.set(i, newCus);
                break;
            }
        }
    }

    // Kiểm tra dữ liệu trùng lặp khi cập nhật
    public boolean isDataUnchanged(CustomerDTO obj) {
        CustomerDTO existingCus = getByIdLocal(obj.getId());
        ValidationUtils validate = ValidationUtils.getInstance();

        // Kiểm tra xem tên, mô tả, và hệ số lương có trùng không
        return existingCus != null &&
                Objects.equals(existingCus.getFirstName(), validate.normalizeWhiteSpace(obj.getFirstName())) &&
                Objects.equals(existingCus.getLastName(), validate.normalizeWhiteSpace(obj.getLastName())) &&
                Objects.equals(existingCus.getDateOfBirth(), obj.getDateOfBirth()) &&
                Objects.equals(existingCus.getPhone(), obj.getPhone()) &&
                Objects.equals(existingCus.getStatusId(), obj.getStatusId()) &&
                Objects.equals(existingCus.getAddress(), validate.normalizeWhiteSpace(obj.getAddress()));
    }

    // searchbar
    public ArrayList<CustomerDTO> filterCustomers(String searchBy, String keyword, int statusFilter) {
        ArrayList<CustomerDTO> filteredList = new ArrayList<>();

        if (keyword == null)
            keyword = "";
        if (searchBy == null)
            searchBy = "";

        keyword = keyword.trim().toLowerCase();

        for (CustomerDTO cus : arrLocal) {
            boolean matchesSearch = true;
            boolean matchesStatus = (statusFilter == -1) || (cus.getStatusId() == statusFilter); // Sửa lỗi ở đây

            // Kiểm tra null tránh lỗi khi gọi .toLowerCase()
            String firstName = cus.getFirstName() != null ? cus.getFirstName().toLowerCase() : "";
            String lastName = cus.getLastName() != null ? cus.getLastName().toLowerCase() : "";
            String id = String.valueOf(cus.getId());
            String phone = cus.getPhone() != null ? cus.getPhone() : "";

            if (!keyword.isEmpty()) {
                switch (searchBy) {
                    case "Mã khách hàng" -> matchesSearch = id.contains(keyword);
                    case "Họ đệm" -> matchesSearch = firstName.contains(keyword);
                    case "Tên" -> matchesSearch = lastName.contains(keyword);
                    case "Số điện thoại" -> matchesSearch = phone.contains(keyword);
                }
            }

            // Chỉ thêm vào danh sách nếu thỏa tất cả điều kiện
            if (matchesSearch && matchesStatus) {
                filteredList.add(cus);
            }
        }

        return filteredList;
    }

    public String nextId() {
        return String.valueOf(CustomerBUS.getInstance().getAllLocal().size() + 1);
    }

    public ArrayList<CustomerDTO> searchCustomerByPhone(String phone) {
        ArrayList<CustomerDTO> list = new ArrayList<>();
        if (phone == null || phone.trim().isEmpty())
            return arrLocal;

        // Tối ưu: sử dụng secondary cache mapByPhone
        String trimmedPhone = phone.trim();
        for (CustomerDTO cus : arrLocal) {
            if (cus.getPhone() != null && cus.getPhone().contains(trimmedPhone))
                list.add(cus);
        }
        return list;
    }
}
