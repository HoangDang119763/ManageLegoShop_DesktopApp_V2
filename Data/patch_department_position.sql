-- ==============================================================
-- PATCH: Thêm quyền VIEW cho Module 13 (Phòng ban & Chức vụ)
-- Vấn đề: Module 13 chỉ có EMPLOYEE_ROLE_POSITION_UPDATE (id=64)
--         → Role không có quyền update (HR Staff, v.v.) sẽ không thấy
--           sidebar button vì hasModuleAccess(13) = false
-- Giải pháp: Thêm permission VIEW riêng, gán cho tất cả role cần xem
-- Áp dụng: chạy 1 lần trên database java_sql (sau khi đã chạy patch_hr_modules.sql)
-- ==============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ──────────────────────────────────────────────────────────────
-- 1. Thêm permission VIEW cho Module 13
-- ──────────────────────────────────────────────────────────────
INSERT IGNORE INTO `permission` (`id`, `name`, `permission_key`, `module_id`) VALUES
(66, 'Xem phòng ban & chức vụ', 'DEPARTMENT_POSITION_VIEW', 13);

-- ──────────────────────────────────────────────────────────────
-- 2. Gán quyền VIEW cho các Role
--    Role 1: Administrator  → toàn quyền
--    Role 2: Manager        → xem được
--    Role 3: Sales          → xem được (cần biết phòng ban để điều hướng)
--    Role 4: Cashier        → xem được
--    Role 5: HR Staff       → xem được (nhưng không sửa)
-- ──────────────────────────────────────────────────────────────
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`) VALUES
(1, 66),
(2, 66),
(3, 66),
(4, 66),
(5, 66);

SET FOREIGN_KEY_CHECKS = 1;

-- ──────────────────────────────────────────────────────────────
-- KIỂM TRA KẾT QUẢ
-- ──────────────────────────────────────────────────────────────
-- SELECT r.id, r.name,
--        GROUP_CONCAT(p.permission_key ORDER BY p.id) AS permissions
-- FROM role r
-- LEFT JOIN role_permission rp ON rp.role_id = r.id
-- LEFT JOIN permission p ON p.id = rp.permission_id
-- WHERE p.module_id = 13
-- GROUP BY r.id, r.name;
