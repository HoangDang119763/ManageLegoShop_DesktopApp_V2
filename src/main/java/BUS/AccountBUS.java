package BUS;

import DAL.AccountDAL;
import DTO.AccountDTO;
import DTO.BUSResult;
import ENUM.*;
import SERVICE.AuthorizationService;
import UTILS.AppMessages;
import UTILS.AvailableUtils;
import UTILS.PasswordUtils;
import UTILS.ValidationUtils;
import de.jensd.fx.glyphs.testapps.App;

import java.util.ArrayList;
import java.util.Objects;

public class AccountBUS extends BaseBUS<AccountDTO, Integer> {
    private static final AccountBUS INSTANCE = new AccountBUS();

    private AccountBUS() {
    }

    public static AccountBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<AccountDTO> getAll() {
        return AccountDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(AccountDTO obj) {
        return obj.getId();
    }

    public BUSResult delete(Integer id, int employee_roleId, int employeeLoginId) {
        if (id == null || id <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);

        // Ngăn chặn xóa tài khoản gốc (id = 1) để bảo vệ hệ thống
        if (id == 1) {
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.ACCOUNT_CANNOT_DELETE_SYSTEM);
        }

        // Ngăn chặn tự xóa tài khoản của chính mình
        if (employeeLoginId == id) {
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.ACCOUNT_CANNOT_DELETE_SELF);
        }

