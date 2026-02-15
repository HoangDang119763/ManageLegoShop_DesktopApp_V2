package BUS;

import DAL.CustomerDAL;
import DAL.SupplierDAL;
import DTO.CategoryDTO;
import DTO.CustomerDTO;
import DTO.SupplierDTO;
import UTILS.ValidationUtils;

import java.util.ArrayList;
import java.util.Objects;

public class SupplierBUS extends BaseBUS<SupplierDTO, Integer> {
    private static final SupplierBUS INSTANCE = new SupplierBUS();

    private SupplierBUS() {
    }

    public static SupplierBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<SupplierDTO> getAll() {
        return SupplierDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(SupplierDTO obj) {
        return obj.getId();
    }

    public ArrayList<SupplierDTO> filterSuppliers(String searchBy, String keyword, int statusFilter) {
        ArrayList<SupplierDTO> filteredList = new ArrayList<>();

        // if (keyword == null)
        // keyword = "";
        // if (searchBy == null)
        // searchBy = "";

        // keyword = keyword.trim().toLowerCase();

        // for (SupplierDTO sup : arrLocal) {
        // // Check keyword match
        // boolean matchesSearch = true;
        // String name = sup.getName() != null ? sup.getName().toLowerCase() : "";
        // String id = String.valueOf(sup.getId());

        // if (!keyword.isEmpty()) {
        // matchesSearch = switch (searchBy) {
        // case "Mã nhà cung cấp" -> id.contains(keyword);
        // case "Tên nhà cung cấp" -> name.contains(keyword);
        // default -> false;
        // };
        // }

        // // Check status match (-1 = all, 1 = active, 0 = inactive)
        // boolean matchesStatus = (statusFilter == -1) || sup.getStatusId() ==
        // statusFilter;

        // // Add if matches all conditions
        // if (matchesSearch && matchesStatus) {
        // filteredList.add(sup);
        // }
        // }

        return filteredList;
    }

    public ArrayList<SupplierDTO> searchSupplierByPhone(String phone) {
        ArrayList<SupplierDTO> temp = new ArrayList<>();

        if (phone == null || phone.trim().isEmpty()) {
            return temp; // hoặc return arrLocal nếu bạn muốn hiển thị tất cả
        }

        // for (SupplierDTO s : arrLocal) {
        // if (s.getPhone() != null && s.getPhone().contains(phone.trim())) {
        // temp.add(s);
        // }
        // }
        return temp;
    }

    public int delete(Integer id, int employee_roleId, int employeeLoginId) {
        // 1.Kiểm tra null
        if (id == null || id <= 0)
            return 2;

        // 2.Kiểm tra quyền xóa Nhà cung cấp (permission ID = 11)

        // 3.Kiểm tra Nhà cung cấp da bi xoa hoac khong ton tai || !targetSup.isStatus()
        // SupplierDTO targetSup = getByIdLocal(id);
        // if (targetSup == null)
        // return 5;

        // 4.Kiểm tra đã xoá ở CSDL
        if (!SupplierDAL.getInstance().delete(id)) {
            return 6;
        }

        // Cập nhật trạng thái trong bộ nhớ local
        // for (SupplierDTO sup : arrLocal) {
        // if (Objects.equals(sup.getId(), id)) {
        // // sup.setStatus(false);
        // break;
        // }
        // }
        return 1;
    }

    private boolean isValidSupplierInput(SupplierDTO obj) {
        if (obj.getName() == null || obj.getName().trim().isEmpty() ||
                obj.getPhone() == null || obj.getPhone().trim().isEmpty() ||
                obj.getAddress() == null || obj.getAddress().trim().isEmpty())
            return false;

        ValidationUtils validator = ValidationUtils.getInstance();
        return validator.validateVietnameseText50(obj.getName()) &&
                validator.validateVietnamesePhoneNumber(obj.getPhone()) &&
                validator.validateVietnameseText65k4(obj.getAddress()) &&
                (obj.getEmail() == null || obj.getEmail().isEmpty() || validator.validateEmail(obj.getEmail()));
    }

    public int insert(SupplierDTO obj, int employee_roleId, int employeeLoginId) {
        // 1. Kiểm tra ID hợp lệ
        if (obj == null || employee_roleId <= 0 || employeeLoginId <= 0)
            return 2;

        // 3. Kiểm tra dữ liệu đầu vào trên GUI
        if (!isValidSupplierInput(obj))
            return 6;

        // 5. validate khi chuyen xuong database
        ValidationUtils validate = ValidationUtils.getInstance();
        // obj.setStatus(true);
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));
        obj.setPhone(obj.getPhone());
        obj.setAddress(validate.normalizeWhiteSpace(obj.getAddress()));
        if (obj.getEmail() != null && !obj.getEmail().isEmpty()) {
            obj.setEmail(validate.normalizeWhiteSpace(obj.getEmail()));
        }

        // 6. Kiểm tra thêm vào CSDL
        if (!SupplierDAL.getInstance().insert(obj))
            return 5;

        return 1;// them thanh cong
    }

    public int update(SupplierDTO obj, int employee_roleId, int employeeLoginId) {
        // 1. Kiểm tra null & phân quyền
        if (obj == null || employee_roleId <= 0 || employeeLoginId <= 0)
            return 2;

        if (!isValidSupplierInput(obj))
            return 6;

        // 6. Kiểm tra đầu vào hợp lệ khi truyền xuống CSDL
        ValidationUtils validate = ValidationUtils.getInstance();
        // obj.setStatus(true);
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));
        obj.setPhone(validate.normalizeWhiteSpace(obj.getPhone()));
        obj.setAddress(validate.normalizeWhiteSpace(obj.getAddress()));
        if (obj.getEmail() != null && !obj.getEmail().isEmpty()) {
            obj.setEmail(validate.normalizeWhiteSpace(obj.getEmail()));
        }

        // 6. Kiểm tra thêm vào CSDL
        if (!SupplierDAL.getInstance().update(obj)) {
            return 5;
        }
        return 1;
    }

    public boolean isExistsSupplier(SupplierDTO obj) {
        ValidationUtils validate = ValidationUtils.getInstance();

        // Chuẩn hóa dữ liệu đầu vào một lần để tối ưu hiệu năng
        String nName = validate.normalizeWhiteSpace(obj.getName());
        String nPhone = validate.normalizeWhiteSpace(obj.getPhone());
        String nAddress = validate.normalizeWhiteSpace(obj.getAddress());
        String nEmail = validate.normalizeWhiteSpace(obj.getEmail());

        // for (SupplierDTO sup : arrLocal) {
        // // Chỉ coi là trùng nếu khác ID nhưng KHỚP HẾT 4 trường quan trọng
        // if (sup.getId() != obj.getId() &&
        // sup.getName().equalsIgnoreCase(nName) &&
        // sup.getPhone().equalsIgnoreCase(nPhone) &&
        // sup.getAddress().equalsIgnoreCase(nAddress) &&
        // sup.getEmail().equalsIgnoreCase(nEmail)) {
        // return true;
        // }
        // }
        return false;
    }

    @Override
    public SupplierDTO getById(Integer id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }

}
