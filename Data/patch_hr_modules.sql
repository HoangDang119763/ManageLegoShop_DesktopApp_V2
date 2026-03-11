-- ==============================================================
-- PATCH: Đồng bộ module HR với Java mapping
-- Vấn đề: module 11, 13, 14 không có permission nào → hasModuleAccess() = false
--         → các module trong HrMainController không hiện
-- Áp dụng: chạy 1 lần trên database java_sql
-- ==============================================================

-- Tắt FK checks tạm thời để tránh lỗi thứ tự
SET FOREIGN_KEY_CHECKS = 0;

-- ──────────────────────────────────────────────────────────────
-- 1. Cập nhật tên module cho đúng với Java mapping
--    (module table chỉ lưu tên, không ảnh hưởng logic phân quyền)
-- ──────────────────────────────────────────────────────────────
UPDATE `module` SET `name` = 'Quản lý nhân sự (HR Ops)'  WHERE `id` = 11;
UPDATE `module` SET `name` = 'Phòng ban & Chức vụ'        WHERE `id` = 13;
UPDATE `module` SET `name` = 'Thống kê nhân sự'            WHERE `id` = 14;

-- ──────────────────────────────────────────────────────────────
-- 2. Thêm permission cho Module 11 (HROperationsTab.fxml)
--    Bao gồm: nghỉ phép, kỷ luật/khen thưởng, chấm công
-- ──────────────────────────────────────────────────────────────
INSERT IGNORE INTO `permission` (`id`, `name`, `permission_key`, `module_id`) VALUES
(57, 'Xem đơn nghỉ phép nhân viên',          'EMPLOYEE_LEAVE_REQUEST_VIEW',   11),
(58, 'Tạo đơn nghỉ phép',                    'EMPLOYEE_LEAVE_REQUEST_CREATE',  11),
(59, 'Duyệt & quản lý đơn nghỉ phép',        'EMPLOYEE_LEAVE_REQUEST_MANAGE',  11),
(60, 'Xem kỷ luật & khen thưởng nhân viên',  'EMPLOYEE_FINE_REWARD_VIEW',      11),
(61, 'Quản lý kỷ luật & khen thưởng',        'EMPLOYEE_FINE_REWARD_MANAGE',    11),
(62, 'Xem chấm công nhân viên',              'EMPLOYEE_ATTENDANCE_VIEW',       11),
(63, 'Quản lý chấm công',                    'EMPLOYEE_ATTENDANCE_MANAGE',     11);

-- ──────────────────────────────────────────────────────────────
-- 3. Thêm permission cho Module 13 (DepartmentPositionUI.fxml)
--    Quản lý phòng ban & chức vụ
-- ──────────────────────────────────────────────────────────────
INSERT IGNORE INTO `permission` (`id`, `name`, `permission_key`, `module_id`) VALUES
(64, 'Cập nhật phòng ban & chức vụ nhân viên', 'EMPLOYEE_ROLE_POSITION_UPDATE', 13);

-- ──────────────────────────────────────────────────────────────
-- 4. Thêm permission cho Module 14 (HrStatisticUI.fxml)
--    Đây là permission chính gây ra lỗi mất module
-- ──────────────────────────────────────────────────────────────
INSERT IGNORE INTO `permission` (`id`, `name`, `permission_key`, `module_id`) VALUES
(65, 'Xem thống kê nhân sự', 'HR_STATISTIC_VIEW', 14);

-- ──────────────────────────────────────────────────────────────
-- 5. Gán quyền cho các Role
-- ──────────────────────────────────────────────────────────────

-- Role 1: Administrator → toàn quyền tất cả module mới
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT 1, `id` FROM `permission` WHERE `id` BETWEEN 57 AND 65;

-- Role 2: Manager → xem + duyệt đơn nghỉ, xem thống kê, quản lý phòng ban
--         Không có MANAGE chấm công, MANAGE kỷ luật (chỉ HR Staff làm)
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT 2, `id` FROM `permission`
WHERE `id` BETWEEN 57 AND 65
  AND `permission_key` NOT IN (
      'EMPLOYEE_ATTENDANCE_MANAGE',
      'EMPLOYEE_FINE_REWARD_MANAGE'
  );

-- Role 5: HR Staff → quản lý đầy đủ module 11, xem thống kê, không được sửa phòng ban
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT 5, `id` FROM `permission`
WHERE `id` BETWEEN 57 AND 65
  AND `permission_key` != 'EMPLOYEE_ROLE_POSITION_UPDATE';

-- Mở lại FK checks
SET FOREIGN_KEY_CHECKS = 1;

-- ──────────────────────────────────────────────────────────────
-- KIỂM TRA KẾT QUẢ (chạy sau khi patch để xác nhận)
-- ──────────────────────────────────────────────────────────────
-- SELECT m.id, m.name, COUNT(p.id) AS permission_count
-- FROM module m
-- LEFT JOIN permission p ON p.module_id = m.id
-- WHERE m.id IN (11, 13, 14)
-- GROUP BY m.id, m.name;

-- SELECT r.id, r.name, COUNT(rp.permission_id) AS perm_count
-- FROM role r
-- LEFT JOIN role_permission rp ON rp.role_id = r.id
-- LEFT JOIN permission p ON p.id = rp.permission_id
-- WHERE p.module_id IN (11, 13, 14)
-- GROUP BY r.id, r.name;
