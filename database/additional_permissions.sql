-- ============================================================
-- BỔ SUNG PHÂN QUYỀN CHO CÁC TAB NHÂN SỰ MỚI
-- File: additional_permissions.sql
-- Module: Quản lý nhân sự (Module 1)
-- ============================================================

-- ============================================================
-- 1. MIGRATION: Thêm cột 'type' vào bảng fine (để phân biệt khen thưởng vs kỷ luật)
-- ============================================================
ALTER TABLE `fine` 
ADD COLUMN `type` ENUM('REWARD', 'DISCIPLINE') DEFAULT 'DISCIPLINE' AFTER `fine_level`;

-- ============================================================
-- 2. MIGRATION: Thêm cột 'date' vào bảng time_sheet (để lọc theo ngày)
-- ============================================================
ALTER TABLE `time_sheet` 
ADD COLUMN `date` DATE GENERATED ALWAYS AS (DATE(check_in)) STORED AFTER `employee_id`,
ADD INDEX `idx_date_employee` (`date`, `employee_id`);

-- ============================================================
-- 3. PHÂN QUYỀN CHO CÁC ROLE (Module 1: Quản lý nhân sự)
-- ============================================================

-- === ROLE 1: Administrator (FULL QUYỀN) ===
-- Đã có từ trước, không cần thêm

-- === ROLE 2: Manager (Full quyền trừ xóa/reset) ===
-- Đã có từ trước, không cần thêm (vì Manager được phép làm tất cả trừ DELETE/RESET)

-- === ROLE 3: Sales Staff ===
-- Sales không cần quyền truy cập HR management (giữ nguyên)

-- === ROLE 4: Warehouse Staff ===
-- Warehouse không cần quyền truy cập HR management (giữ nguyên)

-- === ROLE 5: HR Staff ===
-- HR Staff được cấp quyền đơn nghỉ phép, kỷ luật/khen thưởng, và chấm công
-- Nhưng KHÔNG được phép QUẢN LÝ (MANAGE) - chỉ được TỰA/XEM thông tin
-- Ngoài ra cần thêm quyền mới: Quản lý lương

-- Thêm quyền cho Leave Request (xem + tạo, nhưng không phải duyệt)
INSERT INTO `role_permission` (role_id, permission_id) 
SELECT 5, id FROM `permission` 
WHERE permission_key IN (
    'EMPLOYEE_LEAVE_REQUEST_VIEW',      -- Xem đơn (bao gồm cả đơn của người khác nếu họ đủ quyền hoặc chỉ đơn của mình)
    'EMPLOYEE_LEAVE_REQUEST_CREATE'     -- Tạo đơn mới
)
AND NOT EXISTS (
    SELECT 1 FROM `role_permission` 
    WHERE role_id = 5 AND permission_id = `permission`.id
);

-- Thêm quyền cho Fine & Reward (xem + manage)
INSERT INTO `role_permission` (role_id, permission_id) 
SELECT 5, id FROM `permission` 
WHERE permission_key IN (
    'EMPLOYEE_FINE_REWARD_VIEW',        -- Xem kỷ luật/khen thưởng
    'EMPLOYEE_FINE_REWARD_MANAGE'       -- Quản lý (CRUD) kỷ luật/khen thưởng
)
AND NOT EXISTS (
    SELECT 1 FROM `role_permission` 
    WHERE role_id = 5 AND permission_id = `permission`.id
);

-- Thêm quyền cho Attendance (xem + manage)
INSERT INTO `role_permission` (role_id, permission_id) 
SELECT 5, id FROM `permission` 
WHERE permission_key IN (
    'EMPLOYEE_ATTENDANCE_VIEW',         -- Xem chấm công
    'EMPLOYEE_ATTENDANCE_MANAGE'        -- Quản lý (import excel) chấm công
)
AND NOT EXISTS (
    SELECT 1 FROM `role_permission` 
    WHERE role_id = 5 AND permission_id = `permission`.id
);

-- ============================================================
-- 4. THÊM QUYỀN CHO CÁC EMPLOYEE THƯỜNG (Tự tạo đơn nghỉ phép)
-- ============================================================

-- Tạo role mới cho Employee thường nếu chưa có
-- Lưu ý: Nếu đã có role employees/staff thì bỏ qua dòng này
INSERT IGNORE INTO `role` (`id`, `name`, `description`) 
VALUES (6, 'Employee', 'Nhân viên thường - Chỉ quản lý thông tin cá nhân và tạo đơn nghỉ phép');

-- Cấp quyền cho Employee thường
INSERT INTO `role_permission` (role_id, permission_id) 
SELECT 6, id FROM `permission` 
WHERE permission_key IN (
    'EMPLOYEE_PERSONAL_VIEW',           -- Xem hồ sơ cá nhân
    'EMPLOYEE_PERSONAL_UPDATE',         -- Cập nhật hồ sơ cá nhân của mình
    'EMPLOYEE_LEAVE_REQUEST_VIEW',      -- Xem đơn (chỉ đơn của mình)
    'EMPLOYEE_LEAVE_REQUEST_CREATE'     -- Tạo đơn mới
)
AND NOT EXISTS (
    SELECT 1 FROM `role_permission` 
    WHERE role_id = 6 AND permission_id = `permission`.id
);

-- ============================================================
-- 5. THÊM QUYỀN CHO MANAGER CÓ QUYỀN DUYỆT ĐƠN NGHỈ PHÉP
-- ============================================================

