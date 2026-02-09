package BUS;

import DAL.AccountDAL;
import DTO.AccountDTO;
import ENUM.*;
import SERVICE.AuthorizationService;
import UTILS.AvailableUtils;
import UTILS.PasswordUtils;
import UTILS.ValidationUtils;
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

    public int delete(Integer id, int employee_roleId, int employeeLoginId) {
        if (id == null || id <= 0)
            return 2;

        // Ngăn chặn xóa tài khoản gốc (id = 1) để bảo vệ hệ thống
        if (id == 1) {
            System.out.println("Không thể xóa tài khoản gốc!");
            return 3;
        }

        // Ngăn chặn tự xóa tài khoản của chính mình
        if (employeeLoginId == id) {
            System.out.println("Không thể tự xóa tài khoản của chính mình!");
            return 4;
        }

        // Nếu người thực hiện không có quyền 28, từ chối
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 28))
            return 5;

        if (!AccountDAL.getInstance().delete(id)) {
            return 6;
        }
        arrLocal.removeIf(account -> Objects.equals(account.getId(), id));
        return 1;
    }

    public int insert(AccountDTO obj, int employee_roleId, int employeeLoginId) {
        if (obj == null || isInvalidAccountInput(obj)) {
            return 2;
        }

        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 27))
            return 3;

        // Không phân biệt hoa thường
        if (isDuplicateUsername(obj.getUsername()))
            return 4;

        obj.setUsername(obj.getUsername().toLowerCase());
        obj.setPassword(PasswordUtils.getInstance().hashPassword(obj.getPassword()));

        if (!AccountDAL.getInstance().insert(obj)) {
            return 5;
        }

        arrLocal.add(new AccountDTO(obj));
        return 1;
    }

    public BUSOperationResult changePasswordBySelf(AccountDTO obj, String oldPassword) {
        // Kiểm tra tham số đầu vào
        if (obj == null || obj.getId() <= 0 || oldPassword == null) {
            return BUSOperationResult.INVALID_PARAMS;
        }

        if (obj.getUsername() == null || obj.getPassword() == null) {
            return BUSOperationResult.INVALID_DATA;
        }

        ValidationUtils validator = ValidationUtils.getInstance();
        if (!validator.validateUsername(obj.getUsername(), 4, 50) ||
                !validator.validatePassword(obj.getPassword(), 6, 255)) {
            return BUSOperationResult.INVALID_DATA;
        }

        AccountDTO existingAcc = getByIdLocal(obj.getId());

        // Kiểm tra xem tài khoản có tồn tại và mật khẩu cũ có đúng không
        if (existingAcc == null ||
                !PasswordUtils.getInstance().verifyPassword(oldPassword, existingAcc.getPassword())) {
            return BUSOperationResult.FAIL;
        }

        // Kiểm tra xem mật khẩu mới có giống mật khẩu cũ không
        if (PasswordUtils.getInstance().verifyPassword(obj.getPassword(), existingAcc.getPassword())) {
            return BUSOperationResult.NO_CHANGES;
        }

        // Cập nhật mật khẩu
        obj.setPassword(PasswordUtils.getInstance().hashPassword(obj.getPassword()));
        if (!AccountDAL.getInstance().changePasswordBySelf(obj)) {
            return BUSOperationResult.DB_ERROR;
        }

        updateLocalCache(obj);
        return BUSOperationResult.SUCCESS;
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

    public int checkLogin(String username, String password, ServiceAccessCode codeAccess) {
        // 1. Kiểm tra đầu vào cơ bản
        if (codeAccess != ServiceAccessCode.LOGIN_SERVICE || username == null || password == null) {
            return -1;
        }

        for (AccountDTO account : arrLocal) {
            // 2. Tìm đúng tài khoản dựa trên Username
            if (account.getUsername().equalsIgnoreCase(username)) {

                // 3. Nếu tìm thấy Username, kiểm tra Trạng thái (Status) đầu tiên
                int lockedStatusId = AvailableUtils.getInstance()
                        .getStatusIdByTypeAndName(StatusType.ACCOUNT, Status.Account.LOCKED);

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
