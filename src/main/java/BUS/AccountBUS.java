package BUS;

import DAL.AccountDAL;
import DTO.AccountDTO;
import ENUM.ServiceAccessCode;
import ENUM.StatusType;
import ENUM.Status;
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

    public AccountDTO getByIdLocal(int id) {
        if (id <= 0)
            return null;
        for (AccountDTO account : arrLocal) {
            if (Objects.equals(account.getId(), id)) {
                return new AccountDTO(account);
            }
        }
        return null;
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

    public int update(AccountDTO obj, int employee_roleId, int employeeLoginId) {
        // Kiểm tra tài khoản có tồn tại và dữ liệu hợp lệ
        if (obj == null || obj.getId() <= 0 || employee_roleId <= 0)
            return 2;

        // Không có quyền 29 thì không chỉnh chính mình hay người khác
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 29))
            return 3;

        if (isInvalidAccountInput(obj))
            return 4;

        // Ngăn chặn cập nhật tài khoản gốc nếu không phải chính nó
        if (obj.getId() == 1 && employeeLoginId != 1) {
            return 5;
        }

        if (isDuplicateAccount(obj))
            return 1;
        obj.setPassword(PasswordUtils.getInstance().hashPassword(obj.getPassword()));
        if (!AccountDAL.getInstance().update(obj))
            return 6;

        updateLocalCache(obj);
        return 1;
    }

    private void updateLocalCache(AccountDTO obj) {
        for (int i = 0; i < arrLocal.size(); i++) {
            if (Objects.equals(arrLocal.get(i).getId(), obj.getId())) {
                arrLocal.set(i, new AccountDTO(obj));
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

    private boolean isDuplicateAccount(AccountDTO obj) {
        AccountDTO existingAcc = getByIdLocal(obj.getId());
        // Kiểm tra xem tên và mật khẩu có trùng không
        return existingAcc != null &&
                Objects.equals(existingAcc.getUsername(), obj.getUsername()) &&
                PasswordUtils.getInstance().verifyPassword(obj.getPassword(), existingAcc.getPassword());
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
}
