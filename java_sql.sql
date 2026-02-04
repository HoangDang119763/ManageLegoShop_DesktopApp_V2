-- create schema java_sql;
-- use java_sql;
-- drop database java_sql;

-- Tạo bảng Salary
CREATE TABLE `salary` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `base` DECIMAL(10,2) NOT NULL,
  `coefficient` DECIMAL(5,2) NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Chèn dữ liệu vào bảng Salary
INSERT INTO `salary` (`id`, `base`, `coefficient`) VALUES
  (1, 30200000, 2.95),   -- Tương ứng Tổng giám đốc
  (2, 25200000, 2.65),   -- Tương ứng CEO
  (3, 24200000, 2.45),   -- Tương ứng Giám đốc công nghệ
  (4, 19200000, 2.15),   -- Tương ứng Giám đốc tài chính
  (5, 18200000, 2.15),   -- Tương ứng Giám đốc kinh doanh
  (6, 14200000, 1.65),   -- Tương ứng Quản lý khu vực
  (7, 15200000, 1.75),   -- Tương ứng Trưởng phòng kinh doanh
  (8, 9200000, 1.35),    -- Tương ứng Quản lý cửa hàng
  (9, 10200000, 1.40),   -- Tương ứng Quản lý kho
  (10, 11200000, 1.45),  -- Tương ứng Trưởng nhóm bán hàng
  (11, 6700000, 1.20),   -- Tương ứng Nhân viên hỗ trợ khách hàng
  (12, 7200000, 1.25),   -- Tương ứng Nhân viên bán hàng chuyên nghiệp
  (13, 5400000, 1.10);    -- Tương ứng Nhân viên bán hàng

CREATE TABLE `status` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `type` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `status` (`id`, `name`, `description`, `type`) VALUES 
-- Nhóm Nhân Viên - Employee
(1, 'Active', 'Đang làm việc', 'EMPLOYEE'),
(2, 'Inactive', 'Đã nghỉ việc', 'EMPLOYEE'),
(3, 'On Leave', 'Đang nghỉ phép', 'EMPLOYEE'),
-- Nhóm Tài Khoản - Account
(4, 'Active', 'Được phép đăng nhập', 'ACCOUNT'),
(5, 'Locked', 'Bị khóa (do sai pass/vi phạm)', 'ACCOUNT'),
(6, 'Suspended', 'Tạm ngưng sử dụng', 'ACCOUNT'),
-- Nhóm Sản Phẩm - Product
(7, 'Active', 'Đang kinh doanh', 'PRODUCT'),
(8, 'Inactive', 'Ngừng kinh doanh', 'PRODUCT'),
-- Nhóm Thể Loại - Category
(9, 'Active', 'Hoạt động', 'CATEGORY'),
(10, 'Inactive', 'Vô hiệu', 'CATEGORY'),
-- Nhóm Nhà Cung Cấp - Supplier
(11, 'Active', 'Hoạt động', 'SUPPLIER'),
(12, 'Inactive', 'Vô hiệu', 'SUPPLIER'),
-- Nhóm Khách Hàng - Customer 
(13, 'Active', 'Khách hàng thân thiết', 'CUSTOMER'),
(14, 'Inactive', 'Khách hàng ngưng tương tác', 'CUSTOMER'),
-- Nhóm Hóa Đơn - Invoice 
(15, 'Completed', 'Giao dịch thành công', 'INVOICE'),
(16, 'Canceled', 'Giao dịch đã bị hủy bỏ', 'INVOICE'),
-- Nhóm Phiếu nhập - Import 
(17, 'Completed', 'Giao dịch thành công', 'IMPORT'),
(18, 'Canceled', 'Giao dịch đã bị hủy bỏ', 'IMPORT'),
-- Nhóm Xin nghỉ phép - Leave Request
(19, 'PENDING', 'Đơn đang chờ quản lý phê duyệt', 'LEAVE_REQUEST'),
(20, 'APPROVED', 'Đơn đã được chấp thuận', 'LEAVE_REQUEST'),
(21, 'REJECTED', 'Đơn bị từ chối', 'LEAVE_REQUEST'),
(22, 'CANCELED', 'Đơn đã bị hủy bởi nhân viên', 'LEAVE_REQUEST');