-- Manager cần thêm quyền MANAGE để duyệt/từ chối đơn
INSERT INTO `role_permission` (role_id, permission_id) 
SELECT 2, id FROM `permission` 
WHERE permission_key = 'EMPLOYEE_LEAVE_REQUEST_MANAGE'
AND NOT EXISTS (
    SELECT 1 FROM `role_permission` 
    WHERE role_id = 2 AND permission_id = `permission`.id
);

-- ============================================================
-- LƯỚI PHÂN QUYỀN KHI HOÀN THÀNH
-- ============================================================
/*
╔════════════════════╦═════════════════════════════════════════════════════════╗
║      Role          ║           Quyền Leave Request                          ║
╠════════════════════╬═══════════════════════════════════════════════════════╣
║  Administrator     ║ VIEW + CREATE + MANAGE (Toàn quyền)                    ║
║  Manager           ║ VIEW + CREATE + MANAGE (Độc lập duyệt đơn)             ║
║  HR Staff (Role 5) ║ VIEW + CREATE (Xem/tạo, không duyệt)                  ║
║  Employee (Role 6) ║ VIEW + CREATE (Chỉ xem/tạo đơn của mình)              ║
║  Other             ║ NONE (Không có quyền)                                  ║

╔════════════════════╦═══════════════════════════════════════════════════════╗
║      Role          ║        Quyền Fine & Reward                             ║
╠════════════════════╬═══════════════════════════════════════════════════════╣
║  Administrator     ║ VIEW + MANAGE (Toàn quyền)                             ║
║  Manager           ║ VIEW + MANAGE (Quản lý kỷ luật/khen thưởng)            ║
║  HR Staff (Role 5) ║ VIEW + MANAGE (CRUD kỷ luật/khen thưởng)               ║
║  Other             ║ VIEW (Chỉ xem của mình)                                ║

╔════════════════════╦═══════════════════════════════════════════════════════╗
║      Role          ║         Quyền Attendance (Chấm công)                   ║
╠════════════════════╬═══════════════════════════════════════════════════════╣
║  Administrator     ║ VIEW + MANAGE (Toàn quyền)                             ║
║  Manager           ║ VIEW + MANAGE (Xem/import chấm công)                   ║
║  HR Staff (Role 5) ║ VIEW + MANAGE (Xem/import chấm công)                   ║
║  Other             ║ NONE (Không có quyền)                                  ║
╚════════════════════╩═══════════════════════════════════════════════════════╝
*/

-- ============================================================
-- 6. LOGIC KIỂM TRA QUYỀN TRONG CODE (PSEUDOCODE)
-- ============================================================
/*
// Đơn Nghỉ Phép (Leave Request)
if (user.hasPermission(EMPLOYEE_LEAVE_REQUEST_MANAGE)) {
    // Quản lý được xem TẤT CẢ đơn của toàn công ty
    // Được duyệt/từ chối đơn
    display(leaveRequests = getAll());
    enableApprovalButtons();
} else if (user.hasPermission(EMPLOYEE_LEAVE_REQUEST_VIEW)) {
    // Nhân viên chỉ xem đơn của MÌNH
    display(leaveRequests = getByEmployeeId(currentUserId));
    disableApprovalButtons();
}

// Kỷ Luật & Khen Thưởng (Fine & Reward)
if (user.hasPermission(EMPLOYEE_FINE_REWARD_MANAGE)) {
    // HR/Manager được CRUD kỷ luật/khen thưởng
    enableCRUDButtons();
} else {
    // Nhân viên chỉ xem của mình
    disableCRUDButtons();
}

// Chấm Công (Attendance)
if (user.hasPermission(EMPLOYEE_ATTENDANCE_MANAGE)) {
    // HR/Manager được import + xem tất cả
    enableImportButton();
} else if (user.hasPermission(EMPLOYEE_ATTENDANCE_VIEW)) {
    // Nhân viên chỉ xem của mình
    disableImportButton();
}
*/

-- ============================================================
-- 7. THÊM QUYỀN QUẢN LÝ LƯƠNG CHO ADMIN & MANAGER
-- ============================================================

-- Kiểm tra nếu permission EMPLOYEE_PAYROLL_VIEW không tồn tại thì thêm
INSERT IGNORE INTO `permission` (`permission_key`, `description`, `module_id`)
VALUES 
    ('EMPLOYEE_PAYROLL_VIEW', 'Xem bảng lương', 1),
    ('EMPLOYEE_PAYROLL_MANAGE', 'Quản lý lương (tính lương, export)', 1);

-- Cấp quyền PAYROLL cho Manager (full)
INSERT INTO `role_permission` (role_id, permission_id) 
SELECT 2, id FROM `permission` 
WHERE permission_key IN (
    'EMPLOYEE_PAYROLL_VIEW',
    'EMPLOYEE_PAYROLL_MANAGE'
)
AND NOT EXISTS (
    SELECT 1 FROM `role_permission` 
    WHERE role_id = 2 AND permission_id = `permission`.id
);

-- Cấp quyền PAYROLL cho HR Staff (role 5) - chỉ xem
INSERT INTO `role_permission` (role_id, permission_id) 
SELECT 5, id FROM `permission` 
WHERE permission_key = 'EMPLOYEE_PAYROLL_VIEW'
AND NOT EXISTS (
    SELECT 1 FROM `role_permission` 
    WHERE role_id = 5 AND permission_id = `permission`.id
);

