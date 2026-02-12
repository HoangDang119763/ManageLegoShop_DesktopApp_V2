package BUS;

import DAL.CustomerDAL;
import DTO.CustomerDTO;
import SERVICE.AuthorizationService;
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

    public int delete(Integer id, int employee_roleId, int employeeLoginId) {
        // Kiểm tra ID hợp lệ
        if (id == null || id <= 0)
            return 2; // Khách hàng không tồn tại

        // Kiểm tra quyền xóa khách hàng (permission ID = 5)
        if (employee_roleId <= 0
                || !AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 5)) {
            return 4; // Không có quyền xóa
        }

        // Kiểm tra ID khách hàng vãng lai(gốc)
        if (id == 1)
            return 3;

        // Khach hang da bi xoa hoac khong ton tai || !targetCustomer.isStatus()
        CustomerDTO targetCustomer = getByIdLocal(id);
        if (targetCustomer == null)
            return 5;

        // Xóa khách hàng trong database
        if (!CustomerDAL.getInstance().delete(id)) {
            return 6;
        }

        // Cập nhật trạng thái trong bộ nhớ local (soft delete)
        for (CustomerDTO customer : arrLocal) {
            if (Objects.equals(customer.getId(), id)) {
                // customer.setStatus(false);
                // Cập nhật mapLocal (primary cache)
                mapLocal.put(id, customer);
                // mapByPhone không cần cập nhật vì vẫn giữ số điện thoại
                break;
            }
        }
        return 1;
    }

    public int insert(CustomerDTO obj, int employee_roleId, int employeeLoginId) {
        if (obj == null || employee_roleId <= 0
                || !AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 4)
                || !isValidCustomerInput(obj)) {
            return 2;
        }

        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 4))
            return 4;

        if (isDuplicateCustomer(-1, obj.getFirstName(), obj.getLastName(), obj.getPhone(), obj.getAddress())) {
            return 3;
        }

        // image_url và date_of_birth có thể null

        // validate khi chuyen xuong database
        ValidationUtils validate = ValidationUtils.getInstance();
        // obj.setStatus(true);
        obj.setFirstName(validate.normalizeWhiteSpace(obj.getFirstName()));
        obj.setLastName(validate.normalizeWhiteSpace(obj.getLastName()));
        obj.setAddress(validate.normalizeWhiteSpace(obj.getAddress()));
        obj.setPhone(validate.normalizeWhiteSpace(obj.getPhone()));

        if (!CustomerDAL.getInstance().insert(obj)) {
            return 5;
        }

        // Cập nhật cache: thêm vào cả mapLocal (primary) và mapByPhone (secondary)
        CustomerDTO newCus = new CustomerDTO(obj);
        arrLocal.add(newCus);
        mapLocal.put(newCus.getId(), newCus);
        mapByPhone.put(newCus.getPhone(), newCus);

        return 1;// them thanh cong
    }

    public int update(CustomerDTO obj, int employee_roleId, int employeeLoginId) {
        if (obj == null || obj.getId() <= 0 || employee_roleId <= 0) {
            return 2;
        }

        if (!isValidCustomerInput(obj))
            return 6;

        // Không có quyền sửa
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 6))
            return 4;

        // Kiểm tra trùng lặp trước khi cập nhật
        if (isDuplicateCustomer(obj.getId(), obj.getFirstName(), obj.getLastName(), obj.getPhone(), obj.getAddress())) {
            return 3;
        }

        // Kiểm tra input ở database
        if (isDuplicateCustomerS(obj))
            return 1;
        ValidationUtils validate = ValidationUtils.getInstance();
        // obj.setStatus(true);
        obj.setFirstName(validate.normalizeWhiteSpace(obj.getFirstName()));
        obj.setLastName(validate.normalizeWhiteSpace(obj.getLastName()));
        obj.setAddress(validate.normalizeWhiteSpace(obj.getAddress()));
        obj.setPhone(validate.normalizeWhiteSpace(obj.getPhone()));

        // Thực hiện update trong database
        if (!CustomerDAL.getInstance().update(obj)) {
            return 5;
        }

        // Sửa thành công
        updateLocalCache(obj);
        return 1;
    }

    // Cap nhat cache local (primary: mapLocal + arrLocal, secondary: mapByPhone)
    private void updateLocalCache(CustomerDTO obj) {
        if (obj == null || obj.getId() <= 0)
            return;

        CustomerDTO clonedObj = new CustomerDTO(obj);

        // Cập nhật arrLocal (ArrayList để hiển thị TableView)
        for (int i = 0; i < arrLocal.size(); i++) {
            if (Objects.equals(arrLocal.get(i).getId(), obj.getId())) {
                // Lấy object cũ để xóa từ mapByPhone
                CustomerDTO oldObj = arrLocal.get(i);
                if (oldObj.getPhone() != null) {
                    mapByPhone.remove(oldObj.getPhone());
                }
                // Cập nhật dữ liệu
                arrLocal.set(i, clonedObj);
                break;
            }
        }

        // Cập nhật mapLocal (HashMap primary - O(1))
        mapLocal.put(clonedObj.getId(), clonedObj);

        // Cập nhật mapByPhone (HashMap secondary - O(1))
        if (clonedObj.getPhone() != null && !clonedObj.getPhone().isEmpty()) {
            mapByPhone.put(clonedObj.getPhone(), clonedObj);
        }
    }

    public boolean isDuplicateCustomer(int id, String firstName, String lastName, String phone, String address) {
        if (firstName == null || lastName == null || phone == null || address == null)
            return false;

        for (CustomerDTO customer : arrLocal) {
            if (customer.getId() != id &&
                    customer.getFirstName().trim().equalsIgnoreCase(firstName.trim()) &&
                    customer.getLastName().trim().equalsIgnoreCase(lastName.trim()) &&

                    customer.getPhone().trim().equals(phone.trim()) &&
                    customer.getAddress().trim().equalsIgnoreCase(address.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidCustomerInput(CustomerDTO obj) {
        if (obj.getFirstName() == null || obj.getLastName() == null || obj.getPhone() == null
                || obj.getAddress() == null) {
            return false;
        }

        ValidationUtils validator = ValidationUtils.getInstance();

        return validator.validateVietnameseText100(obj.getFirstName()) &&
                validator.validateVietnameseText100(obj.getLastName()) &&
                validator.validateVietnamesePhoneNumber(obj.getPhone()) &&
                validator.validateVietnameseText255(obj.getAddress());
    }

    public boolean isDuplicateCustomerS(CustomerDTO obj) {
        CustomerDTO existingPro = getByIdLocal(obj.getId());
        ValidationUtils validate = ValidationUtils.getInstance();

        // Kiểm tra xem tên, mô tả, và hệ số lương có trùng không
        return existingPro != null &&
                Objects.equals(existingPro.getFirstName(), validate.normalizeWhiteSpace(obj.getFirstName())) &&
                Objects.equals(existingPro.getLastName(), validate.normalizeWhiteSpace(obj.getLastName())) &&
                Objects.equals(existingPro.getDateOfBirth(), obj.getDateOfBirth()) &&
                Objects.equals(existingPro.getPhone(), obj.getPhone()) &&
                Objects.equals(existingPro.getStatusId(), obj.getStatusId()) &&
                Objects.equals(existingPro.getAddress(), validate.normalizeWhiteSpace(obj.getAddress()));
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