-- Tạo bảng Role
CREATE TABLE `role` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(150) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `start_experience` int DEFAULT 0,  -- Số năm kinh nghiệm tối thiểu
  `end_experience` int DEFAULT 0,    -- Số năm kinh nghiệm tối đa
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `salary_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`salary_id`) REFERENCES `salary` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Chèn dữ liệu vào bảng Role mà không có cột status_id
INSERT INTO `role` (`id`, `name`, `description`, `start_experience`, `end_experience`, `salary_id`) VALUES
-- Nhóm Bán hàng (Phân 3 bậc)
(1, 'Nhân viên bán hàng (Bậc 1)', 'Nhân viên mới, thực hiện bán hàng cơ bản', 0, 1, 13),
(2, 'Nhân viên bán hàng (Bậc 2)', 'Nhân viên đã có kinh nghiệm, tư vấn dòng LEGO chuyên sâu', 1, 3, 12),
(3, 'Nhân viên bán hàng (Bậc 3)', 'Nhân viên nòng cốt, hỗ trợ đào tạo người mới', 3, 5, 11),
-- Nhóm Kho (Phân 2 bậc)
(4, 'Nhân viên kho (Bậc 1)', 'Thực hiện sắp xếp và kiểm đếm hàng hóa', 0, 2, 9),
(5, 'Nhân viên kho (Bậc 2)', 'Quản lý nhập xuất, chịu trách nhiệm tồn kho', 2, 5, 8),
-- Nhóm Quản lý
(6, 'Trưởng nhóm bán hàng', 'Giám sát ca làm việc và hỗ trợ thanh toán phức tạp', 2, 4, 10),
(7, 'Quản lý cửa hàng', 'Điều hành toàn diện hoạt động cửa hàng', 4, 15, 8),
-- Cấp cao nhất
(8, 'Tổng giám đốc', 'Chủ cửa hàng/Điều hành cao cấp', 10, 30, 1);

CREATE TABLE `module` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `module` (`id`, `name`) VALUES
(1, 'Quản lý nhân viên'),
(2, 'Quản lý khách hàng'),
(3, 'Quản lý sản phẩm'),
(4, 'Quản lý nhà cung cấp'),
(5, 'Quản lý bán hàng'),
(6, 'Quản lý nhập hàng'),
(7, 'Quản lý thể loại'),
(8, 'Quản lý khuyến mãi'),
(9, 'Quản lý chức vụ'),
(10, 'Quản lý tài khoản'),
(11, 'Thống kê');

DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `permission_key` varchar(100) NOT NULL UNIQUE, -- Cột quan trọng để BE check
  `module_id` INT(11) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`module_id`) REFERENCES `module` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `permission` (`id`, `name`, `permission_key`, `module_id`) VALUES
-- Quản lý nhân viên
(1, 'Thêm nhân viên', 'EMPLOYEE_ADD', 1),
(2, 'Xóa nhân viên', 'EMPLOYEE_DELETE', 1),
(3, 'Sửa nhân viên', 'EMPLOYEE_EDIT', 1),
-- Quản lý khách hàng
(4, 'Thêm khách hàng', 'CUSTOMER_ADD', 2),
(5, 'Xóa khách hàng', 'CUSTOMER_DELETE', 2),
(6, 'Sửa khách hàng', 'CUSTOMER_EDIT', 2),
-- Quản lý sản phẩm
(7, 'Thêm sản phẩm', 'PRODUCT_ADD', 3),
(8, 'Xóa sản phẩm', 'PRODUCT_DELETE', 3),
(9, 'Sửa sản phẩm', 'PRODUCT_EDIT', 3),
-- Quản lý nhà cung cấp
(10, 'Thêm nhà cung cấp', 'SUPPLIER_ADD', 4),
(11, 'Xóa nhà cung cấp', 'SUPPLIER_DELETE', 4),
(12, 'Sửa nhà cung cấp', 'SUPPLIER_EDIT', 4),
-- Quản lý bán hàng
(13, 'Tạo đơn hàng', 'ORDER_CREATE', 5),
(14, 'Xem đơn hàng', 'ORDER_VIEW', 5),
-- Quản lý nhập hàng
(15, 'Tạo phiếu nhập hàng', 'IMPORT_CREATE', 6),
(16, 'Xem phiếu nhập hàng', 'IMPORT_VIEW', 6),
-- Quản lý thể loại
(17, 'Thêm thể loại', 'CATEGORY_ADD', 7),
(18, 'Xóa thể loại', 'CATEGORY_DELETE', 7),
(19, 'Sửa thể loại', 'CATEGORY_EDIT', 7),
-- Quản lý khuyến mãi
(20, 'Thêm mã giảm giá', 'PROMOTION_ADD', 8),
(21, 'Xóa mã giảm giá', 'PROMOTION_DELETE', 8),
(22, 'Sửa mã giảm giá', 'PROMOTION_EDIT', 8),
-- Quản lý chức vụ & phân quyền
(23, 'Thêm chức vụ', 'ROLE_ADD', 9),
(24, 'Xóa chức vụ', 'ROLE_DELETE', 9),
(25, 'Sửa chức vụ', 'ROLE_EDIT', 9),
(26, 'Sửa phân quyền', 'PERMISSION_EDIT', 9),
-- Quản lý tài khoản
(27, 'Tạo tài khoản', 'ACCOUNT_ADD', 10),
(28, 'Xóa tài khoản', 'ACCOUNT_DELETE', 10),
(29, 'Sửa tài khoản', 'ACCOUNT_EDIT', 10),
-- Thống kê
(30, 'Xem thống kê', 'STATISTICS_VIEW', 11);

CREATE TABLE `role_permission` (
  `role_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL,
  `status` TINYINT(1) NOT NULL DEFAULT 1 CHECK (`status` IN (0,1)),
   PRIMARY KEY (`role_id`, `permission_id`),
   FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
   FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `role_permission` (`role_id`, `permission_id`, `status`)
VALUES
	(1,1,1),(1,2,1),(1,3,1),(1,4,1),(1,5,1),(1,6,1),(1,7,1),(1,8,1),(1,9,1),(1,10,1),(1,11,1),(1,12,1),(1,13,1),(1,14,1),(1,15,1),(1,16,1),(1,17,1),(1,18,1),(1,19,1),(1,20,1),(1,21,1),(1,22,1),(1,23,1),(1,24,1),(1,25,1),(1,26,1),(1,27,1),(1,28,1),(1,29,1),(1,30,1),(2,1,0),(2,2,0),(2,3,0),(2,4,0),(2,5,0),(2,6,0),(2,7,0),(2,8,0),(2,9,0),(2,10,1),(2,11,0),(2,12,0),(2,13,0),(2,14,0),(2,15,1),(2,16,1),(2,17,0),(2,18,0),(2,19,0),(2,20,0),(2,21,0),(2,22,0),(2,23,0),(2,24,0),(2,25,0),(2,26,0),(2,27,0),(2,28,0),(2,29,0),(2,30,0),(3,1,0),(3,2,0),(3,3,0),(3,4,1),(3,5,0),(3,6,0),(3,7,0),(3,8,0),(3,9,0),(3,10,0),(3,11,0),(3,12,0),(3,13,1),(3,14,1),(3,15,0),(3,16,0),(3,17,0),(3,18,0),(3,19,0),(3,20,0),(3,21,0),(3,22,0),(3,23,0),(3,24,0),(3,25,0),(3,26,0),(3,27,0),(3,28,0),(3,29,0),(3,30,0),(4,1,0),(4,2,0),(4,3,0),(4,4,1),(4,5,1),(4,6,1),(4,7,0),(4,8,0),(4,9,0),(4,10,0),(4,11,0),(4,12,0),(4,13,0),(4,14,0),(4,15,0),(4,16,0),(4,17,0),(4,18,0),(4,19,0),(4,20,0),(4,21,0),(4,22,0),(4,23,0),(4,24,0),(4,25,0),(4,26,0),(4,27,0),(4,28,0),(4,29,0),(4,30,0),(5,1,1),(5,2,1),(5,3,1),(5,4,1),(5,5,1),(5,6,1),(5,7,0),(5,8,0),(5,9,0),(5,10,0),(5,11,0),(5,12,0),(5,13,0),(5,14,0),(5,15,0),(5,16,0),(5,17,0),(5,18,0),(5,19,0),(5,20,0),(5,21,0),(5,22,0),(5,23,0),(5,24,0),(5,25,0),(5,26,0),(5,27,1),(5,28,1),(5,29,1),(5,30,0),(6,1,1),(6,2,1),(6,3,1),(6,4,1),(6,5,1),(6,6,1),(6,7,0),(6,8,0),(6,9,0),(6,10,0),(6,11,0),(6,12,0),(6,13,0),(6,14,0),(6,15,0),(6,16,0),(6,17,0),(6,18,0),(6,19,0),(6,20,0),(6,21,0),(6,22,0),(6,23,1),(6,24,1),(6,25,1),(6,26,1),(6,27,1),(6,28,1),(6,29,1),(6,30,1);

