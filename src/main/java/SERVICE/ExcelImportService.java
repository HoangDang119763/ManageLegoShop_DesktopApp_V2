package SERVICE;

import BUS.DepartmentBUS;
import BUS.PositionBUS;
import BUS.StatusBUS;
import DTO.BUSResult;
import DTO.DepartmentDTO;
import DTO.EmployeeDTO;
import DTO.ImportEmployeeExcelDTO;
import DTO.PositionDTO;
import ENUM.BUSOperationResult;
import ENUM.StatusType;
import UTILS.ValidationUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic service for importing data from Excel files
 * Có thể tái sử dụng cho Employee, Product, Supplier, v.v.
 * 
 * Xử lý: đọc, validate và convert dữ liệu từ Excel
 */
public class ExcelImportService {
    private static final ExcelImportService INSTANCE = new ExcelImportService();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ValidationUtils validator = ValidationUtils.getInstance();
    private final DepartmentBUS departmentBUS = DepartmentBUS.getInstance();
    private final PositionBUS positionBUS = PositionBUS.getInstance();
    private final StatusBUS statusBUS = StatusBUS.getInstance();

    private ExcelImportService() {
    }

    public static ExcelImportService getInstance() {
        return INSTANCE;
    }

    /**
     * Read data from Excel file
     * Đọc danh sách dữ liệu từ file Excel và validate (áp dụng cho Employee)
     *
     * @param filePath path to Excel file
     * @return BUSResult containing List of ImportEmployeeExcelDTO (including
     *         invalid rows with error messages)
     */
    public BUSResult readFromExcel(String filePath) {
        List<ImportEmployeeExcelDTO> resultList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                return new BUSResult(BUSOperationResult.FAIL, "File Excel rỗng hoặc không có dữ liệu");
            }

