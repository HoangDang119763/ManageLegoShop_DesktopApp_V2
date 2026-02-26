-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: java_sql
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES 
(1,'admin','$2a$12$QIBl3fm0aG.SDhGTldUk5eTFgClKWp1HjNP06Er4utLo/kG1dNpCG','2026-02-24 22:00:04','2026-02-25 21:06:02',4,1,0),
(2,'huyhoang119763','$2a$12$ipuwsQs46H2VAcT1hwS/kuCpv.MXEvJ2IlcPWTyss6Gsm5hpsHWmy','2026-02-24 22:00:04',NULL,4,2,0),
(3,'vuithii','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-24 22:00:04',NULL,4,2,0),
(4,'lyvan','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-24 22:00:04',NULL,4,2,0),
(5,'nguyenthanh','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-24 22:00:04',NULL,4,3,0),
(6,'trinhvan','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-24 22:00:04',NULL,4,3,0),
(7,'tanthien','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-24 22:00:04',NULL,4,4,0),
(8,'lethib','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-24 22:00:04',NULL,4,3,0),
(9,'phamminh','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-24 22:00:04',NULL,4,4,0),
(10,'nguyenthi','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-24 22:00:04',NULL,4,4,0),
(11,'ngominh','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-24 22:00:04',NULL,4,6,0),
(12,'buithiph','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-24 22:00:04',NULL,4,6,0),
(13,'dovan','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-24 22:00:04',NULL,4,7,0);

/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `allowance`
--