-- 1. Tổng giám đốc (Role 8): Bật Full 1-30
UPDATE `role_permission` SET `status` = 1 WHERE `role_id` = 8;

-- 2. Quản lý cửa hàng (Role 7): Quyền quản lý vận hành + Thống kê
UPDATE `role_permission` SET `status` = 1 
WHERE `role_id` = 7 AND `permission_id` IN (1,3, 4,6, 7,9, 10,12, 13,14, 15,16, 17,19, 20,22, 30);

-- 3. Trưởng nhóm bán hàng (Role 6): Bán hàng + Chăm sóc khách + Voucher
UPDATE `role_permission` SET `status` = 1 
WHERE `role_id` = 6 AND `permission_id` IN (4,6, 7,9, 13,14, 20,22);

-- 4. Nhóm Bán hàng (Role 1, 2, 3): Chỉ tập trung Bán hàng & Khách hàng
UPDATE `role_permission` SET `status` = 1 
WHERE `role_id` IN (1, 2, 3) AND `permission_id` IN (4, 6, 13, 14);

-- 5. Nhóm Kho (Role 4, 5): Quản lý Sản phẩm, Nhà cung cấp, Nhập hàng, Thể loại
UPDATE `role_permission` SET `status` = 1 
WHERE `role_id` IN (4, 5) AND `permission_id` IN (7,9, 10,12, 15,16, 17,19);

CREATE TABLE `employee` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `first_name` VARCHAR(100) NOT NULL,
  `last_name` VARCHAR(100) NOT NULL,
  `phone` VARCHAR(15) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `date_of_birth` DATE DEFAULT NULL,
  `role_id` INT(11) DEFAULT NULL,
  `status_id` INT NOT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `account_id` int DEFAULT NULL,
  `is_health_insurance` tinyint(1) DEFAULT '0',
  `is_social_insurance` tinyint(1) DEFAULT '0',
  `is_unemployment_insurance` tinyint(1) DEFAULT '0',
  `is_personal_income_tax` tinyint(1) DEFAULT '0',
  `is_transportation_support` tinyint(1) DEFAULT '0',
  `is_accommodation_support` tinyint(1) DEFAULT '0',
  `updated_position_at` timestamp NULL DEFAULT NULL,
  `previous_position` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE SET NULL,
  FOREIGN KEY (`status_id`) REFERENCES `status` (`id`) -- Ràng buộc sang bảng status
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `employee` 
(`first_name`, `last_name`, `phone`, `email`, `date_of_birth`, `role_id`, `status_id`, `gender`, `account_id`, `is_health_insurance`, `is_social_insurance`, `is_unemployment_insurance`, `is_personal_income_tax`, `is_transportation_support`, `is_accommodation_support`) 
VALUES 
('Đặng Huy', 'Hoàng', '0123456789', 'hoang.dh@company.com', '2004-06-11', 1, 1, 'Nam', 1, 1, 1, 1, 1, 1, 1),
('Nguyễn Thành', 'Long', '0987654321', 'long.nt@company.com', '2003-04-11', 2, 1, 'Nam', 2, 1, 1, 1, 1, 0, 0),
('Tần Thiên', 'Lang', '0912345678', 'lang.tt@company.com', '2000-01-15', 3, 1, 'Nam', 3, 1, 1, 1, 0, 1, 0),
('Lê Thị', 'Bích', '0933456789', 'bich.lt@company.com', '1988-02-20', 3, 1, 'Nữ', 4, 1, 0, 1, 1, 0, 1),
('Phạm Minh', 'Chính', '0944567890', 'chinh.pm@company.com', '1985-03-25', 3, 1, 'Nam', 5, 1, 1, 0, 1, 1, 1),
('Nguyễn Thị', 'Diệu', '0955678901', 'dieu.nt@company.com', '1992-04-30', 3, 1, 'Nữ', 6, 0, 0, 0, 1, 0, 0),
('Đỗ Văn', 'Em', '0966789012', 'em.dv@company.com', '1995-05-05', 6, 1, 'Nam', 7, 1, 1, 1, 0, 0, 0),
('Bùi Thị', 'Phượng', '0977890123', 'phuong.bt@company.com', '1993-06-10', 5, 1, 'Nữ', 8, 1, 1, 1, 1, 1, 0),
('Ngô Minh', 'Giàu', '0988901234', 'giau.nm@company.com', '1991-07-15', 4, 1, 'Nam', 9, 1, 1, 1, 0, 1, 1),
('Trịnh Văn', 'Hùng', '0999012345', 'hung.tv@company.com', '1989-08-20', 2, 2, 'Nam', 10, 1, 1, 1, 1, 0, 0),
('Vũ Thị', 'Iến', '0900123456', 'ien.vt@company.com', '1994-09-25', 1, 1, 'Nữ', 11, 1, 1, 1, 1, 1, 1),
('Lý Văn', 'Nam', '0911234567', 'nam.lv@company.com', '1996-10-30', 1, 1, 'Nam', 12, 1, 1, 1, 1, 0, 0);
    
