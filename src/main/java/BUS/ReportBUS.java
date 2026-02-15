package BUS;

import DTO.ReportDTO;
import DAL.ReportDAL;
import UTILS.ValidationUtils;
import java.util.ArrayList;

public class ReportBUS extends BaseBUS<ReportDTO, Integer> {
    public static final ReportBUS INSTANCE = new ReportBUS();

    private ReportBUS() {
    }

    public static ReportBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<ReportDTO> getAll() {
        return ReportDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(ReportDTO obj) {
        return obj.getId();
    }

    public ReportDTO getById(Integer id) {
        return ReportDAL.getInstance().getById(id);
    }

    public ArrayList<ReportDTO> getByLevel(String level) {
        ArrayList<ReportDTO> allReports = getAll();
        ArrayList<ReportDTO> result = new ArrayList<>();
        for (ReportDTO report : allReports) {
            if (report.getLevel() != null && report.getLevel().equals(level)) {
                result.add(report);
            }
        }
        return result;
    }

    public ArrayList<ReportDTO> getByCategory(String category) {
        ArrayList<ReportDTO> allReports = getAll();
        ArrayList<ReportDTO> result = new ArrayList<>();
        for (ReportDTO report : allReports) {
            if (report.getCategory() != null && report.getCategory().equals(category)) {
                result.add(report);
            }
        }
        return result;
    }

    public ArrayList<ReportDTO> getByEmployeeId(int employeeId) {
        ArrayList<ReportDTO> allReports = getAll();
        ArrayList<ReportDTO> result = new ArrayList<>();
        for (ReportDTO report : allReports) {
            if (report.getEmployeeId() == employeeId) {
                result.add(report);
            }
        }
        return result;
    }

    public boolean insert(ReportDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidReportInput(obj)) {
            return false;
        }

        return false;
    }

    public boolean update(ReportDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidReportInput(obj)) {
            return false;
        }

        return false;
    }

    public boolean delete(Integer id, int employeeRoleId, int employeeLoginId) {
        if (id == null || id <= 0) {
            return false;
        }

        return false;
    }

    private boolean isValidReportInput(ReportDTO obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getTitle() == null || obj.getTitle().trim().isEmpty()) {
            return false;
        }
        ValidationUtils validator = ValidationUtils.getInstance();
        if (!validator.validateVietnameseText255(obj.getTitle())) {
            return false;
        }
        if (obj.getLevel() == null || obj.getLevel().trim().isEmpty()) {
            return false;
        }
        if (obj.getCategory() == null || obj.getCategory().trim().isEmpty()) {
            return false;
        }
        if (obj.getEmployeeId() <= 0) {
            return false;
        }
        return true;
    }
}
