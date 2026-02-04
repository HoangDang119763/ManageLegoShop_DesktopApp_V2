package BUS;

import DTO.FileDTO;
import DAL.FileDAL;
import UTILS.ValidationUtils;
import SERVICE.AuthorizationService;
import java.util.ArrayList;

public class FileBUS extends BaseBUS<FileDTO, Integer> {
    public static final FileBUS INSTANCE = new FileBUS();

    private FileBUS() {
    }

    public static FileBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<FileDTO> getAll() {
        return FileDAL.getInstance().getAll();
    }

    public FileDTO getById(Integer id) {
        return FileDAL.getInstance().getById(id);
    }

    public ArrayList<FileDTO> getByFileName(String fileName) {
        ArrayList<FileDTO> allFiles = getAll();
        ArrayList<FileDTO> result = new ArrayList<>();
        for (FileDTO file : allFiles) {
            if (file.getFileName() != null && file.getFileName().contains(fileName)) {
                result.add(file);
            }
        }
        return result;
    }

    public boolean insert(FileDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidFileInput(obj)) {
            return false;
        }
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employeeRoleId, 1)) {
            return false;
        }
        if (FileDAL.getInstance().insert(obj)) {
            if (arrLocal.isEmpty()) {
                loadLocal();
            } else {
                arrLocal.add(new FileDTO(obj));
            }
            return true;
        }
        return false;
    }

    public boolean update(FileDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidFileInput(obj)) {
            return false;
        }
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employeeRoleId, 1)) {
            return false;
        }
        if (FileDAL.getInstance().update(obj)) {
            for (FileDTO file : arrLocal) {
                if (file.getId() == obj.getId()) {
                    file.setFilePath(obj.getFilePath());
                    file.setFileName(obj.getFileName());
                    file.setCreatedAt(obj.getCreatedAt());
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(Integer id, int employeeRoleId, int employeeLoginId) {
        if (id == null || id <= 0) {
            return false;
        }
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employeeRoleId, 1)) {
            return false;
        }
        if (FileDAL.getInstance().delete(id)) {
            arrLocal.removeIf(file -> file.getId() == id);
            return true;
        }
        return false;
    }

    private boolean isValidFileInput(FileDTO obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getFilePath() == null || obj.getFilePath().trim().isEmpty()) {
            return false;
        }
        if (obj.getFileName() == null || obj.getFileName().trim().isEmpty()) {
            return false;
        }
        ValidationUtils validator = ValidationUtils.getInstance();
        if (!validator.validateStringLength(obj.getFileName(), 100)) {
            return false;
        }
        return true;
    }
}