LOCK TABLES `allowance` WRITE;
/*!40000 ALTER TABLE `allowance` DISABLE KEYS */;
INSERT INTO `allowance` VALUES (1,'Ăn uống',500000.00,'2026-02-26 00:27:44','2026-02-26 00:27:44'),(2,'Chỗ ở',2000000.00,'2026-02-26 00:27:44','2026-02-26 00:27:44'),(3,'Đi lại',500000.00,'2026-02-26 00:27:44','2026-02-26 00:27:44');
/*!40000 ALTER TABLE `allowance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'Chưa xác định','2026-02-24 22:00:04','2026-02-24 22:00:04',9),(2,'Minifigure','2026-02-24 22:00:04','2026-02-24 22:00:04',9),(3,'Technic','2026-02-24 22:00:04','2026-02-24 22:00:04',9),(4,'Architecture','2026-02-24 22:00:04','2026-02-24 22:00:04',9),(5,'Classic','2026-02-24 22:00:04','2026-02-24 22:00:04',9),(6,'Moc','2026-02-24 22:00:04','2026-02-24 22:00:04',9);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (1,'Vãng','Lai',NULL,'0000000000','','2026-02-24 15:00:04',13),(2,'Nguyễn','Thành','1990-02-15','0123456789','123 Đường Lê Lợi, Quận 1, Hồ Chí Minh','2026-02-24 15:00:04',13),(3,'Trần','Minh','1985-04-20','0987654321','456 Đường Nguyễn Huệ, Quận 1, Hồ Chí Minh','2026-02-24 15:00:04',13),(4,'Lê','Hằng','1995-08-30','0912345678','789 Đường Trần Hưng Đạo, Quận 5, Hồ Chí Minh','2026-02-24 15:00:04',13),(5,'Phạm','Hải','1988-12-01','0934567890','321 Đường Bùi Viện, Quận 1, Hồ Chí Minh','2026-02-24 15:00:04',13),(6,'Đỗ','Lan','1992-05-16','0345678901','654 Đường Lê Văn Sĩ, Quận 3, Hồ Chí Minh','2026-02-24 15:00:04',13),(7,'Nguyễn','Văn','1993-11-11','0123456780','987 Đường Nguyễn Văn Cừ, Quận 5, Hồ Chí Minh','2026-02-24 15:00:04',13),(8,'Trần','Kiên','1994-03-23','0912345679','234 Đường Trần Quốc Thảo, Quận 3, Hồ Chí Minh','2026-02-24 15:00:04',13),(9,'Lê','Phú','1991-07-07','0987654320','567 Đường Phạm Ngọc Thạch, Quận 1, Hồ Chí Minh','2026-02-24 15:00:04',13),(10,'Ngô','Thảo','1996-09-09','0356789012','890 Đường Võ Văn Tần, Quận 3, Hồ Chí Minh','2026-02-24 15:00:04',13),(11,'Bùi','Bích','1987-01-20','0123456781','135 Đường Hàn Hải Nguyên, Quận 4, Hồ Chí Minh','2026-02-24 15:00:04',13),(12,'Mai','An','1999-06-18','0987654322','246 Đường Cách Mạng Tháng 8, Quận 10, Hồ Chí Minh','2026-02-24 15:00:04',13),(13,'Vũ','Khoa','1992-10-10','0345678902','357 Đường Nguyễn Trãi, Quận 5, Hồ Chí Minh','2026-02-24 15:00:04',13),(14,'Hà','Trang','1989-05-21','0934567891','468 Đường Lê Quý Đôn, Quận 3, Hồ Chí Minh','2026-02-24 15:00:04',13),(15,'Phan','Nhi','1995-12-30','0123456782','579 Đường Nguyễn Thị Minh Khai, Quận 1, Hồ Chí Minh','2026-02-24 15:00:04',13),(16,'Nguyễn','Lộc','1994-04-14','0987654323','680 Đường Nam Kỳ Khởi Nghĩa, Quận 3, Hồ Chí Minh','2026-02-24 15:00:04',13),(17,'Lê','Quân','1986-08-08','0356789013','791 Đường Điện Biên Phủ, Quận 1, Hồ Chí Minh','2026-02-24 15:00:04',13),(18,'Trương','Duy','1993-11-02','0123456783','902 Đường Nguyễn Đình Chiểu, Quận 3, Hồ Chí Minh','2026-02-24 15:00:04',13),(19,'Ngô','Việt','1988-07-19','0912345680','113 Đường Phan Đăng Lưu, Quận Bình Thạnh, Hồ Chí Minh','2026-02-24 15:00:04',13),(20,'Đỗ','Hòa','1991-09-29','0987654324','224 Đường Huỳnh Văn Bánh, Quận Phú Nhuận, Hồ Chí Minh','2026-02-24 15:00:04',13),(21,'Nguyễn','Phúc','1992-04-05','0345678903','456 Đường Nguyễn Thái Bình, Quận 1, Hồ Chí Minh','2026-02-24 15:00:04',13),(22,'Lê','Hưng','1989-12-12','0912345670','567 Đường Lê Văn Sĩ, Quận 3, Hồ Chí Minh','2026-02-24 15:00:04',13),(23,'Đỗ','Nghĩa','1995-05-25','0987654325','678 Đường Phạm Hồng Thái, Quận 10, Hồ Chí Minh','2026-02-24 15:00:04',13),(24,'Trần','Tú','1994-07-30','0356789014','789 Đường Trần Bình Trọng, Quận 5, Hồ Chí Minh','2026-02-24 15:00:04',13),(25,'Lê','Đức','1991-01-01','0123456785','890 Đường Lê Thánh Tôn, Quận 1, Hồ Chí Minh','2026-02-24 15:00:04',13),(26,'Nguyễn','Giang','1993-03-03','0987654326','901 Đường Nguyễn Đình Chiểu, Quận 3, Hồ Chí Minh','2026-02-24 15:00:04',13),(27,'Trần','Thành','1987-08-08','0345678904','123 Đường Trần Hưng Đạo, Quận 5, Hồ Chí Minh','2026-02-24 15:00:04',13),(28,'Mai','Hương','1996-09-09','0912345681','234 Đường Cách Mạng Tháng 8, Quận 10, Hồ Chí Minh','2026-02-24 15:00:04',13);
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `department`
--

LOCK TABLES `department` WRITE;
/*!40000 ALTER TABLE `department` DISABLE KEYS */;
INSERT INTO `department` VALUES (1,'Phòng Hội đồng quản trị','Ban lãnh đạo cấp cao',29,'2026-02-24 22:00:03','2026-02-24 22:00:04'),(2,'Phòng Nhân sự','Quản lý tuyển dụng và đào tạo',29,'2026-02-24 22:00:03','2026-02-24 22:00:04'),(3,'Phòng Kinh doanh','Tiếp thị và bán lẻ sản phẩm LEGO',29,'2026-02-24 22:00:03','2026-02-24 22:00:04'),(4,'Phòng Kho vận','Quản lý nhập xuất hàng hóa',29,'2026-02-24 22:00:03','2026-02-24 22:00:04'),(5,'Phòng Kỹ thuật','Bảo trì hệ thống và hỗ trợ',29,'2026-02-24 22:00:03','2026-02-24 22:00:04');
/*!40000 ALTER TABLE `department` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `detail_discount`
--

LOCK TABLES `detail_discount` WRITE;
/*!40000 ALTER TABLE `detail_discount` DISABLE KEYS */;
INSERT INTO `detail_discount` VALUES ('30T4',100000.00,5.00),('30T4',200000.00,7.00),('30T4',300000.00,9.00),('CODE01',50000.00,5.00),('CODE01',100000.00,7.00),('CODE02',30000.00,2000.00),('CODE02',60000.00,5000.00);
/*!40000 ALTER TABLE `detail_discount` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `detail_import`
--

LOCK TABLES `detail_import` WRITE;
/*!40000 ALTER TABLE `detail_import` DISABLE KEYS */;
/*!40000 ALTER TABLE `detail_import` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `detail_invoice`
--

LOCK TABLES `detail_invoice` WRITE;
/*!40000 ALTER TABLE `detail_invoice` DISABLE KEYS */;
/*!40000 ALTER TABLE `detail_invoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `discount`
--

LOCK TABLES `discount` WRITE;
/*!40000 ALTER TABLE `discount` DISABLE KEYS */;
INSERT INTO `discount` VALUES ('30T4','30 Tháng 4',0,'2025-04-30','2025-06-11'),('CODE01','Khuyến mãi mùa hạ',0,'2024-02-05','2025-03-31'),('CODE02','Khuyến mãi mùa hè',1,'2024-03-22','2025-03-31');
/*!40000 ALTER TABLE `discount` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `employee`
--

LOCK TABLES `employee` WRITE;
/*!40000 ALTER TABLE `employee` DISABLE KEYS */;
INSERT INTO `employee` (
    `id`, `first_name`, `last_name`, `phone`, `email`, 
    `date_of_birth`, `gender`, `department_id`, `status_id`, `account_id`, 
    `avatar_url`, `position_id`, `health_insurance_code`, `social_insurance_code`, `unemployment_insurance_code`, 
    `is_meal_support`, `is_transportation_support`, `is_accommodation_support`, `num_dependents`, `updated_at`, `created_at`
) VALUES 
(1,'ID','ADMIN','','admin@company.com',NULL,'Nam',0,1,1,NULL,1,'HI-000000','SI-000000','UI-000000',1,1,1,5,'2026-02-26 01:00:25','2026-02-24 22:00:03'),
(2,'Đặng Huy','Hoàng','0123456789','hoang.dh@company.com','2004-06-11','Nam',1,1,2,NULL,1,'HI-2026001','SI-2026001','UI-2026001',1,1,1,0,'2026-02-26 00:10:51','2026-02-24 22:00:03'),
(3,'Vũ Thị','Iến','0900123456','ien.vt@company.com','1994-09-25','Nữ',1,1,3,NULL,1,'HI-2026011','SI-2026011','UI-2026011',1,1,1,0,'2026-02-26 00:10:51','2026-02-24 22:00:03'),
(4,'Lý Văn','Nam','0911234567','nam.lv@company.com','1996-10-30','Nam',1,1,4,NULL,1,'HI-2026012','SI-2026012','UI-2026012',1,0,0,0,'2026-02-26 00:10:51','2026-02-24 22:00:03'),
(5,'Nguyễn Thành','Long','0987654321','long.nt@company.com','2003-04-11','Nam',2,1,5,NULL,1,'HI-2026002','SI-2026002','UI-2026002',1,0,0,0,'2026-02-26 00:10:51','2026-02-24 22:00:03'),
(6,'Trịnh Văn','Hùng','0999012345','hung.tv@company.com','1989-08-20','Nam',2,1,6,NULL,1,'HI-2026010','SI-2026010','UI-2026010',1,0,0,0,'2026-02-26 00:10:51','2026-02-24 22:00:03'),
(7,'Tần Thiên','Lang','0912345678','lang.tt@company.com','2000-01-15','Nam',3,1,7,NULL,1,'HI-2026003','SI-2026003','UI-2026003',0,1,0,0,'2026-02-26 00:10:51','2026-02-24 22:00:03'),
(8,'Lê Thị','Bích','0933456789','bich.lt@company.com','1988-02-20','Nữ',4,1,8,NULL,1,'HI-2026004','0','UI-2026004',1,0,1,0,'2026-02-26 00:10:51','2026-02-24 22:00:03'),
(9,'Phạm Minh','Chính','0944567890','chinh.pm@company.com','1985-03-25','Nam',3,1,9,NULL,1,'HI-2026005','SI-2026005','0',1,1,1,0,'2026-02-26 00:10:51','2026-02-24 22:00:03'),
(10,'Nguyễn Thị','Diệu','0955678901','dieu.nt@company.com','1992-04-30','Nữ',3,1,10,NULL,1,'HI-2026006','0','0',1,0,0,0,'2026-02-26 00:10:51','2026-02-24 22:00:03'),
(11,'Ngô Minh','Giàu','0988901234','giau.nm@company.com','1991-07-15','Nam',3,1,11,NULL,1,'HI-2026009','SI-2026009','UI-2026009',0,1,1,0,'2026-02-26 00:10:51','2026-02-24 22:00:03'),
(12,'Bùi Thị','Phượng','0977890123','phuong.bt@company.com','1993-06-10','Nữ',3,1,12,NULL,1,'HI-2026008','SI-2026008','UI-2026008',1,1,0,0,'2026-02-26 00:10:51','2026-02-24 22:00:03'),
(13,'Đỗ Văn','Em','0966789012','em.dv@company.com','1995-05-05','Nam',4,1,13,NULL,1,'HI-2026007','SI-2026007','UI-2026007',0,0,0,0,'2026-02-26 00:10:51','2026-02-24 22:00:03');
/*!40000 ALTER TABLE `employee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `employment_history`
--

LOCK TABLES `employment_history` WRITE;
/*!40000 ALTER TABLE `employment_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `employment_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `file`
--

LOCK TABLES `file` WRITE;
/*!40000 ALTER TABLE `file` DISABLE KEYS */;
/*!40000 ALTER TABLE `file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `fine`
--

LOCK TABLES `fine` WRITE;
/*!40000 ALTER TABLE `fine` DISABLE KEYS */;
INSERT INTO `fine` VALUES (1,'Làm vỡ bộ LEGO Ferrari','2026-02-24 22:00:04','LEVEL_3',500000.00,0.00,12),(2,'Đi muộn','2026-02-24 22:00:04','LEVEL_1',50000.00,0.00,8);
/*!40000 ALTER TABLE `fine` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `holiday`
--

LOCK TABLES `holiday` WRITE;
/*!40000 ALTER TABLE `holiday` DISABLE KEYS */;
/*!40000 ALTER TABLE `holiday` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `import`
--

LOCK TABLES `import` WRITE;
/*!40000 ALTER TABLE `import` DISABLE KEYS */;
/*!40000 ALTER TABLE `import` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `invoice`
--

LOCK TABLES `invoice` WRITE;
/*!40000 ALTER TABLE `invoice` DISABLE KEYS */;
/*!40000 ALTER TABLE `invoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `leave_request`
--

LOCK TABLES `leave_request` WRITE;
/*!40000 ALTER TABLE `leave_request` DISABLE KEYS */;
INSERT INTO `leave_request` VALUES (1,1,'Về quê ăn giỗ','2026-02-10','2026-02-12',23,1),(2,1,'Sốt xuất huyết','2026-02-01','2026-02-05',23,2),(3,1,'Đi đám cưới bạn thân','2026-02-15','2026-02-15',23,3),(4,1,'Nghỉ sinh con theo chế độ','2026-03-01','2026-09-01',23,4),(5,1,'Đi du lịch Đà Lạt','2026-02-20','2026-02-25',23,5),(6,1,'Giải quyết việc gia đình','2026-02-03','2026-02-04',23,6);
/*!40000 ALTER TABLE `leave_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `leave_type`
--

LOCK TABLES `leave_type` WRITE;
/*!40000 ALTER TABLE `leave_type` DISABLE KEYS */;
INSERT INTO `leave_type` VALUES (1,'Nghỉ có phép',500000.00);
/*!40000 ALTER TABLE `leave_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `module`
--

LOCK TABLES `module` WRITE;
/*!40000 ALTER TABLE `module` DISABLE KEYS */;
INSERT INTO `module` VALUES (1,'Quản lý nhân viên & Tài khoản'),(2,'Quản lý khách hàng'),(3,'Quản lý sản phẩm'),(4,'Quản lý nhà cung cấp'),(5,'Quản lý bán hàng'),(6,'Quản lý nhập hàng'),(7,'Quản lý thể loại'),(8,'Quản lý khuyến mãi'),(9,'Quản lý chức vụ'),(10,'Thống kê'),(11,'Quản lý nhân sự');
/*!40000 ALTER TABLE `module` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `payroll_history`
--

LOCK TABLES `payroll_history` WRITE;
/*!40000 ALTER TABLE `payroll_history` DISABLE KEYS */;
INSERT INTO `payroll_history` VALUES (1,1,'2026-02-01',500000000000.00,24,1.00,0.00,0.00,0.00,0.00,0.00,NULL,3000000.00,0.00,20835833333.33,NULL,5197458333.33,15638875000.00,'2026-02-26 01:28:10');
/*!40000 ALTER TABLE `payroll_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
INSERT INTO `permission` VALUES (1,'Xem danh sách nhân viên','EMPLOYEE_LIST_VIEW',1),(2,'Xem hồ sơ cá nhân','EMPLOYEE_PERSONAL_VIEW',1),(3,'Cập nhật hồ sơ cá nhân','EMPLOYEE_PERSONAL_UPDATE',1),(4,'Xem vị trí công tác & lịch sử','EMPLOYEE_JOB_VIEW',1),(5,'Cập nhật vị trí công tác','EMPLOYEE_JOB_UPDATE',1),(6,'Xem lương & bảo hiểm','EMPLOYEE_PAYROLLINFO_VIEW',1),(7,'Cập nhật lương & bảo hiểm','EMPLOYEE_PAYROLLINFO_UPDATE',1),(8,'Xem tài khoản hệ thống','EMPLOYEE_ACCOUNT_VIEW',1),(9,'Đặt lại mật khẩu nhân viên','EMPLOYEE_ACCOUNT_RESET_PASSWORD',1),(10,'Cập nhật trạng thái tài khoản','EMPLOYEE_ACCOUNT_UPDATE_STATUS',1),(11,'Thêm mới nhân viên, tài khoản','EMPLOYEE_INSERT',1),(12,'Xóa nhân viên','EMPLOYEE_DELETE',1),(13,'Xem danh sách khách hàng','CUSTOMER_LIST_VIEW',2),(14,'Thêm khách hàng','CUSTOMER_INSERT',2),(15,'Cập nhật khách hàng','CUSTOMER_UPDATE',2),(16,'Xóa khách hàng','CUSTOMER_DELETE',2),(17,'Xem danh sách sản phẩm','PRODUCT_LIST_VIEW',3),(18,'Thêm sản phẩm','PRODUCT_INSERT',3),(19,'Cập nhật sản phẩm','PRODUCT_UPDATE',3),(20,'Xóa sản phẩm','PRODUCT_DELETE',3),(21,'Xem danh sách nhà cung cấp','SUPPLIER_LIST_VIEW',4),(22,'Thêm nhà cung cấp','SUPPLIER_INSERT',4),(23,'Cập nhật nhà cung cấp','SUPPLIER_UPDATE',4),(24,'Xóa nhà cung cấp','SUPPLIER_DELETE',4),(25,'Xem danh sách đơn hàng','INVOICE_LIST_VIEW',5),(26,'Tạo đơn hàng mới','INVOICE_INSERT',5),(27,'Xem phiếu nhập hàng','IMPORT_LIST_VIEW',6),(28,'Tạo phiếu nhập hàng mới','IMPORT_INSERT',6),(29,'Xem phiếu nhập hàng','IMPORT_APPROVE',6),(30,'Xem danh mục sản phẩm','CATEGORY_LIST_VIEW',7),(31,'Thêm danh mục sản phẩm','CATEGORY_INSERT',7),(32,'Cập nhật danh mục sản phẩm','CATEGORY_UPDATE',7),(33,'Xóa danh mục sản phẩm','CATEGORY_DELETE',7),(34,'Xem chương trình khuyến mãi','DISCOUNT_LIST_VIEW',8),(35,'Thêm chương trình khuyến mãi','DISCOUNT_INSERT',8),(36,'Cập nhật chương trình khuyến mãi','DISCOUNT_UPDATE',8),(37,'Xóa chương trình khuyến mãi','DISCOUNT_DELETE',8),(38,'Xem danh sách chức vụ','ROLE_LIST_VIEW',9),(39,'Thêm chức vụ mới','ROLE_INSERT',9),(40,'Cập nhật chức vụ','ROLE_UPDATE',9),(41,'Xóa chức vụ','ROLE_DELETE',9),(42,'Xem bảng phân quyền','PERMISSION_VIEW',9),(43,'Cập nhật cấu hình phân quyền','PERMISSION_UPDATE',9),(44,'Xem báo cáo thống kê','STATISTICS_VIEW',10),(45,'Xem đơn nghỉ phép','EMPLOYEE_LEAVE_REQUEST_VIEW',1),(46,'Tạo đơn nghỉ phép','EMPLOYEE_LEAVE_REQUEST_CREATE',1),(47,'Quản lý duyệt đơn nghỉ','EMPLOYEE_LEAVE_REQUEST_MANAGE',1),(48,'Xem kỷ luật & khen thưởng','EMPLOYEE_FINE_REWARD_VIEW',1),(49,'Quản lý kỷ luật & khen thưởng','EMPLOYEE_FINE_REWARD_MANAGE',1),(50,'Xem chấm công','EMPLOYEE_ATTENDANCE_VIEW',1),(51,'Quản lý chấm công','EMPLOYEE_ATTENDANCE_MANAGE',1),(52,'Cập nhật chức vụ thanh viên','EMPLOYEE_ROLE_POSITION_UPDATE',1);
/*!40000 ALTER TABLE `permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `position`
--

LOCK TABLES `position` WRITE;
/*!40000 ALTER TABLE `position` DISABLE KEYS */;
INSERT INTO `position` VALUES (1,'Giám đốc',500000000000.00,'2026-02-26 00:10:27','2026-02-26 00:59:04');
/*!40000 ALTER TABLE `position` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES ('SP00001','Naruto - 01',0,0.00,0.00,6,'Minifigure nhân vật Naruto.','images/product/sp00001.png',2,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00002','Naruto - 02',0,0.00,0.00,6,'Minifigure Naruto trong trạng thái chiến đấu.','images/product/sp00002.png',2,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00003','Sasuke Uchiha',0,0.00,0.00,6,'Minifigure nhân vật Sasuke Uchiha từ series Naruto.',NULL,2,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00004','Kakashi Hatake',0,0.00,0.00,6,'Minifigure nhân vật Kakashi với Sharingan.',NULL,2,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00005','Sakura Haruno',0,0.00,0.00,6,'Minifigure nhân vật Sakura từ series Naruto.',NULL,2,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00007','Darth Vader',0,0.00,0.00,6,'Minifigure Darth Vader với lightsaber đỏ và mặt nạ.',NULL,2,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00008','Iron Man Mark 85',0,0.00,0.00,6,'Minifigure Iron Man trong bộ giáp Mark 85 từ Avengers: Endgame.',NULL,2,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00010','Harry Potter',0,0.00,0.00,6,'Minifigure Harry Potter với đũa phép và kính tròn.',NULL,2,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00011','LEGO Technic Bugatti Chiron',0,0.00,0.00,6,'Mô hình kỹ thuật cao của siêu xe Bugatti Chiron.',NULL,3,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00014','LEGO Technic Race Car',0,0.00,0.00,6,'Xe đua công thức 1 với động cơ pistons hoạt động.',NULL,3,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00019','LEGO Architecture Empire State Building',0,0.00,0.00,6,'Mô hình chi tiết của tòa nhà Empire State.',NULL,4,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00030','MOC - Tháp Rùa Hồ Gươm',0,0.00,0.00,6,'Mô hình Tháp Rùa trên Hồ Gươm, 1250 chi tiết.',NULL,6,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00031','LEGO City Police Station',0,0.00,0.00,6,'Trụ sở cảnh sát thành phố.',NULL,6,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00036','LEGO Star Wars Millennium Falcon',0,0.00,0.00,6,'Tàu Millennium Falcon với nhiều nhân vật.',NULL,4,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00038','LEGO Star Wars AT-AT',0,0.00,0.00,6,'Walker AT-AT từ phim The Empire Strikes Back.',NULL,4,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00039','LEGO Star Wars Death Star',0,0.00,0.00,6,'Ngôi sao tử thần Death Star.',NULL,4,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00040','LEGO Star Wars X-Wing Starfighter',0,0.00,0.00,6,'Tàu chiến X-Wing của Luke Skywalker.',NULL,4,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00042','LEGO Marvel Sanctum Sanctorum',0,0.00,0.00,6,'Sanctum Sanctorum của Doctor Strange.',NULL,3,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00043','LEGO Marvel Guardians Ship',0,0.00,0.00,6,'Tàu của đội Guardians of the Galaxy.',NULL,3,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00044','LEGO Marvel Spider-Man Daily Bugle',0,0.00,0.00,6,'Tòa nhà Daily Bugle với nhiều nhân vật Spider-Man.',NULL,3,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00048','LEGO Harry Potter Hogwarts Express',0,0.00,0.00,6,'Tàu Hogwarts Express với sân ga 9¾.',NULL,1,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00049','LEGO Harry Potter Chamber of Secrets',0,0.00,0.00,6,'Phòng chứa bí mật với rắn Basilisk.',NULL,1,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00051','LEGO Creator Expert Bookshop',0,0.00,0.00,6,'Hiệu sách chi tiết với căn hộ ở trên.',NULL,2,'2026-02-24 22:00:04','2026-02-24 22:00:04'),('SP00052','LEGO Creator Expert Assembly Square',0,0.00,0.00,6,'Quảng trường trung tâm với nhiều tòa nhà.',NULL,4,'2026-02-24 22:00:04','2026-02-24 22:00:04');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `profit_stat`
--

LOCK TABLES `profit_stat` WRITE;
/*!40000 ALTER TABLE `profit_stat` DISABLE KEYS */;
/*!40000 ALTER TABLE `profit_stat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `report`
--

LOCK TABLES `report` WRITE;
/*!40000 ALTER TABLE `report` DISABLE KEYS */;
/*!40000 ALTER TABLE `report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` (`id`, `name`, `description`, `start_experience`, `end_experience`, `created_at`, `updated_at`) VALUES 
(1,'IT Admin','Quản trị hệ thống toàn quyền',0,0,'2026-02-24 22:00:03','2026-02-24 22:00:03'),
(2,'Tổng giám đốc','Chủ cửa hàng/Điều hành cao cấp',10,30,'2026-02-24 22:00:03','2026-02-24 22:00:03'),
(3,'Quản lý cửa hàng','Điều hành toàn diện hoạt động cửa hàng',4,15,'2026-02-24 22:00:03','2026-02-24 22:00:03'),
(4,'Trưởng nhóm bán hàng','Giám sát ca làm việc và hỗ trợ thanh toán phức tạp',2,4,'2026-02-24 22:00:03','2026-02-24 22:00:03'),
(5,'Nhân viên bán hàng (Bậc 3)','Nhân viên nòng cốt, hỗ trợ đào tạo người mới',3,5,'2026-02-24 22:00:03','2026-02-24 22:00:03'),
(6,'Nhân viên bán hàng (Bậc 2)','Nhân viên kinh nghiệm, tư vấn chuyên sâu',1,3,'2026-02-24 22:00:03','2026-02-24 22:00:03'),
(7,'Nhân viên bán hàng (Bậc 1)','Nhân viên mới',0,1,'2026-02-24 22:00:03','2026-02-24 22:00:03'),
(8,'Nhân viên kho (Bậc 2)','Quản lý nhập xuất kho',2,5,'2026-02-24 22:00:03','2026-02-24 22:00:03'),
(9,'Nhân viên kho (Bậc 1)','Sắp xếp và kiểm kê kho',0,2,'2026-02-24 22:00:03','2026-02-24 22:00:03');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `role_permission`
--

LOCK TABLES `role_permission` WRITE;
/*!40000 ALTER TABLE `role_permission` DISABLE KEYS */;
INSERT INTO `role_permission` VALUES (1,1),(2,1),(3,1),(1,2),(2,2),(3,2),(1,3),(2,3),(1,4),(2,4),(3,4),(1,5),(2,5),(1,6),(2,6),(1,7),(2,7),(1,8),(2,8),(1,9),(2,9),(1,10),(2,10),(1,11),(2,11),(1,12),(2,12),(1,13),(2,13),(3,13),(4,13),(5,13),(6,13),(7,13),(1,14),(2,14),(1,15),(2,15),(1,16),(2,16),(1,17),(2,17),(3,17),(4,17),(5,17),(6,17),(7,17),(8,17),(9,17),(1,18),(2,18),(1,19),(2,19),(3,19),(8,19),(9,19),(1,20),(2,20),(1,21),(2,21),(1,22),(2,22),(1,23),(2,23),(1,24),(2,24),(1,25),(2,25),(3,25),(1,26),(2,26),(4,26),(5,26),(6,26),(7,26),(1,27),(2,27),(8,27),(9,27),(1,28),(2,28),(8,28),(9,28),(1,29),(2,29),(1,30),(2,30),(1,31),(2,31),(1,32),(2,32),(1,33),(2,33),(1,34),(2,34),(1,35),(2,35),(1,36),(2,36),(1,37),(2,37),(1,38),(2,38),(1,39),(2,39),(1,40),(2,40),(1,41),(2,41),(1,42),(2,42),(1,43),(2,43),(1,44),(2,44),(3,44),(1,45),(2,45),(3,45),(4,45),(5,45),(6,45),(7,45),(8,45),(9,45),(1,46),(2,46),(5,46),(6,46),(7,46),(8,46),(9,46),(1,47),(2,47),(3,47),(1,48),(2,48),(3,48),(4,48),(1,49),(2,49),(3,49),(1,50),(2,50),(3,50),(4,50),(5,50),(6,50),(7,50),(8,50),(9,50),(1,51),(2,51),(3,51),(1,52),(2,52),(3,52);
/*!40000 ALTER TABLE `role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `status`
--

LOCK TABLES `status` WRITE;
/*!40000 ALTER TABLE `status` DISABLE KEYS */;
INSERT INTO `status` VALUES (1,'Active','Đang làm việc','EMPLOYEE'),(2,'Inactive','Đã nghỉ việc','EMPLOYEE'),(3,'On_Leave','Đang nghỉ phép','EMPLOYEE'),(4,'Active','Được phép đăng nhập','ACCOUNT'),(5,'Locked','Bị khóa (do sai pass/vi phạm)','ACCOUNT'),(6,'Active','Đang kinh doanh','PRODUCT'),(7,'Suspended','Ngừng kinh doanh','PRODUCT'),(8,'Inactive','Vô hiệu','PRODUCT'),(9,'Active','Hoạt động','CATEGORY'),(10,'Inactive','Vô hiệu','CATEGORY'),(11,'Active','Hoạt động','SUPPLIER'),(12,'Inactive','Vô hiệu','SUPPLIER'),(13,'Active','Hoạt động','CUSTOMER'),(14,'Inactive','Ngưng tương tác','CUSTOMER'),(15,'Completed','Hoàn thành','INVOICE'),(16,'Canceled','Hủy bỏ','INVOICE'),(17,'Completed','Hoàn thành','IMPORT'),(18,'Incompleted','Chưa hoàn thành','IMPORT'),(19,'Draft','Nháp','IMPORT'),(20,'Pending','Đơn đang chờ quản lý phê duyệt','LEAVE_REQUEST'),(21,'Approved','Đơn đã được chấp thuận','LEAVE_REQUEST'),(22,'Rejected','Đơn bị từ chối','LEAVE_REQUEST'),(23,'Canceled','Đơn đã bị hủy bởi nhân viên','LEAVE_REQUEST'),(24,'Pending','Quyết định đang chờ cấp trên phê duyệt','EMPLOYMENT_HISTORY'),(25,'Approved','Quyết định đã được duyệt, chờ ngày có hiệu lực','EMPLOYMENT_HISTORY'),(26,'Effective','Quyết định đã chính thức đi vào hiệu lực','EMPLOYMENT_HISTORY'),(27,'Rejected','Quyết định bị cấp trên từ chối','EMPLOYMENT_HISTORY'),(28,'Canceled','Quyết định đã bị hủy bỏ trước khi thực hiện','EMPLOYMENT_HISTORY'),(29,'Active','Hoạt động','DEPARTMENT'),(30,'Inactive','Vô hiệu','DEPARTMENT');
/*!40000 ALTER TABLE `status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `supplier`
--

LOCK TABLES `supplier` WRITE;
/*!40000 ALTER TABLE `supplier` DISABLE KEYS */;
INSERT INTO `supplier` VALUES (1,'Nhà cung cấp A','0903344554','99 An Dương Vương, Phường 16, Quận 8, TP Hồ Chí Minh','supplierA@example.com',11),(2,'Nhà cung cấp B','0903344556','04 Tôn Đức Thắng, Phường Bến Nghé, Quận 1, TP Hồ Chí Minh','supplierB@example.com',11),(3,'Nhà cung cấp C','0903344557','123 Nguyễn Thị Minh Khai, Quận 3, TP Hồ Chí Minh','supplierC@example.com',11),(4,'Nhà cung cấp D','0903344558','456 Lê Lợi, Quận 1, TP Hồ Chí Minh','supplierD@example.com',11),(5,'Nhà cung cấp E','0903344559','789 Trường Chinh, Quận Tân Bình, TP Hồ Chí Minh','supplierE@example.com',11),(6,'Nhà cung cấp F','0903344560','101 Nguyễn Văn Cừ, Quận 5, TP Hồ Chí Minh','supplierF@example.com',11),(7,'Nhà cung cấp G','0903344561','202 Phan Văn Trị, Quận Bình Thạnh, TP Hồ Chí Minh','supplierG@example.com',11),(8,'Nhà cung cấp H','0903344562','303 Nguyễn Huệ, Quận 1, TP Hồ Chí Minh','supplierH@example.com',11),(9,'Nhà cung cấp I','0903344563','404 Lê Văn Sỹ, Quận 3, TP Hồ Chí Minh','supplierI@example.com',11),(10,'Nhà cung cấp J','0903344564','505 Bến Vân Đồn, Quận 4, TP Hồ Chí Minh','supplierJ@example.com',11),(11,'Nhà cung cấp K','0903344565','606 Đinh Tiên Hoàng, Quận Bình Thạnh, TP Hồ Chí Minh','supplierK@example.com',11),(12,'Nhà cung cấp L','0903344566','707 Trần Hưng Đạo, Quận 1, TP Hồ Chí Minh','supplierL@example.com',11),(13,'Nhà cung cấp M','0903344567','808 Hoàng Văn Thụ, Quận Tân Bình, TP Hồ Chí Minh','supplierM@example.com',11),(14,'Nhà cung cấp N','0903344568','909 Nguyễn Thái Sơn, Quận Gò Vấp, TP Hồ Chí Minh','supplierN@example.com',11),(15,'Nhà cung cấp O','0903344569','1001 Lạc Long Quân, Quận 11, TP Hồ Chí Minh','supplierO@example.com',11),(16,'Nhà cung cấp P','0903344570','1102 Âu Cơ, Quận Tân Phú, TP Hồ Chí Minh','supplierP@example.com',11),(17,'Nhà cung cấp Q','0903344571','1203 Trần Quốc Toản, Quận 3, TP Hồ Chí Minh','supplierQ@example.com',11),(18,'Nhà cung cấp R','0903344572','1304 Ngô Quyền, Quận 10, TP Hồ Chí Minh','supplierR@example.com',11),(19,'Nhà cung cấp S','0903344573','1405 Đinh Bộ Lĩnh, Quận Bình Thạnh, TP Hồ Chí Minh','supplierS@example.com',11),(20,'Nhà cung cấp T','0903344574','1506 Huỳnh Tấn Phát, Quận 7, TP Hồ Chí Minh','supplierT@example.com',11);
/*!40000 ALTER TABLE `supplier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `time_sheet`
--

LOCK TABLES `time_sheet` WRITE;
/*!40000 ALTER TABLE `time_sheet` DISABLE KEYS */;
INSERT INTO `time_sheet` VALUES (1,1,'2026-02-01 08:00:00','2026-02-01 17:00:00',8.00,0.00),(2,2,'2026-02-01 08:00:00','2026-02-01 17:00:00',8.00,0.00),(3,3,'2026-02-01 08:00:00','2026-02-01 17:00:00',8.00,0.00),(4,4,'2026-02-01 08:00:00','2026-02-01 17:00:00',8.00,0.00),(5,5,'2026-02-01 08:00:00','2026-02-01 17:00:00',8.00,0.00),(6,7,'2026-02-01 08:00:00','2026-02-01 17:00:00',8.00,0.00),(7,8,'2026-02-01 08:00:00','2026-02-01 17:00:00',8.00,0.00),(8,9,'2026-02-01 08:00:00','2026-02-01 17:00:00',8.00,0.00),(9,10,'2026-02-01 08:00:00','2026-02-01 17:00:00',8.00,0.00),(10,11,'2026-02-01 08:00:00','2026-02-01 17:00:00',8.00,0.00),(11,12,'2026-02-01 08:00:00','2026-02-01 17:00:00',8.00,0.00),(12,13,'2026-02-01 08:00:00','2026-02-01 17:00:00',8.00,0.00),(13,6,'2026-02-01 08:00:00','2026-02-01 17:00:00',8.00,0.00),(16,2,'2026-02-02 08:00:00','2026-02-02 19:00:00',10.00,2.00),(17,3,'2026-02-02 08:00:00','2026-02-02 20:00:00',11.00,3.00);
/*!40000 ALTER TABLE `time_sheet` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-26  1:32:11
