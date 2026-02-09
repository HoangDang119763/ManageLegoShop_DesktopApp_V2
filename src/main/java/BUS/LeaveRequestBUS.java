package BUS;

import DAL.LeaveRequestDAL;
import DTO.LeaveRequestDTO;
import SERVICE.AuthorizationService;
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
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 25))
            return 4;

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
        arrLocal.add(new LeaveRequestDTO(obj));
        return 1; // thêm thành công
    }

    public int update(LeaveRequestDTO obj, int employee_roleId, int employeeLoginId) {
        // 1. Kiểm tra null & phân quyền
        if (obj == null || employee_roleId <= 0 || employeeLoginId <= 0)
            return 2;

        // 2. Kiểm tra phân quyền (permission ID for update leave request)
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 26))
            return 4;

        // 3. Kiểm tra đầu vào hợp lệ
        if (!isValidLeaveRequestInput(obj))
            return 2;

        // 4. Kiểm tra dữ liệu mới có trùng dữ liệu cũ không
        if (isDuplicateLeaveRequestS(obj))
            return 1;

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

        updateLocalCache(obj);
        return 1;
    }

    public int delete(Integer id, int employee_roleId, int employeeLoginId) {
        // 1. Kiểm tra null
        if (id == null || id <= 0)
            return 2;

        // 2. Kiểm tra phân quyền (permission ID for delete leave request)
        if (employee_roleId <= 0 || employeeLoginId <= 0
                || !AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 27))
            return 4;

        // 3. Kiểm tra leave request có tồn tại không
        LeaveRequestDTO targetLeaveRequest = getByIdLocal(id);
        if (targetLeaveRequest == null)
            return 5;

        // 4. Kiểm tra đã xoá ở CSDL
        if (!LeaveRequestDAL.getInstance().delete(id))
            return 6;

        // 5. Cập nhật trạng thái trong bộ nhớ local
        for (int i = 0; i < arrLocal.size(); i++) {
            if (Objects.equals(arrLocal.get(i).getId(), id)) {
                arrLocal.remove(i);
                break;
            }
        }
        return 1;
    }

    // Cập nhật cache local
    private void updateLocalCache(LeaveRequestDTO obj) {
        for (int i = 0; i < arrLocal.size(); i++) {
            if (Objects.equals(arrLocal.get(i).getId(), obj.getId())) {
                arrLocal.set(i, new LeaveRequestDTO(obj));
                break;
            }
        }
    }

    public boolean isDuplicateLeaveRequestS(LeaveRequestDTO obj) {
        LeaveRequestDTO existingLeaveRequest = getByIdLocal(obj.getId());
        ValidationUtils validate = ValidationUtils.getInstance();

        // Kiểm tra xem type, content, start_date, end_date có trùng không
        return existingLeaveRequest != null &&
                Objects.equals(existingLeaveRequest.getType(),
                        obj.getType() != null ? validate.normalizeWhiteSpace(obj.getType()) : obj.getType())
                &&
                Objects.equals(existingLeaveRequest.getContent(),
                        obj.getContent() != null ? validate.normalizeWhiteSpace(obj.getContent()) : obj.getContent())
                &&
                Objects.equals(existingLeaveRequest.getStartDate(), obj.getStartDate()) &&
                Objects.equals(existingLeaveRequest.getEndDate(), obj.getEndDate()) &&
                Objects.equals(existingLeaveRequest.getStatusId(), obj.getStatusId()) &&
                Objects.equals(existingLeaveRequest.getEmployeeId(), obj.getEmployeeId());
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

        if (keyword == null)
            keyword = "";
        if (searchBy == null)
            searchBy = "";

        keyword = keyword.trim().toLowerCase();

        for (LeaveRequestDTO leaveRequest : arrLocal) {
            boolean matchesSearch = true;
            boolean matchesStatus = (statusFilter == -1) || (leaveRequest.isStatus() == (statusFilter == 1));
            boolean matchesEmployee = (employeeFilter == -1) || (leaveRequest.getEmployeeId() == employeeFilter);

            String id = String.valueOf(leaveRequest.getId());
            String type = leaveRequest.getType() != null ? leaveRequest.getType().toLowerCase() : "";
            String content = leaveRequest.getContent() != null ? leaveRequest.getContent().toLowerCase() : "";
            String employeeId = String.valueOf(leaveRequest.getEmployeeId());

            if (!keyword.isEmpty()) {
                switch (searchBy) {
                    case "Mã yêu cầu" -> matchesSearch = id.contains(keyword);
                    case "Loại phép" -> matchesSearch = type.contains(keyword);
                    case "Nội dung" -> matchesSearch = content.contains(keyword);
                    case "Mã nhân viên" -> matchesSearch = employeeId.contains(keyword);
                }
            }

            if (matchesSearch && matchesStatus && matchesEmployee) {
                filteredList.add(leaveRequest);
            }
        }

        return filteredList;
    }
}