        // Nếu người thực hiện không có quyền 28, từ chối
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 28))
            return new BUSResult(BUSOperationResult.UNAUTHORIZED, AppMessages.UNAUTHORIZED);

        if (!AccountDAL.getInstance().delete(id)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }
        arrLocal.removeIf(account -> Objects.equals(account.getId(), id));
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.ACCOUNT_DELETE_SUCCESS);
    }

    public BUSResult insert(AccountDTO obj, int employee_roleId, int employeeLoginId) {
        if (obj == null || isInvalidAccountInput(obj)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 27))
            return new BUSResult(BUSOperationResult.UNAUTHORIZED, AppMessages.UNAUTHORIZED);

        // Không phân biệt hoa thường
        if (isDuplicateUsername(obj.getUsername()))
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.ACCOUNT_USERNAME_DUPLICATE);

        obj.setUsername(obj.getUsername().toLowerCase());
        obj.setPassword(PasswordUtils.getInstance().hashPassword(obj.getPassword()));

        if (!AccountDAL.getInstance().insert(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        arrLocal.add(new AccountDTO(obj));
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.ACCOUNT_ADD_SUCCESS);
    }

    public BUSResult changePasswordBySelf(AccountDTO obj, String oldPassword) {
        // Kiểm tra tham số đầu vào
        if (obj == null || obj.getId() <= 0 || oldPassword == null) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        if (obj.getUsername() == null || obj.getPassword() == null) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        ValidationUtils validator = ValidationUtils.getInstance();
        if (!validator.validateUsername(obj.getUsername(), 4, 50) ||
                !validator.validatePassword(obj.getPassword(), 6, 255)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        AccountDTO existingAcc = getByIdLocal(obj.getId());

        // Kiểm tra xem tài khoản có tồn tại và mật khẩu cũ có đúng không
        if (existingAcc == null ||
                !PasswordUtils.getInstance().verifyPassword(oldPassword, existingAcc.getPassword())) {
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.ACCOUNT_OLD_PASSWORD_WRONG);
        }

        // Kiểm tra xem mật khẩu mới có giống mật khẩu cũ không
        if (PasswordUtils.getInstance().verifyPassword(obj.getPassword(), existingAcc.getPassword())) {
            return new BUSResult(BUSOperationResult.NO_CHANGES, AppMessages.ACCOUNT_PASSWORD_CHANGE_SUCCESS);
        }

        // Cập nhật mật khẩu
        obj.setPassword(PasswordUtils.getInstance().hashPassword(obj.getPassword()));
        if (!AccountDAL.getInstance().changePasswordBySelf(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        updateLocalCache(obj);
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.ACCOUNT_PASSWORD_CHANGE_SUCCESS);
    }

    private void updateLocalCache(AccountDTO obj) {
        AccountDTO newAcc = new AccountDTO(obj);

        // Update primary map
        mapLocal.put(obj.getId(), newAcc);

        for (int i = 0; i < arrLocal.size(); i++) {
            if (Objects.equals(arrLocal.get(i).getId(), obj.getId())) {
                arrLocal.set(i, newAcc);
                break;
            }
        }
    }

    private boolean isDuplicateUsername(String username) {
        if (username == null)
            return false;
        for (AccountDTO account : arrLocal) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public int checkLogin(String username, String password) {
        // 1. Kiểm tra đầu vào cơ bản
        if (username == null || password == null) {
            return -1;
        }

        for (AccountDTO account : arrLocal) {
            // 2. Tìm đúng tài khoản dựa trên Username
            if (account.getUsername().equalsIgnoreCase(username)) {

                // 3. Nếu tìm thấy Username, kiểm tra Trạng thái (Status) đầu tiên
                int lockedStatusId = StatusBUS.getInstance()
                        .getByTypeAndStatusNameLocal(StatusType.ACCOUNT, Status.Account.LOCKED).getId();

                if (account.getStatusId() == lockedStatusId) {
                    return -2; // Tài khoản bị khóa
                }

                // 4. Nếu không khóa, mới kiểm tra mật khẩu
                if (PasswordUtils.getInstance().verifyPassword(password, account.getPassword())) {
                    updateLastLogin(account.getId());
                    return account.getId(); // Đăng nhập thành công
                } else {
                    return -1; // Sai mật khẩu
                }
            }
        }
        return -1;
    }

    private boolean isInvalidAccountInput(AccountDTO obj) {
        if (obj.getUsername() == null || obj.getPassword() == null)
            return true;

        ValidationUtils validator = ValidationUtils.getInstance();
        return !validator.validateUsername(obj.getUsername(), 4, 50) ||
                !validator.validatePassword(obj.getPassword(), 6, 255);
    }

    public boolean isValidForCreateAccount(int employeeId, int type) {
        if (employeeId <= 0 || (type != 0 && type != 1))
            return false;

        if (type == 0) {
            return AccountBUS.getInstance().getByIdLocal(employeeId) == null
                    && EmployeeBUS.getInstance().getByIdLocal(employeeId).getRoleId() != 1;
        } else {
            return AccountBUS.getInstance().getByIdLocal(employeeId) == null;
        }
    }

    public boolean isExistAccount(int employeeId) {
        if (employeeId <= 0)
            return false;

        // Kiểm tra xem tài khoản có tồn tại không
        return AccountBUS.getInstance().getByIdLocal(employeeId) != null;
    }


    public ArrayList<AccountDTO> filterAccounts(String searchBy, String keyword) {
        ArrayList<AccountDTO> filteredList = new ArrayList<>();

        if (keyword == null)
            keyword = "";
        if (searchBy == null)
            searchBy = "";

        keyword = keyword.trim().toLowerCase();

        for (AccountDTO acc : arrLocal) {
            boolean matchesSearch = true;

            // Kiểm tra null tránh lỗi khi gọi .toLowerCase()
            String id = String.valueOf(acc.getId());
            String username = acc.getUsername() != null ? acc.getUsername().toLowerCase() : "";

            if (!keyword.isEmpty()) {
                switch (searchBy) {
                    case "Mã tài khoản" -> matchesSearch = id.contains(keyword);
                    case "Tài khoản" -> matchesSearch = username.contains(keyword);
                }
            }

            // Chỉ thêm vào danh sách nếu thỏa tất cả điều kiện
            if (matchesSearch) {
                filteredList.add(acc);
            }
        }

        return filteredList;
    }

    // Cập nhật thời gian đăng nhập cuối cùng cho tài khoản
    public void updateLastLogin(int accountId) {
        AccountDAL.getInstance().updateLastLogin(accountId);
    }
}
