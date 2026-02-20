-- Insert new HR permissions
INSERT INTO `permission` (`name`, `permission_key`, `module_id`) VALUES
('Xem đơn nghỉ phép','EMPLOYEE_LEAVE_REQUEST_VIEW',1),
('Tạo đơn nghỉ phép','EMPLOYEE_LEAVE_REQUEST_CREATE',1),
('Quản lý duyệt đơn nghỉ','EMPLOYEE_LEAVE_REQUEST_MANAGE',1),
('Xem kỷ luật & khen thưởng','EMPLOYEE_FINE_REWARD_VIEW',1),
('Quản lý kỷ luật & khen thưởng','EMPLOYEE_FINE_REWARD_MANAGE',1),
('Xem chấm công','EMPLOYEE_ATTENDANCE_VIEW',1),
('Quản lý chấm công','EMPLOYEE_ATTENDANCE_MANAGE',1),
('Cập nhật chức vụ thanh viên','EMPLOYEE_ROLE_POSITION_UPDATE',1);

-- Grant permissions to IT Admin (Role 1) - all permissions
INSERT INTO `role_permission` (`role_id`, `permission_id`) 
SELECT 1, `id` FROM `permission` 
WHERE `permission_key` IN ('EMPLOYEE_LEAVE_REQUEST_VIEW','EMPLOYEE_LEAVE_REQUEST_CREATE','EMPLOYEE_LEAVE_REQUEST_MANAGE','EMPLOYEE_FINE_REWARD_VIEW','EMPLOYEE_FINE_REWARD_MANAGE','EMPLOYEE_ATTENDANCE_VIEW','EMPLOYEE_ATTENDANCE_MANAGE','EMPLOYEE_ROLE_POSITION_UPDATE');

-- Grant permissions to Tổng Giám Đốc (Role 2) - almost all
INSERT INTO `role_permission` (`role_id`, `permission_id`) 
SELECT 2, `id` FROM `permission` 
WHERE `permission_key` IN ('EMPLOYEE_LEAVE_REQUEST_VIEW','EMPLOYEE_LEAVE_REQUEST_CREATE','EMPLOYEE_LEAVE_REQUEST_MANAGE','EMPLOYEE_FINE_REWARD_VIEW','EMPLOYEE_FINE_REWARD_MANAGE','EMPLOYEE_ATTENDANCE_VIEW','EMPLOYEE_ATTENDANCE_MANAGE','EMPLOYEE_ROLE_POSITION_UPDATE');

-- Grant permissions to Quản lý cửa hàng (Role 3) - can view and manage HR data
INSERT INTO `role_permission` (`role_id`, `permission_id`) 
SELECT 3, `id` FROM `permission` 
WHERE `permission_key` IN ('EMPLOYEE_LEAVE_REQUEST_VIEW','EMPLOYEE_LEAVE_REQUEST_MANAGE','EMPLOYEE_FINE_REWARD_VIEW','EMPLOYEE_FINE_REWARD_MANAGE','EMPLOYEE_ATTENDANCE_VIEW','EMPLOYEE_ATTENDANCE_MANAGE','EMPLOYEE_ROLE_POSITION_UPDATE');

-- Trưởng nhóm bán hàng (Role 4) - can view
INSERT INTO `role_permission` (`role_id`, `permission_id`) 
SELECT 4, `id` FROM `permission` 
WHERE `permission_key` IN ('EMPLOYEE_LEAVE_REQUEST_VIEW','EMPLOYEE_FINE_REWARD_VIEW','EMPLOYEE_ATTENDANCE_VIEW');

-- Regular employees (Role 5-9) - can only see own data and create leave requests
INSERT INTO `role_permission` (`role_id`, `permission_id`) 
SELECT role_id, permission_id FROM (
  SELECT 5 as role_id UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
) roles
CROSS JOIN (
  SELECT `id` as permission_id FROM `permission` 
  WHERE `permission_key` IN ('EMPLOYEE_LEAVE_REQUEST_VIEW','EMPLOYEE_LEAVE_REQUEST_CREATE','EMPLOYEE_ATTENDANCE_VIEW')
) perms;