-- Giữ nguyên cấu trúc bảng account
CREATE TABLE `account` (
  `id` INT NOT NULL, 
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `last_login` DATETIME NULL,
  `status_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_account_employee` FOREIGN KEY (`id`) REFERENCES `employee` (`id`),
  CONSTRAINT `fk_account_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Cập nhật dữ liệu INSERT: Active (4), Locked (5)
INSERT INTO `account` (`id`, `username`, `password`, `status_id`) VALUES
(1, 'huyhoang119763', '$2a$12$ipuwsQs46H2VAcT1hwS/kuCpv.MXEvJ2IlcPWTyss6Gsm5hpsHWmy', 4),
(2, 'thanhlong123456', '$2a$12$ipuwsQs46H2VAcT1hwS/kuCpv.MXEvJ2IlcPWTyss6Gsm5hpsHWmy', 4),
(3, 'tlang01', '$2a$12$ipuwsQs46H2VAcT1hwS/kuCpv.MXEvJ2IlcPWTyss6Gsm5hpsHWmy', 4),
(4, 'lethib88', '$2a$12$ipuwsQs46H2VAcT1hwS/kuCpv.MXEvJ2IlcPWTyss6Gsm5hpsHWmy', 4),
(5, 'phamminhc85', '$2a$12$ipuwsQs46H2VAcT1hwS/kuCpv.MXEvJ2IlcPWTyss6Gsm5hpsHWmy', 4),
(6, 'nguyenthid92', '$2a$12$ipuwsQs46H2VAcT1hwS/kuCpv.MXEvJ2IlcPWTyss6Gsm5hpsHWmy', 4),
(7, 'dovane95', '$2a$12$ipuwsQs46H2VAcT1hwS/kuCpv.MXEvJ2IlcPWTyss6Gsm5hpsHWmy', 4),
(8, 'buithif93', '$2a$12$ipuwsQs46H2VAcT1hwS/kuCpv.MXEvJ2IlcPWTyss6Gsm5hpsHWmy', 4),
(9, 'ngominhg91', '$2a$12$ipuwsQs46H2VAcT1hwS/kuCpv.MXEvJ2IlcPWTyss6Gsm5hpsHWmy', 4),
(10, 'trinhvanh89', '$2a$12$ipuwsQs46H2VAcT1hwS/kuCpv.MXEvJ2IlcPWTyss6Gsm5hpsHWmy', 5),
(11, 'vuthii94', '$2a$12$ipuwsQs46H2VAcT1hwS/kuCpv.MXEvJ2IlcPWTyss6Gsm5hpsHWmy', 4),
(12, 'lyvanj96', '$2a$12$ipuwsQs46H2VAcT1hwS/kuCpv.MXEvJ2IlcPWTyss6Gsm5hpsHWmy', 4);

CREATE TABLE `customer` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `first_name` VARCHAR(100) NOT NULL,
  `last_name` VARCHAR(100) NOT NULL,
  `date_of_birth` DATE DEFAULT NULL,
  `phone` VARCHAR(15) NOT NULL,
  `address` VARCHAR(255) NOT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_customer_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `customer` (`first_name`, `last_name`, `date_of_birth`, `phone`, `address`, `status_id`) VALUES
('Vãng', 'Lai', null, '0000000000', '', 13),
('Nguyễn', 'Thành', '1990-02-15', '0123456789', '123 Đường Lê Lợi, Quận 1, Hồ Chí Minh', 13),
('Trần', 'Minh', '1985-04-20', '0987654321', '456 Đường Nguyễn Huệ, Quận 1, Hồ Chí Minh', 14),
('Lê', 'Hằng', '1995-08-30', '0912345678', '789 Đường Trần Hưng Đạo, Quận 5, Hồ Chí Minh', 13),
('Phạm', 'Hải', '1988-12-01', '0934567890', '321 Đường Bùi Viện, Quận 1, Hồ Chí Minh', 14),
('Đỗ', 'Lan', '1992-05-16', '0345678901', '654 Đường Lê Văn Sĩ, Quận 3, Hồ Chí Minh', 13),
('Nguyễn', 'Văn', '1993-11-11', '0123456780', '987 Đường Nguyễn Văn Cừ, Quận 5, Hồ Chí Minh', 14),
('Trần', 'Kiên', '1994-03-23', '0912345679', '234 Đường Trần Quốc Thảo, Quận 3, Hồ Chí Minh', 14),
('Lê', 'Phú', '1991-07-07', '0987654320', '567 Đường Phạm Ngọc Thạch, Quận 1, Hồ Chí Minh', 14),
('Ngô', 'Thảo', '1996-09-09', '0356789012', '890 Đường Võ Văn Tần, Quận 3, Hồ Chí Minh', 14),
('Bùi', 'Bích', '1987-01-20', '0123456781', '135 Đường Hàn Hải Nguyên, Quận 4, Hồ Chí Minh', 14),
('Mai', 'An', '1999-06-18', '0987654322', '246 Đường Cách Mạng Tháng 8, Quận 10, Hồ Chí Minh', 14),
('Vũ', 'Khoa', '1992-10-10', '0345678902', '357 Đường Nguyễn Trãi, Quận 5, Hồ Chí Minh', 13),
('Hà', 'Trang', '1989-05-21', '0934567891', '468 Đường Lê Quý Đôn, Quận 3, Hồ Chí Minh', 14),
('Phan', 'Nhi', '1995-12-30', '0123456782', '579 Đường Nguyễn Thị Minh Khai, Quận 1, Hồ Chí Minh', 14),
('Nguyễn', 'Lộc', '1994-04-14', '0987654323', '680 Đường Nam Kỳ Khởi Nghĩa, Quận 3, Hồ Chí Minh', 13),
('Lê', 'Quân', '1986-08-08', '0356789013', '791 Đường Điện Biên Phủ, Quận 1, Hồ Chí Minh', 14),
('Trương', 'Duy', '1993-11-02', '0123456783', '902 Đường Nguyễn Đình Chiểu, Quận 3, Hồ Chí Minh', 14),
('Ngô', 'Việt', '1988-07-19', '0912345680', '113 Đường Phan Đăng Lưu, Quận Bình Thạnh, Hồ Chí Minh', 14),
('Đỗ', 'Hòa', '1991-09-29', '0987654324', '224 Đường Huỳnh Văn Bánh, Quận Phú Nhuận, Hồ Chí Minh', 14),
('Nguyễn', 'Phúc', '1992-04-05', '0345678903', '456 Đường Nguyễn Thái Bình, Quận 1, Hồ Chí Minh', 14),
('Lê', 'Hưng', '1989-12-12', '0912345670', '567 Đường Lê Văn Sĩ, Quận 3, Hồ Chí Minh', 13),
('Đỗ', 'Nghĩa', '1995-05-25', '0987654325', '678 Đường Phạm Hồng Thái, Quận 10, Hồ Chí Minh', 14),
('Trần', 'Tú', '1994-07-30', '0356789014', '789 Đường Trần Bình Trọng, Quận 5, Hồ Chí Minh', 14),
('Lê', 'Đức', '1991-01-01', '0123456785', '890 Đường Lê Thánh Tôn, Quận 1, Hồ Chí Minh', 14),
('Nguyễn', 'Giang', '1993-03-03', '0987654326', '901 Đường Nguyễn Đình Chiểu, Quận 3, Hồ Chí Minh', 14),
('Trần', 'Thành', '1987-08-08', '0345678904', '123 Đường Trần Hưng Đạo, Quận 5, Hồ Chí Minh', 13),
('Mai', 'Hương', '1996-09-09', '0912345681', '234 Đường Cách Mạng Tháng 8, Quận 10, Hồ Chí Minh', 14);

CREATE TABLE `discount` (
  `code` VARCHAR(50) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `type` TINYINT(1) NOT NULL DEFAULT 0,
  `startDate` DATE NOT NULL,
  `endDate` DATE NOT NULL,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `discount` (`code`, `name`, `type`, `startDate`, `endDate`) VALUES
('CODE01','Khuyến mãi mùa hạ',0,'2024-02-05','2025-03-31'),
('CODE02','Khuyến mãi mùa hè',1,'2024-03-22','2025-03-31'),
('30T4','30 Tháng 4',0,'2025-04-30','2025-06-11');

CREATE TABLE `detail_discount` (
  `discount_code` VARCHAR(50) NOT NULL,
  `total_price_invoice` DECIMAL(12,2) NOT NULL,
  `discount_amount` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`discount_code`, `total_price_invoice`),
  CONSTRAINT `fk_discount_code` FOREIGN KEY (`discount_code`) REFERENCES `discount` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `detail_discount` (`discount_code`, `total_price_invoice`, `discount_amount`) VALUES
('CODE01', 50000.00, 5.00),
('CODE01', 100000.00, 7.00),
('CODE02', 30000.00, 2000.00),
('CODE02', 60000.00, 5000.00),
('30T4', 100000.00, 5.00),
('30T4', 200000.00, 7.00),
('30T4', 300000.00, 9.00);

CREATE TABLE `category` (
  `id` int(11) NOT NULL Auto_increment,
  `name` varchar(100) NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_category_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `category` (`id`, `name`, `status_id`) VALUES
(1, 'Chưa xác định', 9),
(2, 'Minifigure', 9),
(3, 'Technic', 9),
(4, 'Architecture', 9),
(5, 'Classic', 9),
(6, 'Moc', 9);

-- Tạo bảng Supplier
CREATE TABLE `supplier` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `phone` VARCHAR(15) NOT NULL,
  `address` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) DEFAULT NULL,
  `status_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_supplier_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Chèn dữ liệu vào bảng Supplier với email mẫu
INSERT INTO `supplier` (`id`, `name`, `phone`, `address`, `email`, `status_id`) VALUES
(1, 'Nhà cung cấp A', '0903344554', '99 An Dương Vương, Phường 16, Quận 8, TP Hồ Chí Minh', 'supplierA@example.com', 12),
(2, 'Nhà cung cấp B', '0903344556', '04 Tôn Đức Thắng, Phường Bến Nghé, Quận 1, TP Hồ Chí Minh', 'supplierB@example.com', 12),
(3, 'Nhà cung cấp C', '0903344557', '123 Nguyễn Thị Minh Khai, Quận 3, TP Hồ Chí Minh', 'supplierC@example.com', 12),
(4, 'Nhà cung cấp D', '0903344558', '456 Lê Lợi, Quận 1, TP Hồ Chí Minh', 'supplierD@example.com', 12),
(5, 'Nhà cung cấp E', '0903344559', '789 Trường Chinh, Quận Tân Bình, TP Hồ Chí Minh', 'supplierE@example.com', 12),
(6, 'Nhà cung cấp F', '0903344560', '101 Nguyễn Văn Cừ, Quận 5, TP Hồ Chí Minh', 'supplierF@example.com', 12),
(7, 'Nhà cung cấp G', '0903344561', '202 Phan Văn Trị, Quận Bình Thạnh, TP Hồ Chí Minh', 'supplierG@example.com', 12),
(8, 'Nhà cung cấp H', '0903344562', '303 Nguyễn Huệ, Quận 1, TP Hồ Chí Minh', 'supplierH@example.com', 12),
(9, 'Nhà cung cấp I', '0903344563', '404 Lê Văn Sỹ, Quận 3, TP Hồ Chí Minh', 'supplierI@example.com', 12),
(10, 'Nhà cung cấp J', '0903344564', '505 Bến Vân Đồn, Quận 4, TP Hồ Chí Minh', 'supplierJ@example.com', 12),
(11, 'Nhà cung cấp K', '0903344565', '606 Đinh Tiên Hoàng, Quận Bình Thạnh, TP Hồ Chí Minh', 'supplierK@example.com', 12),
(12, 'Nhà cung cấp L', '0903344566', '707 Trần Hưng Đạo, Quận 1, TP Hồ Chí Minh', 'supplierL@example.com', 12),
(13, 'Nhà cung cấp M', '0903344567', '808 Hoàng Văn Thụ, Quận Tân Bình, TP Hồ Chí Minh', 'supplierM@example.com', 12),
(14, 'Nhà cung cấp N', '0903344568', '909 Nguyễn Thái Sơn, Quận Gò Vấp, TP Hồ Chí Minh', 'supplierN@example.com', 12),
(15, 'Nhà cung cấp O', '0903344569', '1001 Lạc Long Quân, Quận 11, TP Hồ Chí Minh', 'supplierO@example.com', 12),
(16, 'Nhà cung cấp P', '0903344570', '1102 Âu Cơ, Quận Tân Phú, TP Hồ Chí Minh', 'supplierP@example.com', 12),
(17, 'Nhà cung cấp Q', '0903344571', '1203 Trần Quốc Toản, Quận 3, TP Hồ Chí Minh', 'supplierQ@example.com', 12),
(18, 'Nhà cung cấp R', '0903344572', '1304 Ngô Quyền, Quận 10, TP Hồ Chí Minh', 'supplierR@example.com', 12),
(19, 'Nhà cung cấp S', '0903344573', '1405 Đinh Bộ Lĩnh, Quận Bình Thạnh, TP Hồ Chí Minh', 'supplierS@example.com', 12),
(20, 'Nhà cung cấp T', '0903344574', '1506 Huỳnh Tấn Phát, Quận 7, TP Hồ Chí Minh', 'supplierT@example.com', 12);

CREATE TABLE `product` (
  `id` NVARCHAR(50) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `stock_quantity` INT(11) NOT NULL DEFAULT 0,
  `selling_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `import_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `status_id` INT NOT NULL,
  `description` TEXT DEFAULT NULL,
  `image_url` VARCHAR(255) DEFAULT NULL,
  `category_id` INT(11) NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_product_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`),
  CONSTRAINT `fk_category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `product` (`id`, `name`, `stock_quantity`, `selling_price`, `import_price`, `status_id`, `description`, `image_url`, `category_id`) VALUES
('SP00001', 'Naruto - 01', 38, 21000.00, 20000.00, 7, 'Minifigure nhân vật Naruto.', 'images/product/sp00001.png', 2),
('SP00002', 'Naruto - 02', 37, 18900.00, 18000.00, 7, 'Minifigure Naruto trong trạng thái chiến đấu.', 'images/product/sp00002.png', 2),
('SP00003', 'Sasuke Uchiha', 0, 0.00, 0.00, 8, 'Minifigure nhân vật Sasuke Uchiha từ series Naruto.', NULL, 2),
('SP00004', 'Kakashi Hatake', 19, 15750.00, 15000.00, 7, 'Minifigure nhân vật Kakashi với Sharingan.', NULL, 2),
('SP00005', 'Sakura Haruno', 20, 26250.00, 25000.00, 7, 'Minifigure nhân vật Sakura từ series Naruto.', NULL, 2),
('SP00007', 'Darth Vader', 19, 42000.00, 40000.00, 7, 'Minifigure Darth Vader với lightsaber đỏ và mặt nạ.', NULL, 2),
('SP00008', 'Iron Man Mark 85', 20, 31500.00, 30000.00, 7, 'Minifigure Iron Man trong bộ giáp Mark 85 từ Avengers: Endgame.', NULL, 2),
('SP00010', 'Harry Potter', 19, 105000.00, 100000.00, 7, 'Minifigure Harry Potter với đũa phép và kính tròn.', NULL, 2),
('SP00011', 'LEGO Technic Bugatti Chiron', 20, 31500.00, 30000.00, 7, 'Mô hình kỹ thuật cao của siêu xe Bugatti Chiron.', NULL, 3),
('SP00014', 'LEGO Technic Race Car', 20, 47250.00, 45000.00, 7, 'Xe đua công thức 1 với động cơ pistons hoạt động.', NULL, 3),
('SP00019', 'LEGO Architecture Empire State Building', 0, 0.00, 0.00, 7, 'Mô hình chi tiết của tòa nhà Empire State.', NULL, 4),
('SP00030', 'MOC - Tháp Rùa Hồ Gươm', 0, 0.00, 0.00, 7, 'Mô hình Tháp Rùa trên Hồ Gươm, 1250 chi tiết.', NULL, 6),
('SP00031', 'LEGO City Police Station', 0, 0.00, 0.00, 7, 'Trụ sở cảnh sát thành phố.', NULL, 6),
('SP00036', 'LEGO Star Wars Millennium Falcon', 0, 0.00, 0.00, 7, 'Tàu Millennium Falcon với nhiều nhân vật.', NULL, 4),
('SP00038', 'LEGO Star Wars AT-AT', 0, 0.00, 0.00, 7, 'Walker AT-AT từ phim The Empire Strikes Back.', NULL, 4),
('SP00039', 'LEGO Star Wars Death Star', 0, 0, 0, 7, 'Ngôi sao tử thần Death Star.', NULL, 4),
('SP00040', 'LEGO Star Wars X-Wing Starfighter', 0, 0, 65000.00, 7, 'Tàu chiến X-Wing của Luke Skywalker.', NULL, 4),
('SP00042', 'LEGO Marvel Sanctum Sanctorum', 0, 0, 0, 7, 'Sanctum Sanctorum của Doctor Strange.', NULL, 3),
('SP00043', 'LEGO Marvel Guardians Ship', 0, 0, 0, 7, 'Tàu của đội Guardians of the Galaxy.', NULL, 3),
('SP00044', 'LEGO Marvel Spider-Man Daily Bugle', 0, 0, 0, 7, 'Tòa nhà Daily Bugle với nhiều nhân vật Spider-Man.', NULL, 3),
('SP00048', 'LEGO Harry Potter Hogwarts Express', 0, 0, 0, 7, 'Tàu Hogwarts Express với sân ga 9¾.', NULL, 1),
('SP00049', 'LEGO Harry Potter Chamber of Secrets', 0, 0, 0, 7, 'Phòng chứa bí mật với rắn Basilisk.', NULL, 1),
('SP00051', 'LEGO Creator Expert Bookshop', 0, 0, 0, 7, 'Hiệu sách chi tiết với căn hộ ở trên.', NULL, 2),
('SP00052', 'LEGO Creator Expert Assembly Square', 0, 0, 0, 7, 'Quảng trường trung tâm với nhiều tòa nhà.', NULL, 4);

CREATE TABLE `invoice` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `create_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `employee_id` INT(11) NOT NULL,
  `customer_id` INT(11) NOT NULL,
  `discount_code` VARCHAR(50),
  `discount_amount` DECIMAL(10,2) NOT NULL,
  `total_price` DECIMAL(12,2) NOT NULL,
  `status_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`),
  FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  FOREIGN KEY (`status_id`) REFERENCES `status` (`id`),
  FOREIGN KEY (`discount_code`) REFERENCES `discount` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- 1. Tạo Hóa đơn (Status 15: COMPLETED)
INSERT INTO `invoice` (`id`, `create_date`, `employee_id`, `customer_id`, `discount_code`, `discount_amount`, `total_price`, `status_id`) VALUES
(1, '2024-02-01 10:00:00', 1, 1, NULL, 0.00, 98700.00, 15),
(2, '2024-02-10 14:20:00', 1, 2, NULL, 0.00, 162750.00, 15);

CREATE TABLE `detail_invoice` (
  `invoice_id` INT(11) NOT NULL,
  `product_id` NVARCHAR(50) NOT NULL,
  `quantity` INT(11) NOT NULL DEFAULT 1,
  `price` DECIMAL(10,2) NOT NULL,        -- Giá bán cho khách
  `cost_price` DECIMAL(10,2) NOT NULL,   -- GIÁ VỐN LÚC BÁN (Snapshot)
  `total_price` DECIMAL(10,2) NOT NULL,  -- quantity * price
  PRIMARY KEY (`invoice_id`, `product_id`),
  FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`id`),
  FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- 2. Chi tiết hóa đơn (Lưu Snapshot giá vốn)
INSERT INTO `detail_invoice` (`invoice_id`, `product_id`, `quantity`, `price`, `cost_price`, `total_price`) VALUES
-- Đơn 101: Bán Naruto 01 và 02
(1, 'SP00001', 2, 21000.00, 20000.00, 60000.00),
(1, 'SP00002', 3, 18900.00, 18000.00, 82500.00),

-- Đơn 102: Bán Kakashi và Harry Potter
(2, 'SP00004', 1, 15750.00, 15000.00, 15750.00),
(2, 'SP00010', 1, 105000.00, 100000.00, 105000.00),
(2, 'SP00007', 1, 42000.00, 40000.00, 42000.00);

CREATE TABLE `import` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `create_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `employee_id` INT(11) NOT NULL,
  `supplier_id` INT(11) NOT NULL,
  `total_price` DECIMAL(12,2) NOT NULL,
  `status_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`),
  FOREIGN KEY (`status_id`) REFERENCES `status` (`id`),
  FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- 1. Tạo Phiếu nhập (Status 17: COMPLETED)
INSERT INTO `import` (`id`, `create_date`, `employee_id`, `supplier_id`, `total_price`, `status_id`) VALUES
(1, '2024-01-01 08:00:00', 1, 1, 6460000.00, 17), -- Lô đầu tiên (Đã đẩy)
(2, '2024-02-15 09:30:00', 1, 1, 800000.00, 17);   -- Lô chờ (Chưa đẩy)

CREATE TABLE `detail_import` (
  `import_id` INT(11) NOT NULL,
  `product_id` NVARCHAR(50) NOT NULL,
  `quantity` INT(11) NOT NULL,
  `profit_percent` DECIMAL(10,2) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `total_price` DECIMAL(10,2) NOT NULL,
  `is_pushed` TINYINT(1) NOT NULL DEFAULT 0, -- 1: Đã đẩy giá, 0: Đang chờ
  PRIMARY KEY (`import_id`, `product_id`),
  FOREIGN KEY (`import_id`) REFERENCES `import` (`id`),
  FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- 2. Chi tiết phiếu nhập
-- Lô 1: Nhập 20 cái cho mỗi SP (từ SP00001 đến SP00014 - bỏ qua SP00003 vì giá 0)
INSERT INTO `detail_import` (`import_id`, `product_id`, `quantity`, `price`, `total_price`, `is_pushed`) VALUES
(1, 'SP00001', 20, 20000.00, 400000.00, 1),
(1, 'SP00002', 20, 18000.00, 360000.00, 1),
(1, 'SP00004', 20, 15000.00, 300000.00, 1),
(1, 'SP00005', 20, 25000.00, 500000.00, 1),
(1, 'SP00007', 20, 40000.00, 800000.00, 1),
(1, 'SP00008', 20, 30000.00, 600000.00, 1),
(1, 'SP00010', 20, 100000.00, 2000000.00, 1),
(1, 'SP00011', 20, 30000.00, 600000.00, 1),
(1, 'SP00014', 20, 45000.00, 900000.00, 1);

-- Lô 2: Nhập thêm Naruto 01 và 02 (Giá tăng, đang CHỜ ĐẨY)
-- Bạn dùng cái này để test: Khi bán hết 20 cái cũ, sẽ lấy giá 22k và 20k này đẩy vào Product.
INSERT INTO `detail_import` (`import_id`, `product_id`, `quantity`, `price`, `total_price`, `is_pushed`) VALUES
(2, 'SP00001', 20, 22000.00, 440000.00, 0),
(2, 'SP00002', 20, 20000.00, 400000.00, 0);

CREATE TABLE `leave_request` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` varchar(100) DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `status_id` int NOT NULL,
  `employee_id` int NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `leave_request_ibfk_1` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`),
  CONSTRAINT `leave_request_ibfk_2` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `leave_request` (`type`, `content`, `start_date`, `end_date`, `status_id`, `employee_id`) VALUES
('Nghỉ phép', 'Về quê ăn giỗ', '2026-02-10', '2026-02-12', 19, 1),
('Nghỉ bệnh', 'Sốt xuất huyết', '2026-02-01', '2026-02-05', 20, 2),
('Nghỉ việc riêng', 'Đi đám cưới bạn thân', '2026-02-15', '2026-02-15', 21, 3),
('Nghỉ thai sản', 'Nghỉ sinh con theo chế độ', '2026-03-01', '2026-09-01', 20, 4),
('Nghỉ phép', 'Đi du lịch Đà Lạt', '2026-02-20', '2026-02-25', 21, 5),
('Nghỉ việc riêng', 'Giải quyết việc gia đình', '2026-02-03', '2026-02-04', 19, 6);

CREATE TABLE `time_sheet` (
  `id` int NOT NULL AUTO_INCREMENT,
  `employee_id` int NOT NULL,
  `check_in` DATETIME NOT NULL,  -- Ngày và giờ vào ca
  `check_out` DATETIME DEFAULT NULL, -- Ngày và giờ ra ca (có thể NULL nếu chưa ra)
  `work_hours` DECIMAL(10,2) DEFAULT 0,
  PRIMARY KEY (`id`),
  CONSTRAINT `time_sheet_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- Seed cho mỗi nhân viên 1 ngày làm việc mẫu 8 tiếng để test
INSERT INTO `time_sheet` (`employee_id`, `check_in`, `check_out`, `work_hours`)
SELECT id, '2026-02-01 08:00:00', '2026-02-01 17:00:00', 8.00 FROM `employee`;

-- Thêm một ngày OT (làm 10 tiếng) cho nhân viên ID 2 và 3 để test Overtime
INSERT INTO `time_sheet` (`employee_id`, `check_in`, `check_out`, `work_hours`) VALUES 
(2, '2026-02-02 08:00:00', '2026-02-02 19:00:00', 10.00),
(3, '2026-02-02 08:00:00', '2026-02-02 20:00:00', 11.00);

CREATE TABLE `profit_stat` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `stat_date` DATE NOT NULL,             -- Ngày thống kê (Ngày đầu tháng hoặc từng ngày)
  `revenue` DECIMAL(15,2) NOT NULL DEFAULT 0.00,  -- Tổng doanh thu
  `expense` DECIMAL(15,2) NOT NULL DEFAULT 0.00,  -- Tổng chi phí (Giá vốn + lương...)
  `total_profit` DECIMAL(15,2) NOT NULL DEFAULT 0.00, -- Lợi nhuận ròng
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời điểm tạo bản ghi
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Cập nhật lần cuối
  PRIMARY KEY (`id`),
  -- Ràng buộc quan trọng: Mỗi mốc thời gian chỉ có duy nhất 1 bản ghi thống kê
  UNIQUE KEY `uk_stat_date` (`stat_date`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `report` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  `description` TEXT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Giờ phút giây sự cố
  `level` VARCHAR(50) NOT NULL,    -- Lưu chuỗi: HIGH, MEDIUM, LOW
  `category` VARCHAR(50) NOT NULL, -- Lưu chuỗi: SYSTEM, SECURITY...
  `employee_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_report_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `fine` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `reason` VARCHAR(255) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Giờ phút giây chuẩn
  `fine_level` VARCHAR(50) NOT NULL,    -- Lưu tên Enum (LEVEL_1, LEVEL_2...)
  `amount` DECIMAL(15,2) NOT NULL,      -- Tổng tiền phạt (Dùng Decimal cho chuẩn)
  `fine_pay` DECIMAL(15,2) DEFAULT 0,   -- Số tiền đã nộp trước
  `employee_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fine_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `fine` (`reason`, `fine_level`, `amount`, `employee_id`) VALUES 
('Làm vỡ bộ LEGO Ferrari', 'LEVEL_3', 500000.00, 12),
('Đi muộn', 'LEVEL_1', 50000.00, 8);

CREATE TABLE `payroll_history` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `employee_id` INT NOT NULL,           
  `salary_period` DATE NOT NULL,        -- Lưu ngày 01 của tháng (VD: 2026-02-01)
  `temporary_salary` DECIMAL(15,2) NOT NULL, -- Lương cơ bản theo hợp đồng/chức vụ
  `overtime_amount` DECIMAL(15,2) DEFAULT 0,  -- Tiền làm thêm (tính từ số giờ overtime)
  `total_allowance` DECIMAL(15,2) DEFAULT 0,  -- Tổng phụ cấp (ăn trưa, xăng xe...)
  `total_bonus` DECIMAL(15,2) DEFAULT 0,      -- Tiền thưởng doanh số/thưởng nóng
  `total_deduction` DECIMAL(15,2) DEFAULT 0,  -- Tổng khấu trừ (BHXH, thuế...)
  `total_fine` DECIMAL(15,2) DEFAULT 0,       -- Tổng tiền phạt (lấy từ bảng fine)
  `net_salary` DECIMAL(15,2) NOT NULL,        -- Lương thực lĩnh cuối cùng
  `paid_date` DATETIME DEFAULT NULL,          -- Ngày thực tế bấm nút trả lương
  PRIMARY KEY (`id`),
  -- Ràng buộc: Một nhân viên chỉ có 1 phiếu lương duy nhất cho 1 tháng
  UNIQUE KEY `uk_employee_period` (`employee_id`, `salary_period`),
  CONSTRAINT `fk_payroll_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `deduction` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `employee_id` INT NOT NULL,
  `salary_period` DATE NOT NULL, -- Quan trọng: Để biết khấu trừ cho tháng nào
  `health_insurance` DECIMAL(15,2) DEFAULT 0.00,
  `social_insurance` DECIMAL(15,2) DEFAULT 0.00,
  `unemployment_insurance` DECIMAL(15,2) DEFAULT 0.00,
  `personal_income_tax` DECIMAL(15,2) DEFAULT 0.00,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_deduction_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `tax` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `employee_id` INT NOT NULL,
  `num_dependents` INT DEFAULT 0, -- Chỉ cần cột này để tính toán
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee` (`employee_id`),
  CONSTRAINT `fk_tax_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `allowance` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `employee_id` INT NOT NULL,
  `salary_period` DATE NOT NULL,         -- Tháng/Năm nhận trợ cấp (VD: 2026-02-01)
  `attendance_bonus` DECIMAL(15,2) DEFAULT 0,      -- Thưởng chuyên cần
  `annual_leave_days` DECIMAL(5,1) DEFAULT 0,      -- Số ngày nghỉ phép (để double/decimal vì có thể nghỉ nửa ngày 0.5)
  `transportation_support` DECIMAL(15,2) DEFAULT 0, -- Hỗ trợ đi lại
  `accommodation_support` DECIMAL(15,2) DEFAULT 0,  -- Hỗ trợ chỗ ở
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_allowance_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `file` (
  `id` int NOT NULL AUTO_INCREMENT,
  `file_path` varchar(255) NOT NULL,
  `file_name` varchar(100) NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `holiday` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `date` DATE NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `holiday` (`name`, `date`) VALUES
('Tết Dương lịch', '2026-01-01'),
-- Tết Nguyên Đán (Dự kiến nghỉ từ 29 Tết đến mùng 5 Tết)
('Giao thừa Tết Nguyên Đán', '2026-02-16'),
('Mùng 1 Tết Nguyên Đán', '2026-02-17'),
('Mùng 2 Tết Nguyên Đán', '2026-02-18'),
('Mùng 3 Tết Nguyên Đán', '2026-02-19'),
('Mùng 4 Tết Nguyên Đán', '2026-02-20'),
('Mùng 5 Tết Nguyên Đán', '2026-02-21'),
-- Các ngày lễ khác
('Giỗ tổ Hùng Vương', '2026-04-26'), -- 10/3 Âm lịch rơi vào 26/04
('Ngày Giải phóng miền Nam', '2026-04-30'),
('Ngày Quốc tế Lao động', '2026-05-01'),
('Ngày Quốc khánh', '2026-09-02'),
('Ngày Quốc khánh (nghỉ thêm)', '2026-09-01');

INSERT INTO `tax` (`employee_id`, `num_dependents`)
SELECT id, 0 FROM `employee`; -- Mặc định ai cũng 0 người phụ thuộc cho nhanh

INSERT INTO `allowance` (`employee_id`, `salary_period`, `transportation_support`, `accommodation_support`)
SELECT 
    id, 
    '2026-02-01', 
    IF(is_transportation_support = 1, 500000, 0), 
    IF(is_accommodation_support = 1, 1000000, 0)
FROM `employee`;

INSERT INTO `deduction` (`employee_id`, `salary_period`, `health_insurance`, `social_insurance`, `unemployment_insurance`)
SELECT 
    id, 
    '2026-02-01', 
    IF(is_health_insurance = 1, 150000, 0), 
    IF(is_social_insurance = 1, 800000, 0),
    IF(is_unemployment_insurance = 1, 100000, 0)
FROM `employee`;