            // Skip header row (row 0)
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null)
                    continue;

                try {
                    ImportEmployeeExcelDTO employee = parseRowToDTO(row, rowIndex + 1);
                    validateRow(employee);
                    resultList.add(employee);
                } catch (Exception e) {
                    resultList.add(new ImportEmployeeExcelDTO(rowIndex + 1, "Lỗi đọc dòng: " + e.getMessage()));
                }
            }

            return new BUSResult(BUSOperationResult.SUCCESS, "Thành công", resultList);
        } catch (IOException e) {
            return new BUSResult(BUSOperationResult.FAIL, "Lỗi đọc file Excel: " + e.getMessage());
        } catch (Exception e) {
            return new BUSResult(BUSOperationResult.FAIL, "Lỗi: " + e.getMessage());
        }
    }

    /**
     * Parse Excel row to ImportEmployeeExcelDTO
     * Chuyển đổi dòng Excel sang DTO
     */
    private ImportEmployeeExcelDTO parseRowToDTO(Row row, int rowNumber) {
        ImportEmployeeExcelDTO dto = new ImportEmployeeExcelDTO();
        dto.setRowNumber(rowNumber);

        try {
            // Column order: FirstName, LastName, Phone, Email, DateOfBirth, Gender,
            // DepartmentId, PositionId, HealthInsCode, SocialInsCode,
            // UnemploymentInsCode, MealSupport, TransportSupport, AccommodationSupport,
            // NumDependents, Username

            dto.setFirstName(getCellStringValue(row, 0).trim());
            dto.setLastName(getCellStringValue(row, 1).trim());
            dto.setPhone(getCellStringValue(row, 2).trim());
            dto.setEmail(getCellStringValue(row, 3).trim());

            // Parse DateOfBirth (dd/MM/yyyy)
            String dobStr = getCellStringValue(row, 4).trim();
            if (!dobStr.isEmpty()) {
                try {
                    dto.setDateOfBirth(LocalDate.parse(dobStr, DATE_FORMATTER));
                } catch (DateTimeParseException e) {
                    dto.setErrorMessage("Dòng " + rowNumber + ": Ngày sinh không hợp lệ (dd/MM/yyyy)");
                    dto.setValid(false);
                    return dto;
                }
            }

            dto.setGender(getCellStringValue(row, 5).trim());

            // Parse IDs
            Integer deptId = getCellIntValue(row, 6);
            Integer posId = getCellIntValue(row, 7);
            dto.setDepartmentId(deptId);
            dto.setPositionId(posId);

            // Insurance codes (optional)
            dto.setHealthInsCode(getCellStringValue(row, 8).trim());
            dto.setSocialInsCode(getCellStringValue(row, 9).trim());
            dto.setUnemploymentInsCode(getCellStringValue(row, 10).trim());

            // Parse boolean support fields
            dto.setMealSupport(parseYesNo(getCellStringValue(row, 11).trim()));
            dto.setTransportSupport(parseYesNo(getCellStringValue(row, 12).trim()));
            dto.setAccommodationSupport(parseYesNo(getCellStringValue(row, 13).trim()));

            // Parse NumDependents
            Integer numDep = getCellIntValue(row, 14);
            dto.setNumDependents(numDep != null ? numDep : 0);

            // Username
            dto.setUsername(getCellStringValue(row, 15).trim());
            Integer roleId = getCellIntValue(row, 16);
            dto.setRoleId(roleId);

        } catch (Exception e) {
            dto.setErrorMessage("Dòng " + rowNumber + ": Lỗi parse dữ liệu - " + e.getMessage());
            dto.setValid(false);
        }

        return dto;
    }

    /**
     * Validate row data
     * Áp dụng các validation rule (áp dụng cho Employee)
     */
    public void validateRow(ImportEmployeeExcelDTO dto) {
        if (!dto.isValid())
            return; // Skip if already has parse error

        StringBuilder errors = new StringBuilder();

        // 1. FirstName validation
        if (dto.getFirstName() == null || dto.getFirstName().isEmpty()) {
            errors.append("Họ đệm không được để trống. ");
        } else if (!validator.validateVietnameseText100(dto.getFirstName())) {
            errors.append("Họ đệm không hợp lệ (tối đa 100 ký tự). ");
        }

        // 2. LastName validation
        if (dto.getLastName() == null || dto.getLastName().isEmpty()) {
            errors.append("Tên không được để trống. ");
        } else if (!validator.validateVietnameseText100(dto.getLastName())) {
            errors.append("Tên không hợp lệ (tối đa 100 ký tự). ");
        }

        // 3. Phone validation
        if (dto.getPhone() == null || dto.getPhone().isEmpty()) {
            errors.append("Số điện thoại không được để trống. ");
        } else if (!validator.validateVietnamesePhoneNumber(dto.getPhone())) {
            errors.append("Số điện thoại không hợp lệ (10-11 chữ số). ");
        }

        // 4. Email validation
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            errors.append("Email không được để trống. ");
        } else if (!validator.validateEmail(dto.getEmail())) {
            errors.append("Email không hợp lệ. ");
        }

        // 5. DateOfBirth validation
        if (dto.getDateOfBirth() == null) {
            errors.append("Ngày sinh không được để trống. ");
        } else if (!validator.validateDateOfBirth(dto.getDateOfBirth())) {
            errors.append("Ngày sinh không hợp lệ (phải >= 18 tuổi). ");
        }

        // 6. Gender validation
        if (dto.getGender() == null || dto.getGender().isEmpty()) {
            errors.append("Giới tính không được để trống. ");
        }

        // 7. DepartmentId validation - Phải tồn tại và active
        if (dto.getDepartmentId() == null || dto.getDepartmentId() <= 0) {
            errors.append("ID phòng ban không hợp lệ. ");
        } else {
            DepartmentDTO dept = departmentBUS.getById(dto.getDepartmentId());
            if (dept == null) {
                errors.append("Phòng ban không tồn tại. ");
            } else {
                // Check if department is active
                int activeDeptStatusId = statusBUS.getByTypeAndStatusName(StatusType.DEPARTMENT,
                        ENUM.Status.Department.ACTIVE).getId();
                if (dept.getStatusId() != activeDeptStatusId) {
                    errors.append("Phòng ban không active. ");
                }
            }
        }

        // 8. PositionId validation
        if (dto.getPositionId() == null || dto.getPositionId() <= 0) {
            errors.append("ID vị trí không hợp lệ. ");
        } else {
            PositionDTO pos = positionBUS.getById(dto.getPositionId());
            if (pos == null) {
                errors.append("Vị trí không tồn tại. ");
            }
        }

        // 9. Insurance codes validation (optional but if provided, max 15 chars)
        if (dto.getHealthInsCode() != null && dto.getHealthInsCode().length() > 15) {
            errors.append("Mã BHYT tối đa 15 ký tự. ");
        }
        if (dto.getSocialInsCode() != null && dto.getSocialInsCode().length() > 15) {
            errors.append("Mã BHXH tối đa 15 ký tự. ");
        }
        if (dto.getUnemploymentInsCode() != null && dto.getUnemploymentInsCode().length() > 15) {
            errors.append("Mã BH thất nghiệp tối đa 15 ký tự. ");
        }

        // 10. NumDependents validation
        if (dto.getNumDependents() == null || dto.getNumDependents() < 0) {
            errors.append("Số người phụ thuộc phải >= 0. ");
        }

        // 11. Username validation (4-50 chars)
        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            errors.append("Tên đăng nhập không được để trống. ");
        } else if (dto.getUsername().length() < 4 || dto.getUsername().length() > 50) {
            errors.append("Tên đăng nhập phải 4-50 ký tự. ");
        }

        if (errors.length() > 0) {
            dto.setErrorMessage("Dòng " + dto.getRowNumber() + ": " + errors.toString().trim());
            dto.setValid(false);
        }
    }

    /**
     * Convert ImportEmployeeExcelDTO to EmployeeDTO
     * Chuyển đổi DTO import sang EmployeeDTO để lưu database
     */
    public EmployeeDTO mapToDTO(ImportEmployeeExcelDTO importDTO) {
        if (!importDTO.isValid()) {
            throw new IllegalArgumentException("Cannot map invalid ImportEmployeeExcelDTO");
        }

        // Get default status for active employee
        int activeStatusId = statusBUS.getByTypeAndStatusName(StatusType.EMPLOYEE,
                ENUM.Status.Employee.ACTIVE).getId();

        return new EmployeeDTO(
                0, // id - will be auto-generated
                importDTO.getFirstName(),
                importDTO.getLastName(),
                importDTO.getPhone(),
                importDTO.getEmail(),
                importDTO.getDateOfBirth(),
                importDTO.getDepartmentId(),
                activeStatusId,
                importDTO.getGender(),
                null, // account_id
                null, // avatarUrl
                importDTO.getPositionId(),
                importDTO.getHealthInsCode(),
                importDTO.getSocialInsCode(),
                importDTO.getUnemploymentInsCode(),
                importDTO.getMealSupport() != null ? importDTO.getMealSupport() : false,
                importDTO.getTransportSupport() != null ? importDTO.getTransportSupport() : false,
                importDTO.getAccommodationSupport() != null ? importDTO.getAccommodationSupport() : false,
                importDTO.getNumDependents(),
                null, // createdAt
                null // updatedAt
        );
    }

    // ==================== HELPER METHODS ====================

    private String getCellStringValue(Row row, int colIndex) {
        if (row.getCell(colIndex) == null)
            return "";
        switch (row.getCell(colIndex).getCellType()) {
            case STRING:
                return row.getCell(colIndex).getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) row.getCell(colIndex).getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(row.getCell(colIndex).getBooleanCellValue());
            default:
                return "";
        }
    }

    private Integer getCellIntValue(Row row, int colIndex) {
        if (row.getCell(colIndex) == null)
            return null;
        try {
            switch (row.getCell(colIndex).getCellType()) {
                case NUMERIC:
                    return (int) row.getCell(colIndex).getNumericCellValue();
                case STRING:
                    return Integer.parseInt(row.getCell(colIndex).getStringCellValue());
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean parseYesNo(String value) {
        if (value == null || value.isEmpty())
            return false;
        return value.equalsIgnoreCase("Y") || value.equalsIgnoreCase("yes");
    }
}
