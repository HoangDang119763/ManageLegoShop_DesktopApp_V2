package BUS;

import DAL.LeaveRequestDAL;
import DTO.LeaveRequestDTO;
import UTILS.ValidationUtils;

import java.util.ArrayList;
import java.util.Objects;

public class LeaveRequestBUS extends BaseBUS<LeaveRequestDTO, Integer> {
    private static final LeaveRequestBUS INSTANCE = new LeaveRequestBUS();

    private LeaveRequestBUS() {
    }

    public static LeaveRequestBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<LeaveRequestDTO> getAll() {
        return LeaveRequestDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(LeaveRequestDTO obj) {
        return obj.getId();
    }

    public int insert(LeaveRequestDTO obj, int employee_roleId, int employeeLoginId) {
        // 1. Kiểm tra null
        if (obj == null || employee_roleId <= 0 || employeeLoginId <= 0)
            return 2;

        // 2. Kiểm tra phân quyền (permission ID for insert leave request)

        // 3. Kiểm tra đầu vào hợp lệ
        if (!isValidLeaveRequestInput(obj))
            return 2;

        // 4. validate khi chuyển xuống database
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setStatus(true);
        if (obj.getType() != null) {
            obj.setType(validate.normalizeWhiteSpace(obj.getType()));
        }
        if (obj.getContent() != null) {
            obj.setContent(validate.normalizeWhiteSpace(obj.getContent()));
        }

        // 5. Kiểm tra thêm vào CSDL
        if (!LeaveRequestDAL.getInstance().insert(obj))
            return 5;

        // 6. Thêm vào danh sách tạm

        return 1; // thêm thành công
    }

    public int update(LeaveRequestDTO obj, int employee_roleId, int employeeLoginId) {
        // 1. Kiểm tra null & phân quyền
        if (obj == null || employee_roleId <= 0 || employeeLoginId <= 0)
            return 2;

        // 2. Kiểm tra phân quyền (permission ID for update leave request)

        // 3. Kiểm tra đầu vào hợp lệ
        if (!isValidLeaveRequestInput(obj))
            return 2;

        // 4. Kiểm tra dữ liệu mới có trùng dữ liệu cũ không

        // 5. Kiểm tra đầu vào hợp lệ khi truyền xuống CSDL
        ValidationUtils validate = ValidationUtils.getInstance();
        if (obj.getType() != null) {
            obj.setType(validate.normalizeWhiteSpace(obj.getType()));
        }
        if (obj.getContent() != null) {
            obj.setContent(validate.normalizeWhiteSpace(obj.getContent()));
        }

        // 6. Kiểm tra update vào CSDL
        if (!LeaveRequestDAL.getInstance().update(obj))
            return 5;

        return 1;
    }

    public int delete(Integer id) {
        // 1. Kiểm tra null
        if (id == null || id <= 0)
            return 2;

        // 3. Kiểm tra leave request có tồn tại không
        // LeaveRequestDTO targetLeaveRequest = getByIdLocal(id);
        // if (targetLeaveRequest == null)
        // return 5;

        // 4. Kiểm tra đã xoá ở CSDL
        if (!LeaveRequestDAL.getInstance().delete(id))
            return 6;

        // 5. Cập nhật trạng thái trong bộ nhớ local
        return 1;
    }

    public boolean delete(Integer id, int employeeRoleId, int employeeLoginId) {
        return delete(id) == 1;
    }

    public ArrayList<LeaveRequestDTO> getByEmployeeId(int employeeId) {
        ArrayList<LeaveRequestDTO> allLeaveRequests = getAll();
        ArrayList<LeaveRequestDTO> result = new ArrayList<>();
        for (LeaveRequestDTO leave : allLeaveRequests) {
            if (leave.getEmployeeId() == employeeId) {
                result.add(leave);
            }
        }
        return result;
    }

    private boolean isValidLeaveRequestInput(LeaveRequestDTO obj) {
        // Type and content can be nullable
        // But start_date and end_date are required
        if (obj.getStartDate() == null || obj.getEndDate() == null)
            return false;

        // End date must be after start date
        if (obj.getEndDate().isBefore(obj.getStartDate()))
            return false;

        ValidationUtils validator = ValidationUtils.getInstance();

        if (obj.getType() != null && !obj.getType().isEmpty()) {
            if (!validator.validateVietnameseText100(obj.getType()))
                return false;
        }

        if (obj.getContent() != null && !obj.getContent().isEmpty()) {
            if (!validator.validateVietnameseText255(obj.getContent()))
                return false;
        }

        return true;
    }

    public ArrayList<LeaveRequestDTO> filterLeaveRequests(String searchBy, String keyword, int statusFilter,
            int employeeFilter) {
        ArrayList<LeaveRequestDTO> filteredList = new ArrayList<>();

        // if (keyword == null)
        // keyword = "";
        // if (searchBy == null)
        // searchBy = "";

        // keyword = keyword.trim().toLowerCase();

        // for (LeaveRequestDTO leaveRequest : arrLocal) {
        // boolean matchesSearch = true;
        // boolean matchesStatus = (statusFilter == -1) || (leaveRequest.isStatus() ==
        // (statusFilter == 1));
        // boolean matchesEmployee = (employeeFilter == -1) ||
        // (leaveRequest.getEmployeeId() == employeeFilter);

        // String id = String.valueOf(leaveRequest.getId());
        // String type = leaveRequest.getType() != null ?
        // leaveRequest.getType().toLowerCase() : "";
        // String content = leaveRequest.getContent() != null ?
        // leaveRequest.getContent().toLowerCase() : "";
        // String employeeId = String.valueOf(leaveRequest.getEmployeeId());

        // if (!keyword.isEmpty()) {
        // switch (searchBy) {
        // case "Mã yêu cầu" -> matchesSearch = id.contains(keyword);
        // case "Loại phép" -> matchesSearch = type.contains(keyword);
        // case "Nội dung" -> matchesSearch = content.contains(keyword);
        // case "Mã nhân viên" -> matchesSearch = employeeId.contains(keyword);
        // }
        // }

        // if (matchesSearch && matchesStatus && matchesEmployee) {
        // filteredList.add(leaveRequest);
        // }
        // }

        return filteredList;
    }

    @Override
    public LeaveRequestDTO getById(Integer id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }
}
