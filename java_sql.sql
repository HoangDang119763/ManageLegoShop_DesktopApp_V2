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
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_bin NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_bin NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `last_login` datetime DEFAULT NULL,
  `status_id` int NOT NULL,
  `require_relogin` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `fk_account_status` (`status_id`),
  CONSTRAINT `fk_account_employee` FOREIGN KEY (`id`) REFERENCES `employee` (`id`),
  CONSTRAINT `fk_account_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,'admin','$2a$12$QIBl3fm0aG.SDhGTldUk5eTFgClKWp1HjNP06Er4utLo/kG1dNpCG','2026-02-15 23:54:40','2026-02-24 17:07:46',4,0),(2,'huyhoang119763','$2a$12$ipuwsQs46H2VAcT1hwS/kuCpv.MXEvJ2IlcPWTyss6Gsm5hpsHWmy','2026-02-15 23:54:40',NULL,4,0),(3,'vuithii','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-15 23:54:40',NULL,4,0),(4,'lyvan','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-15 23:54:40',NULL,4,0),(5,'nguyenthanh','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-15 23:54:40',NULL,4,0),(6,'trinhvan','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-15 23:54:40',NULL,4,0),(7,'tanthien','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-15 23:54:40',NULL,4,0),(8,'lethib','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-15 23:54:40',NULL,4,0),(9,'phamminh','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-15 23:54:40',NULL,4,0),(10,'nguyenthi','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-15 23:54:40',NULL,4,0),(11,'ngominh','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-15 23:54:40',NULL,4,0),(12,'buithiph','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-15 23:54:40',NULL,4,0),(13,'dovan','$2a$12$qRb3nf6c.jQkpzhp7wvHnOSYofcIH2CZlu00ohT/UR61doxanfyua','2026-02-15 23:54:40',NULL,4,0);
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `allowance`
--

DROP TABLE IF EXISTS `allowance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `allowance` (
  `id` int NOT NULL AUTO_INCREMENT,
  `employee_id` int NOT NULL,
  `salary_period` date NOT NULL,
  `attendance_bonus` decimal(15,2) DEFAULT '0.00',
  `annual_leave_days` decimal(5,1) DEFAULT '0.0',
  `transportation_support` decimal(15,2) DEFAULT '0.00',
  `accommodation_support` decimal(15,2) DEFAULT '0.00',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_allowance_employee` (`employee_id`),
  CONSTRAINT `fk_allowance_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `allowance`
--

LOCK TABLES `allowance` WRITE;
/*!40000 ALTER TABLE `allowance` DISABLE KEYS */;
INSERT INTO `allowance` VALUES (1,1,'2026-02-01',0.00,0.0,500000.00,1000000.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(2,2,'2026-02-01',0.00,0.0,500000.00,1000000.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(3,3,'2026-02-01',0.00,0.0,500000.00,1000000.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(4,4,'2026-02-01',0.00,0.0,0.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(5,5,'2026-02-01',0.00,0.0,0.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(6,6,'2026-02-01',0.00,0.0,0.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(7,7,'2026-02-01',0.00,0.0,500000.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(8,8,'2026-02-01',0.00,0.0,0.00,1000000.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(9,9,'2026-02-01',0.00,0.0,500000.00,1000000.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(10,10,'2026-02-01',0.00,0.0,0.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(11,11,'2026-02-01',0.00,0.0,500000.00,1000000.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(12,12,'2026-02-01',0.00,0.0,500000.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(13,13,'2026-02-01',0.00,0.0,0.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41');
/*!40000 ALTER TABLE `allowance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_category_status` (`status_id`),
  CONSTRAINT `fk_category_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'Chưa xác định','2026-02-15 23:54:40','2026-02-15 23:54:41',9),(2,'Minifigure','2026-02-15 23:54:40','2026-02-15 23:54:41',9),(3,'Technic','2026-02-15 23:54:40','2026-02-15 23:54:41',9),(4,'Architecture','2026-02-15 23:54:40','2026-02-15 23:54:41',9),(5,'Classic','2026-02-15 23:54:40','2026-02-15 23:54:41',9),(6,'Moc','2026-02-15 23:54:40','2026-02-15 23:54:41',9);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `id` int NOT NULL AUTO_INCREMENT,
  `first_name` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `last_name` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `phone` varchar(15) COLLATE utf8mb4_bin NOT NULL,
  `address` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_customer_status` (`status_id`),
  CONSTRAINT `fk_customer_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (1,'Vãng','Lai',NULL,'0000000000','','2026-02-15 16:54:41',13),(2,'Nguyễn','Thành','1990-02-15','0123456789','123 Đường Lê Lợi, Quận 1, Hồ Chí Minh','2026-02-15 16:54:41',13),(3,'Trần','Minh','1985-04-20','0987654321','456 Đường Nguyễn Huệ, Quận 1, Hồ Chí Minh','2026-02-15 16:54:41',13),(4,'Lê','Hằng','1995-08-30','0912345678','789 Đường Trần Hưng Đạo, Quận 5, Hồ Chí Minh','2026-02-15 16:54:41',13),(5,'Phạm','Hải','1988-12-01','0934567890','321 Đường Bùi Viện, Quận 1, Hồ Chí Minh','2026-02-15 16:54:41',13),(6,'Đỗ','Lan','1992-05-16','0345678901','654 Đường Lê Văn Sĩ, Quận 3, Hồ Chí Minh','2026-02-15 16:54:41',13),(7,'Nguyễn','Văn','1993-11-11','0123456780','987 Đường Nguyễn Văn Cừ, Quận 5, Hồ Chí Minh','2026-02-15 16:54:41',13),(8,'Trần','Kiên','1994-03-23','0912345679','234 Đường Trần Quốc Thảo, Quận 3, Hồ Chí Minh','2026-02-15 16:54:41',13),(9,'Lê','Phú','1991-07-07','0987654320','567 Đường Phạm Ngọc Thạch, Quận 1, Hồ Chí Minh','2026-02-15 16:54:41',13),(10,'Ngô','Thảo','1996-09-09','0356789012','890 Đường Võ Văn Tần, Quận 3, Hồ Chí Minh','2026-02-15 16:54:41',13),(11,'Bùi','Bích','1987-01-20','0123456781','135 Đường Hàn Hải Nguyên, Quận 4, Hồ Chí Minh','2026-02-15 16:54:41',13),(12,'Mai','An','1999-06-18','0987654322','246 Đường Cách Mạng Tháng 8, Quận 10, Hồ Chí Minh','2026-02-15 16:54:41',13),(13,'Vũ','Khoa','1992-10-10','0345678902','357 Đường Nguyễn Trãi, Quận 5, Hồ Chí Minh','2026-02-15 16:54:40',13),(14,'Hà','Trang','1989-05-21','0934567891','468 Đường Lê Quý Đôn, Quận 3, Hồ Chí Minh','2026-02-15 16:54:41',13),(15,'Phan','Nhi','1995-12-30','0123456782','579 Đường Nguyễn Thị Minh Khai, Quận 1, Hồ Chí Minh','2026-02-15 16:54:41',13),(16,'Nguyễn','Lộc','1994-04-14','0987654323','680 Đường Nam Kỳ Khởi Nghĩa, Quận 3, Hồ Chí Minh','2026-02-15 16:54:40',13),(17,'Lê','Quân','1986-08-08','0356789013','791 Đường Điện Biên Phủ, Quận 1, Hồ Chí Minh','2026-02-15 16:54:41',13),(18,'Trương','Duy','1993-11-02','0123456783','902 Đường Nguyễn Đình Chiểu, Quận 3, Hồ Chí Minh','2026-02-15 16:54:41',13),(19,'Ngô','Việt','1988-07-19','0912345680','113 Đường Phan Đăng Lưu, Quận Bình Thạnh, Hồ Chí Minh','2026-02-15 16:54:41',13),(20,'Đỗ','Hòa','1991-09-29','0987654324','224 Đường Huỳnh Văn Bánh, Quận Phú Nhuận, Hồ Chí Minh','2026-02-15 16:54:41',13),(21,'Nguyễn','Phúc','1992-04-05','0345678903','456 Đường Nguyễn Thái Bình, Quận 1, Hồ Chí Minh','2026-02-15 16:54:41',13),(22,'Lê','Hưng','1989-12-12','0912345670','567 Đường Lê Văn Sĩ, Quận 3, Hồ Chí Minh','2026-02-15 16:54:40',13),(23,'Đỗ','Nghĩa','1995-05-25','0987654325','678 Đường Phạm Hồng Thái, Quận 10, Hồ Chí Minh','2026-02-15 16:54:41',13),(24,'Trần','Tú','1994-07-30','0356789014','789 Đường Trần Bình Trọng, Quận 5, Hồ Chí Minh','2026-02-15 16:54:41',13),(25,'Lê','Đức','1991-01-01','0123456785','890 Đường Lê Thánh Tôn, Quận 1, Hồ Chí Minh','2026-02-15 16:54:41',13),(26,'Nguyễn','Giang','1993-03-03','0987654326','901 Đường Nguyễn Đình Chiểu, Quận 3, Hồ Chí Minh','2026-02-15 16:54:41',13),(27,'Trần','Thành','1987-08-08','0345678904','123 Đường Trần Hưng Đạo, Quận 5, Hồ Chí Minh','2026-02-15 16:54:41',13),(28,'Mai','Hương','1996-09-09','0912345681','234 Đường Cách Mạng Tháng 8, Quận 10, Hồ Chí Minh','2026-02-15 16:54:41',13);
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `deduction`
--

DROP TABLE IF EXISTS `deduction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `deduction` (
  `id` int NOT NULL AUTO_INCREMENT,
  `employee_id` int NOT NULL,
  `salary_period` date NOT NULL,
  `health_insurance` decimal(15,2) DEFAULT '0.00',
  `social_insurance` decimal(15,2) DEFAULT '0.00',
  `unemployment_insurance` decimal(15,2) DEFAULT '0.00',
  `personal_income_tax` decimal(15,2) DEFAULT '0.00',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_deduction_employee` (`employee_id`),
  CONSTRAINT `fk_deduction_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `deduction`
--

LOCK TABLES `deduction` WRITE;
/*!40000 ALTER TABLE `deduction` DISABLE KEYS */;
INSERT INTO `deduction` VALUES (1,1,'2026-02-01',0.00,800000.00,100000.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(2,2,'2026-02-01',0.00,800000.00,100000.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(3,3,'2026-02-01',0.00,800000.00,100000.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(4,4,'2026-02-01',0.00,800000.00,100000.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(5,5,'2026-02-01',0.00,800000.00,100000.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(6,6,'2026-02-01',0.00,800000.00,100000.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(7,7,'2026-02-01',0.00,800000.00,100000.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(8,8,'2026-02-01',0.00,0.00,100000.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(9,9,'2026-02-01',0.00,800000.00,0.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(10,10,'2026-02-01',0.00,0.00,0.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(11,11,'2026-02-01',0.00,800000.00,100000.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(12,12,'2026-02-01',0.00,800000.00,100000.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41'),(13,13,'2026-02-01',0.00,800000.00,100000.00,0.00,'2026-02-15 23:54:41','2026-02-15 23:54:41');
/*!40000 ALTER TABLE `deduction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `department`
--

DROP TABLE IF EXISTS `department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `department` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_bin NOT NULL,
  `description` text COLLATE utf8mb4_bin,
  `status_id` int NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_dept_status` (`status_id`),
  CONSTRAINT `fk_dept_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `department`
--

LOCK TABLES `department` WRITE;
/*!40000 ALTER TABLE `department` DISABLE KEYS */;
INSERT INTO `department` VALUES (1,'Phòng Hội đồng quản trị','Ban lãnh đạo cấp cao',28,'2026-02-15 23:54:40','2026-02-15 23:54:41'),(2,'Phòng Nhân sự','Quản lý tuyển dụng và đào tạo',28,'2026-02-15 23:54:40','2026-02-15 23:54:41'),(3,'Phòng Kinh doanh','Tiếp thị và bán lẻ sản phẩm LEGO',28,'2026-02-15 23:54:40','2026-02-15 23:54:41'),(4,'Phòng Kho vận','Quản lý nhập xuất hàng hóa',28,'2026-02-15 23:54:40','2026-02-15 23:54:41'),(5,'Phòng Kỹ thuật','Bảo trì hệ thống và hỗ trợ',28,'2026-02-15 23:54:40','2026-02-15 23:54:41');
/*!40000 ALTER TABLE `department` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detail_discount`
--

DROP TABLE IF EXISTS `detail_discount`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detail_discount` (
  `discount_code` varchar(50) COLLATE utf8mb4_bin NOT NULL,
  `total_price_invoice` decimal(12,2) NOT NULL,
  `discount_amount` decimal(10,2) NOT NULL,
  PRIMARY KEY (`discount_code`,`total_price_invoice`),
  CONSTRAINT `fk_discount_code` FOREIGN KEY (`discount_code`) REFERENCES `discount` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detail_discount`
--

LOCK TABLES `detail_discount` WRITE;
/*!40000 ALTER TABLE `detail_discount` DISABLE KEYS */;
INSERT INTO `detail_discount` VALUES ('30T4',100000.00,5.00),('30T4',200000.00,7.00),('30T4',300000.00,9.00),('CODE01',50000.00,5.00),('CODE01',100000.00,7.00),('CODE02',30000.00,2000.00),('CODE02',60000.00,5000.00);
/*!40000 ALTER TABLE `detail_discount` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detail_import`
--

DROP TABLE IF EXISTS `detail_import`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detail_import` (
  `import_id` int NOT NULL,
  `product_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `quantity` int NOT NULL,
  `profit_percent` decimal(10,2) NOT NULL DEFAULT '1.00',
  `price` decimal(10,2) NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `is_pushed` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`import_id`,`product_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `detail_import_ibfk_1` FOREIGN KEY (`import_id`) REFERENCES `import` (`id`),
  CONSTRAINT `detail_import_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detail_import`
--

LOCK TABLES `detail_import` WRITE;
/*!40000 ALTER TABLE `detail_import` DISABLE KEYS */;
INSERT INTO `detail_import` VALUES (1,'SP00001',20,1.00,20000.00,400000.00,1),(1,'SP00002',20,1.00,18000.00,360000.00,1),(1,'SP00004',20,1.00,15000.00,300000.00,1),(1,'SP00005',20,1.00,25000.00,500000.00,1),(1,'SP00007',20,1.00,40000.00,800000.00,1),(1,'SP00008',20,1.00,30000.00,600000.00,1),(1,'SP00010',20,1.00,100000.00,2000000.00,1),(1,'SP00011',20,1.00,30000.00,600000.00,1),(1,'SP00014',20,1.00,45000.00,900000.00,1),(2,'SP00001',20,1.00,22000.00,440000.00,0),(2,'SP00002',20,1.00,20000.00,400000.00,0);
/*!40000 ALTER TABLE `detail_import` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detail_invoice`
--

DROP TABLE IF EXISTS `detail_invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detail_invoice` (
  `invoice_id` int NOT NULL,
  `product_id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `price` decimal(10,2) NOT NULL,
  `cost_price` decimal(10,2) NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`invoice_id`,`product_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `detail_invoice_ibfk_1` FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`id`),
  CONSTRAINT `detail_invoice_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detail_invoice`
--

LOCK TABLES `detail_invoice` WRITE;
/*!40000 ALTER TABLE `detail_invoice` DISABLE KEYS */;
INSERT INTO `detail_invoice` VALUES (1,'SP00001',2,21000.00,20000.00,60000.00),(1,'SP00002',3,18900.00,18000.00,82500.00),(2,'SP00004',1,15750.00,15000.00,15750.00),(2,'SP00007',1,42000.00,40000.00,42000.00),(2,'SP00010',1,105000.00,100000.00,105000.00);
/*!40000 ALTER TABLE `detail_invoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `discount`
--

DROP TABLE IF EXISTS `discount`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `discount` (
  `code` varchar(50) COLLATE utf8mb4_bin NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `type` tinyint(1) NOT NULL DEFAULT '0',
  `startDate` date NOT NULL,
  `endDate` date NOT NULL,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `discount`
--

LOCK TABLES `discount` WRITE;
/*!40000 ALTER TABLE `discount` DISABLE KEYS */;
INSERT INTO `discount` VALUES ('30T4','30 Tháng 4',0,'2025-04-30','2025-06-11'),('CODE01','Khuyến mãi mùa hạ',0,'2024-02-05','2025-03-31'),('CODE02','Khuyến mãi mùa hè',1,'2024-03-22','2025-03-31');
/*!40000 ALTER TABLE `discount` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employee`
--

DROP TABLE IF EXISTS `employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee` (
  `id` int NOT NULL AUTO_INCREMENT,
  `first_name` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `last_name` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `phone` varchar(15) COLLATE utf8mb4_bin NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `gender` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `role_id` int DEFAULT NULL,
  `department_id` int DEFAULT NULL,
  `status_id` int NOT NULL,
  `account_id` int DEFAULT NULL,
  `health_ins_code` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  `social_insurance_code` varchar(50) COLLATE utf8mb4_bin DEFAULT '0',
  `unemployment_insurance_code` varchar(50) COLLATE utf8mb4_bin DEFAULT '0',
  `is_personal_income_tax` tinyint(1) DEFAULT '0',
  `is_transportation_support` tinyint(1) DEFAULT '0',
  `is_accommodation_support` tinyint(1) DEFAULT '0',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_employee_role` (`role_id`),
  KEY `idx_employee_department` (`department_id`),
  KEY `idx_employee_account` (`account_id`),
  KEY `fk_employee_status` (`status_id`),
  CONSTRAINT `fk_employee_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_employee_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_employee_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee`
--

LOCK TABLES `employee` WRITE;
/*!40000 ALTER TABLE `employee` DISABLE KEYS */;
INSERT INTO `employee` VALUES (1,'ID','ADMIN','','',NULL,'Nam',1,NULL,1,1,'HI-000000','1','1',1,1,1,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(2,'Đặng Huy','Hoàng','0123456789','hoang.dh@company.com','2004-06-11','Nam',2,1,1,2,'HI-2026001','1','1',1,1,1,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(3,'Vũ Thị','Iến','0900123456','ien.vt@company.com','1994-09-25','Nữ',2,1,1,3,'HI-2026011','1','1',1,1,1,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(4,'Lý Văn','Nam','0911234567','nam.lv@company.com','1996-10-30','Nam',2,1,1,4,'HI-2026012','1','1',1,0,0,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(5,'Nguyễn Thành','Long','0987654321','long.nt@company.com','2003-04-11','Nam',3,2,1,5,'HI-2026002','1','1',1,0,0,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(6,'Trịnh Văn','Hùng','0999012345','hung.tv@company.com','1989-08-20','Nam',3,2,1,6,'HI-2026010','1','1',1,0,0,'2026-02-15 23:54:41','2026-02-15 23:54:40'),(7,'Tần Thiên','Lang','0912345678','lang.tt@company.com','2000-01-15','Nam',4,3,1,7,'HI-2026003','1','1',0,1,0,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(8,'Lê Thị','Bích','0933456789','bich.lt@company.com','1988-02-20','Nữ',3,4,1,8,'HI-2026004','0','1',1,0,1,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(9,'Phạm Minh','Chính','0944567890','chinh.pm@company.com','1985-03-25','Nam',4,3,1,9,'HI-2026005','1','0',1,1,1,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(10,'Nguyễn Thị','Diệu','0955678901','dieu.nt@company.com','1992-04-30','Nữ',4,3,1,10,'HI-2026006','0','0',1,0,0,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(11,'Ngô Minh','Giàu','0988901234','giau.nm@company.com','1991-07-15','Nam',6,3,1,11,'HI-2026009','1','1',0,1,1,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(12,'Bùi Thị','Phượng','0977890123','phuong.bt@company.com','1993-06-10','Nữ',6,3,1,12,'HI-2026008','1','1',1,1,0,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(13,'Đỗ Văn','Em','0966789012','em.dv@company.com','1995-05-05','Nam',7,4,1,13,'HI-2026007','1','1',0,0,0,'2026-02-15 23:54:40','2026-02-15 23:54:40');
/*!40000 ALTER TABLE `employee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employment_history`
--

DROP TABLE IF EXISTS `employment_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employment_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `employee_id` int NOT NULL,
  `department_id` int NOT NULL,
  `role_id` int NOT NULL,
  `effective_date` date NOT NULL,
  `approver_id` int DEFAULT NULL,
  `status_id` int NOT NULL,
  `reason` text COLLATE utf8mb4_bin,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_eh_employee` (`employee_id`),
  KEY `fk_eh_dept` (`department_id`),
  KEY `fk_eh_role` (`role_id`),
  KEY `fk_eh_approver` (`approver_id`),
  KEY `fk_eh_status` (`status_id`),
  CONSTRAINT `fk_eh_approver` FOREIGN KEY (`approver_id`) REFERENCES `employee` (`id`),
  CONSTRAINT `fk_eh_dept` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
  CONSTRAINT `fk_eh_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_eh_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  CONSTRAINT `fk_eh_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employment_history`
--

LOCK TABLES `employment_history` WRITE;
/*!40000 ALTER TABLE `employment_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `employment_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file`
--

DROP TABLE IF EXISTS `file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `file` (
  `id` int NOT NULL AUTO_INCREMENT,
  `file_path` varchar(255) COLLATE utf8mb4_bin NOT NULL,
  `file_name` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file`
--

LOCK TABLES `file` WRITE;
/*!40000 ALTER TABLE `file` DISABLE KEYS */;
/*!40000 ALTER TABLE `file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fine`
--

DROP TABLE IF EXISTS `fine`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fine` (
  `id` int NOT NULL AUTO_INCREMENT,
  `reason` varchar(255) COLLATE utf8mb4_bin NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fine_level` varchar(50) COLLATE utf8mb4_bin NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `fine_pay` decimal(15,2) DEFAULT '0.00',
  `employee_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fine_ibfk_1` (`employee_id`),
  CONSTRAINT `fine_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fine`
--

LOCK TABLES `fine` WRITE;
/*!40000 ALTER TABLE `fine` DISABLE KEYS */;
INSERT INTO `fine` VALUES (1,'Làm vỡ bộ LEGO Ferrari','2026-02-15 23:54:40','LEVEL_3',500000.00,0.00,12),(2,'Đi muộn','2026-02-15 23:54:40','LEVEL_1',50000.00,0.00,8);
/*!40000 ALTER TABLE `fine` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `holiday`
--

DROP TABLE IF EXISTS `holiday`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `holiday` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_bin NOT NULL,
  `date` date NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `holiday`
--

LOCK TABLES `holiday` WRITE;
/*!40000 ALTER TABLE `holiday` DISABLE KEYS */;
/*!40000 ALTER TABLE `holiday` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `import`
--

DROP TABLE IF EXISTS `import`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `import` (
  `id` int NOT NULL AUTO_INCREMENT,
  `create_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `employee_id` int NOT NULL,
  `supplier_id` int NOT NULL,
  `total_price` decimal(12,2) NOT NULL,
  `status_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `employee_id` (`employee_id`),
  KEY `status_id` (`status_id`),
  KEY `supplier_id` (`supplier_id`),
  CONSTRAINT `import_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`),
  CONSTRAINT `import_ibfk_2` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`),
  CONSTRAINT `import_ibfk_3` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `import`
--

LOCK TABLES `import` WRITE;
/*!40000 ALTER TABLE `import` DISABLE KEYS */;
INSERT INTO `import` VALUES (1,'2024-01-01 08:00:00',1,1,6460000.00,17),(2,'2024-02-15 09:30:00',1,1,800000.00,17);
/*!40000 ALTER TABLE `import` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invoice`
--

DROP TABLE IF EXISTS `invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoice` (
  `id` int NOT NULL AUTO_INCREMENT,
  `create_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `employee_id` int NOT NULL,
  `customer_id` int NOT NULL,
  `discount_code` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  `discount_amount` decimal(10,2) NOT NULL,
  `total_price` decimal(12,2) NOT NULL,
  `status_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `employee_id` (`employee_id`),
  KEY `customer_id` (`customer_id`),
  KEY `status_id` (`status_id`),
  KEY `discount_code` (`discount_code`),
  CONSTRAINT `invoice_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`),
  CONSTRAINT `invoice_ibfk_2` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `invoice_ibfk_3` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`),
  CONSTRAINT `invoice_ibfk_4` FOREIGN KEY (`discount_code`) REFERENCES `discount` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice`
--

LOCK TABLES `invoice` WRITE;
/*!40000 ALTER TABLE `invoice` DISABLE KEYS */;
INSERT INTO `invoice` VALUES (1,'2024-02-01 10:00:00',1,1,NULL,0.00,98700.00,15),(2,'2024-02-10 14:20:00',1,2,NULL,0.00,162750.00,15);
/*!40000 ALTER TABLE `invoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `leave_request`
--

DROP TABLE IF EXISTS `leave_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `leave_request` (
  `id` int NOT NULL AUTO_INCREMENT,
  `leave_type_id` int DEFAULT NULL,
  `content` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `status_id` int NOT NULL,
  `employee_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `leave_request_ibfk_1` (`status_id`),
  KEY `leave_request_ibfk_2` (`employee_id`),
  KEY `leave_type_idx` (`leave_type_id`),
  CONSTRAINT `leave_request_ibfk_1` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`),
  CONSTRAINT `leave_request_ibfk_2` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`),
  CONSTRAINT `leave_type` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_type` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `leave_request`
--

LOCK TABLES `leave_request` WRITE;
/*!40000 ALTER TABLE `leave_request` DISABLE KEYS */;
INSERT INTO `leave_request` VALUES (1,1,'Về quê ăn giỗ','2026-02-10','2026-02-12',22,1),(2,1,'Sốt xuất huyết','2026-02-01','2026-02-05',20,2),(3,1,'Đi đám cưới bạn thân','2026-02-15','2026-02-15',22,3),(4,1,'Nghỉ sinh con theo chế độ','2026-03-01','2026-09-01',22,4),(5,1,'Đi du lịch Đà Lạt','2026-02-20','2026-02-25',22,5),(6,1,'Giải quyết việc gia đình','2026-02-03','2026-02-04',22,6),(7,1,'','2026-02-23','2026-02-24',1,2);
/*!40000 ALTER TABLE `leave_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `leave_type`
--

DROP TABLE IF EXISTS `leave_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `leave_type` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `fine_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `leave_type`
--

LOCK TABLES `leave_type` WRITE;
/*!40000 ALTER TABLE `leave_type` DISABLE KEYS */;
INSERT INTO `leave_type` VALUES (1,'Nghỉ có phép',500000.00);
/*!40000 ALTER TABLE `leave_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `module`
--

DROP TABLE IF EXISTS `module`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `module` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `module`
--

LOCK TABLES `module` WRITE;
/*!40000 ALTER TABLE `module` DISABLE KEYS */;
INSERT INTO `module` VALUES (1,'Quản lý nhân viên & Tài khoản'),(2,'Quản lý khách hàng'),(3,'Quản lý sản phẩm'),(4,'Quản lý nhà cung cấp'),(5,'Quản lý bán hàng'),(6,'Quản lý nhập hàng'),(7,'Quản lý thể loại'),(8,'Quản lý khuyến mãi'),(9,'Quản lý chức vụ'),(10,'Thống kê'),(11,'Quản lý nhân sự');
/*!40000 ALTER TABLE `module` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payroll_history`
--

DROP TABLE IF EXISTS `payroll_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payroll_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `employee_id` int NOT NULL,
  `salary_period` date NOT NULL,
  `temporary_salary` decimal(15,2) NOT NULL,
  `overtime_amount` decimal(15,2) DEFAULT '0.00',
  `total_allowance` decimal(15,2) DEFAULT '0.00',
  `total_bonus` decimal(15,2) DEFAULT '0.00',
  `total_deduction` decimal(15,2) DEFAULT '0.00',
  `total_fine` decimal(15,2) DEFAULT '0.00',
  `net_salary` decimal(15,2) NOT NULL,
  `paid_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_period` (`employee_id`,`salary_period`),
  CONSTRAINT `fk_payroll_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payroll_history`
--

LOCK TABLES `payroll_history` WRITE;
/*!40000 ALTER TABLE `payroll_history` DISABLE KEYS */;
INSERT INTO `payroll_history` VALUES (1,2,'2026-02-01',89090000.00,428145.98,1500000.00,0.00,9861814.60,-500000.00,80656331.38,'2026-02-16 00:40:34'),(5,1,'2026-02-01',89090000.00,0.00,1500000.00,0.00,9869000.00,0.00,80721000.00,'2026-02-22 22:10:40');
/*!40000 ALTER TABLE `payroll_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permission` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_bin NOT NULL,
  `permission_key` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `module_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `permission_key` (`permission_key`),
  KEY `module_id` (`module_id`),
  CONSTRAINT `permission_ibfk_1` FOREIGN KEY (`module_id`) REFERENCES `module` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
INSERT INTO `permission` VALUES (1,'Xem danh sách nhân viên','EMPLOYEE_LIST_VIEW',1),(2,'Xem hồ sơ cá nhân','EMPLOYEE_PERSONAL_VIEW',1),(3,'Cập nhật hồ sơ cá nhân','EMPLOYEE_PERSONAL_UPDATE',1),(4,'Xem vị trí công tác & lịch sử','EMPLOYEE_JOB_VIEW',1),(5,'Cập nhật vị trí công tác','EMPLOYEE_JOB_UPDATE',1),(6,'Xem lương & bảo hiểm','EMPLOYEE_PAYROLLINFO_VIEW',1),(7,'Cập nhật lương & bảo hiểm','EMPLOYEE_PAYROLLINFO_UPDATE',1),(8,'Xem tài khoản hệ thống','EMPLOYEE_ACCOUNT_VIEW',1),(9,'Đặt lại mật khẩu nhân viên','EMPLOYEE_ACCOUNT_RESET_PASSWORD',1),(10,'Cập nhật trạng thái tài khoản','EMPLOYEE_ACCOUNT_UPDATE_STATUS',1),(11,'Thêm mới nhân viên, tài khoản','EMPLOYEE_INSERT',1),(12,'Xóa nhân viên','EMPLOYEE_DELETE',1),(13,'Xem danh sách khách hàng','CUSTOMER_LIST_VIEW',2),(14,'Thêm khách hàng','CUSTOMER_INSERT',2),(15,'Cập nhật khách hàng','CUSTOMER_UPDATE',2),(16,'Xóa khách hàng','CUSTOMER_DELETE',2),(17,'Xem danh sách sản phẩm','PRODUCT_LIST_VIEW',3),(18,'Thêm sản phẩm','PRODUCT_INSERT',3),(19,'Cập nhật sản phẩm','PRODUCT_UPDATE',3),(20,'Xóa sản phẩm','PRODUCT_DELETE',3),(21,'Xem danh sách nhà cung cấp','SUPPLIER_LIST_VIEW',4),(22,'Thêm nhà cung cấp','SUPPLIER_INSERT',4),(23,'Cập nhật nhà cung cấp','SUPPLIER_UPDATE',4),(24,'Xóa nhà cung cấp','SUPPLIER_DELETE',4),(25,'Xem danh sách đơn hàng','ORDER_LIST_VIEW',5),(26,'Tạo đơn hàng mới','ORDER_CREATE',5),(27,'Xem phiếu nhập hàng','IMPORT_LIST_VIEW',6),(28,'Tạo phiếu nhập hàng mới','IMPORT_CREATE',6),(29,'Xem danh mục sản phẩm','CATEGORY_LIST_VIEW',7),(30,'Thêm danh mục sản phẩm','CATEGORY_INSERT',7),(31,'Cập nhật danh mục sản phẩm','CATEGORY_UPDATE',7),(32,'Xóa danh mục sản phẩm','CATEGORY_DELETE',7),(33,'Xem chương trình khuyến mãi','PROMOTION_LIST_VIEW',8),(34,'Thêm chương trình khuyến mãi','PROMOTION_INSERT',8),(35,'Cập nhật chương trình khuyến mãi','PROMOTION_UPDATE',8),(36,'Xóa chương trình khuyến mãi','PROMOTION_DELETE',8),(37,'Xem danh sách chức vụ','ROLE_VIEW',9),(38,'Thêm chức vụ mới','ROLE_INSERT',9),(39,'Cập nhật chức vụ','ROLE_UPDATE',9),(40,'Xóa chức vụ','ROLE_DELETE',9),(41,'Xem bảng phân quyền','PERMISSION_VIEW',9),(42,'Cập nhật cấu hình phân quyền','PERMISSION_UPDATE',9),(43,'Xem báo cáo thống kê','STATISTICS_VIEW',10),(44,'Xem đơn nghỉ phép','EMPLOYEE_LEAVE_REQUEST_VIEW',11),(45,'Tạo đơn nghỉ phép','EMPLOYEE_LEAVE_REQUEST_CREATE',11),(46,'Quản lý duyệt đơn nghỉ','EMPLOYEE_LEAVE_REQUEST_MANAGE',11),(47,'Xem kỷ luật & khen thưởng','EMPLOYEE_FINE_REWARD_VIEW',11),(48,'Quản lý kỷ luật & khen thưởng','EMPLOYEE_FINE_REWARD_MANAGE',11),(49,'Xem chấm công','EMPLOYEE_ATTENDANCE_VIEW',11),(50,'Quản lý chấm công','EMPLOYEE_ATTENDANCE_MANAGE',11),(51,'Cập nhật chức vụ','EMPLOYEE_ROLE_POSITION_UPDATE',11);
/*!40000 ALTER TABLE `permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_bin NOT NULL,
  `stock_quantity` int NOT NULL DEFAULT '0',
  `selling_price` decimal(10,2) NOT NULL DEFAULT '0.00',
  `import_price` decimal(10,2) NOT NULL DEFAULT '0.00',
  `status_id` int NOT NULL,
  `description` text COLLATE utf8mb4_bin,
  `image_url` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `category_id` int NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_product_status` (`status_id`),
  KEY `fk_category_id` (`category_id`),
  CONSTRAINT `fk_category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
  CONSTRAINT `fk_product_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES ('SP00001','Naruto - 01',38,21000.00,20000.00,6,'Minifigure nhân vật Naruto.','images/product/sp00001.png',2,'2026-02-15 23:54:40','2026-02-15 23:54:40'),('SP00002','Naruto - 02',37,18900.00,18000.00,6,'Minifigure Naruto trong trạng thái chiến đấu.','images/product/sp00002.png',2,'2026-02-15 23:54:40','2026-02-15 23:54:40'),('SP00003','Sasuke Uchiha',0,0.00,0.00,6,'Minifigure nhân vật Sasuke Uchiha từ series Naruto.',NULL,2,'2026-02-15 23:54:40','2026-02-15 23:54:40'),('SP00004','Kakashi Hatake',19,15750.00,15000.00,6,'Minifigure nhân vật Kakashi với Sharingan.',NULL,2,'2026-02-15 23:54:40','2026-02-15 23:54:40'),('SP00005','Sakura Haruno',20,26250.00,25000.00,6,'Minifigure nhân vật Sakura từ series Naruto.',NULL,2,'2026-02-15 23:54:40','2026-02-15 23:54:40'),('SP00007','Darth Vader',19,42000.00,40000.00,6,'Minifigure Darth Vader với lightsaber đỏ và mặt nạ.',NULL,2,'2026-02-15 23:54:40','2026-02-15 23:54:40'),('SP00008','Iron Man Mark 85',20,31500.00,30000.00,6,'Minifigure Iron Man trong bộ giáp Mark 85 từ Avengers: Endgame.',NULL,2,'2026-02-15 23:54:40','2026-02-15 23:54:40'),('SP00010','Harry Potter',19,105000.00,100000.00,6,'Minifigure Harry Potter với đũa phép và kính tròn.',NULL,2,'2026-02-15 23:54:40','2026-02-15 23:54:40'),('SP00011','LEGO Technic Bugatti Chiron',20,31500.00,30000.00,6,'Mô hình kỹ thuật cao của siêu xe Bugatti Chiron.',NULL,3,'2026-02-15 23:54:40','2026-02-15 23:54:41'),('SP00014','LEGO Technic Race Car',20,47250.00,45000.00,6,'Xe đua công thức 1 với động cơ pistons hoạt động.',NULL,3,'2026-02-15 23:54:40','2026-02-15 23:54:41'),('SP00019','LEGO Architecture Empire State Building',0,0.00,0.00,6,'Mô hình chi tiết của tòa nhà Empire State.',NULL,4,'2026-02-15 23:54:40','2026-02-15 23:54:41'),('SP00030','MOC - Tháp Rùa Hồ Gươm',0,0.00,0.00,6,'Mô hình Tháp Rùa trên Hồ Gươm, 1250 chi tiết.',NULL,6,'2026-02-15 23:54:40','2026-02-15 23:54:41'),('SP00031','LEGO City Police Station',0,0.00,0.00,6,'Trụ sở cảnh sát thành phố.',NULL,6,'2026-02-15 23:54:40','2026-02-15 23:54:41'),('SP00036','LEGO Star Wars Millennium Falcon',0,0.00,0.00,6,'Tàu Millennium Falcon với nhiều nhân vật.',NULL,4,'2026-02-15 23:54:40','2026-02-15 23:54:41'),('SP00038','LEGO Star Wars AT-AT',0,0.00,0.00,6,'Walker AT-AT từ phim The Empire Strikes Back.',NULL,4,'2026-02-15 23:54:40','2026-02-15 23:54:41'),('SP00039','LEGO Star Wars Death Star',0,0.00,0.00,6,'Ngôi sao tử thần Death Star.',NULL,4,'2026-02-15 23:54:40','2026-02-15 23:54:41'),('SP00040','LEGO Star Wars X-Wing Starfighter',0,0.00,65000.00,6,'Tàu chiến X-Wing của Luke Skywalker.',NULL,4,'2026-02-15 23:54:40','2026-02-15 23:54:41'),('SP00042','LEGO Marvel Sanctum Sanctorum',0,0.00,0.00,6,'Sanctum Sanctorum của Doctor Strange.',NULL,3,'2026-02-15 23:54:40','2026-02-15 23:54:41'),('SP00043','LEGO Marvel Guardians Ship',0,0.00,0.00,6,'Tàu của đội Guardians of the Galaxy.',NULL,3,'2026-02-15 23:54:40','2026-02-15 23:54:41'),('SP00044','LEGO Marvel Spider-Man Daily Bugle',0,0.00,0.00,6,'Tòa nhà Daily Bugle với nhiều nhân vật Spider-Man.',NULL,3,'2026-02-15 23:54:40','2026-02-15 23:54:41'),('SP00048','LEGO Harry Potter Hogwarts Express',0,0.00,0.00,6,'Tàu Hogwarts Express với sân ga 9¾.',NULL,1,'2026-02-15 23:54:40','2026-02-15 23:54:41'),('SP00049','LEGO Harry Potter Chamber of Secrets',0,0.00,0.00,6,'Phòng chứa bí mật với rắn Basilisk.',NULL,1,'2026-02-15 23:54:40','2026-02-15 23:54:41'),('SP00051','LEGO Creator Expert Bookshop',0,0.00,0.00,6,'Hiệu sách chi tiết với căn hộ ở trên.',NULL,2,'2026-02-15 23:54:40','2026-02-15 23:54:41'),('SP00052','LEGO Creator Expert Assembly Square',0,0.00,0.00,6,'Quảng trường trung tâm với nhiều tòa nhà.',NULL,4,'2026-02-15 23:54:40','2026-02-15 23:54:41');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profit_stat`
--

DROP TABLE IF EXISTS `profit_stat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `profit_stat` (
  `id` int NOT NULL AUTO_INCREMENT,
  `stat_date` date NOT NULL,
  `revenue` decimal(15,2) NOT NULL DEFAULT '0.00',
  `expense` decimal(15,2) NOT NULL DEFAULT '0.00',
  `total_profit` decimal(15,2) NOT NULL DEFAULT '0.00',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profit_stat`
--

LOCK TABLES `profit_stat` WRITE;
/*!40000 ALTER TABLE `profit_stat` DISABLE KEYS */;
/*!40000 ALTER TABLE `profit_stat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `report`
--

DROP TABLE IF EXISTS `report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `report` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) COLLATE utf8mb4_bin NOT NULL,
  `description` text COLLATE utf8mb4_bin,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `level` varchar(50) COLLATE utf8mb4_bin NOT NULL,
  `category` varchar(50) COLLATE utf8mb4_bin NOT NULL,
  `employee_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_report_employee` (`employee_id`),
  CONSTRAINT `fk_report_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `report`
--

LOCK TABLES `report` WRITE;
/*!40000 ALTER TABLE `report` DISABLE KEYS */;
/*!40000 ALTER TABLE `report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(150) COLLATE utf8mb4_bin NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `start_experience` int DEFAULT '0',
  `end_experience` int DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `salary_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `salary_id` (`salary_id`),
  CONSTRAINT `role_ibfk_1` FOREIGN KEY (`salary_id`) REFERENCES `salary` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'IT Admin','Quản trị hệ thống toàn quyền',0,0,'2026-02-15 23:54:40','2026-02-15 23:54:40',1),(2,'Tổng giám đốc','Chủ cửa hàng/Điều hành cao cấp',10,30,'2026-02-15 23:54:40','2026-02-15 23:54:40',1),(3,'Quản lý cửa hàng','Điều hành toàn diện hoạt động cửa hàng',4,15,'2026-02-15 23:54:40','2026-02-15 23:54:40',8),(4,'Trưởng nhóm bán hàng','Giám sát ca làm việc và hỗ trợ thanh toán phức tạp',2,4,'2026-02-15 23:54:40','2026-02-15 23:54:40',10),(5,'Nhân viên bán hàng (Bậc 3)','Nhân viên nòng cốt, hỗ trợ đào tạo người mới',3,5,'2026-02-15 23:54:40','2026-02-15 23:54:40',11),(6,'Nhân viên bán hàng (Bậc 2)','Nhân viên kinh nghiệm, tư vấn chuyên sâu',1,3,'2026-02-15 23:54:40','2026-02-15 23:54:40',12),(7,'Nhân viên bán hàng (Bậc 1)','Nhân viên mới',0,1,'2026-02-15 23:54:40','2026-02-15 23:54:40',13),(8,'Nhân viên kho (Bậc 2)','Quản lý nhập xuất kho',2,5,'2026-02-15 23:54:40','2026-02-15 23:54:40',8),(9,'Nhân viên kho (Bậc 1)','Sắp xếp và kiểm kê kho',0,2,'2026-02-15 23:54:40','2026-02-15 23:54:40',9);
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_permission`
--

DROP TABLE IF EXISTS `role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_permission` (
  `role_id` int NOT NULL,
  `permission_id` int NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `permission_id` (`permission_id`),
  CONSTRAINT `role_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  CONSTRAINT `role_permission_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_permission`
--

LOCK TABLES `role_permission` WRITE;
/*!40000 ALTER TABLE `role_permission` DISABLE KEYS */;
INSERT INTO `role_permission` VALUES (1,1),(2,1),(3,1),(1,2),(2,2),(3,2),(1,3),(2,3),(1,4),(2,4),(3,4),(1,5),(2,5),(1,6),(2,6),(1,7),(2,7),(1,8),(2,8),(1,9),(2,9),(1,10),(2,10),(1,11),(2,11),(1,12),(2,12),(1,13),(2,13),(1,14),(2,14),(1,15),(2,15),(1,16),(2,16),(1,17),(2,17),(1,18),(2,18),(1,19),(2,19),(3,19),(8,19),(9,19),(1,20),(2,20),(1,21),(2,21),(1,22),(2,22),(1,23),(2,23),(1,24),(2,24),(1,25),(2,25),(1,26),(2,26),(4,26),(5,26),(6,26),(7,26),(1,27),(2,27),(1,28),(2,28),(8,28),(9,28),(1,29),(2,29),(1,30),(2,30),(1,31),(2,31),(1,32),(2,32),(1,33),(2,33),(1,34),(2,34),(1,35),(2,35),(1,36),(2,36),(1,37),(2,37),(1,38),(2,38),(1,39),(2,39),(1,40),(2,40),(1,41),(2,41),(1,42),(2,42),(1,43),(2,43),(3,43),(1,44),(2,44),(3,44),(5,44),(6,44),(7,44),(8,44),(9,44),(1,45),(2,45),(3,45),(5,45),(6,45),(7,45),(8,45),(9,45),(1,46),(2,46),(3,46),(1,47),(2,47),(3,47),(1,48),(2,48),(3,48),(1,49),(2,49),(3,49),(5,49),(6,49),(7,49),(8,49),(9,49),(1,50),(2,50),(3,50),(1,51),(2,51);
/*!40000 ALTER TABLE `role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `salary`
--

DROP TABLE IF EXISTS `salary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `salary` (
  `id` int NOT NULL AUTO_INCREMENT,
  `base` decimal(10,2) NOT NULL,
  `coefficient` decimal(5,2) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `salary`
--

LOCK TABLES `salary` WRITE;
/*!40000 ALTER TABLE `salary` DISABLE KEYS */;
INSERT INTO `salary` VALUES (1,30200000.00,2.95,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(2,25200000.00,2.65,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(3,24200000.00,2.45,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(4,19200000.00,2.15,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(5,18200000.00,2.15,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(6,14200000.00,1.65,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(7,15200000.00,1.75,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(8,9200000.00,1.35,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(9,10200000.00,1.40,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(10,11200000.00,1.45,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(11,6700000.00,1.20,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(12,7200000.00,1.25,'2026-02-15 23:54:40','2026-02-15 23:54:40'),(13,5400000.00,1.10,'2026-02-15 23:54:40','2026-02-15 23:54:40');
/*!40000 ALTER TABLE `salary` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `status`
--

DROP TABLE IF EXISTS `status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `status` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_bin NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `type` varchar(50) COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `status`
--

LOCK TABLES `status` WRITE;
/*!40000 ALTER TABLE `status` DISABLE KEYS */;
INSERT INTO `status` VALUES (1,'Active','Đang làm việc','EMPLOYEE'),(2,'Inactive','Đã nghỉ việc','EMPLOYEE'),(3,'On_Leave','Đang nghỉ phép','EMPLOYEE'),(4,'Active','Được phép đăng nhập','ACCOUNT'),(5,'Locked','Bị khóa (do sai pass/vi phạm)','ACCOUNT'),(6,'Active','Đang kinh doanh','PRODUCT'),(7,'Suspended','Ngừng kinh doanh','PRODUCT'),(8,'Inactive','Vô hiệu','PRODUCT'),(9,'Active','Hoạt động','CATEGORY'),(10,'Inactive','Vô hiệu','CATEGORY'),(11,'Active','Hoạt động','SUPPLIER'),(12,'Inactive','Vô hiệu','SUPPLIER'),(13,'Active','Hoạt động','CUSTOMER'),(14,'Inactive','Ngưng tương tác','CUSTOMER'),(15,'Completed','Hoàn thành','INVOICE'),(16,'Canceled','Hủy bỏ','INVOICE'),(17,'Completed','Hoàn thành','IMPORT'),(18,'Canceled','Hủy bỏ','IMPORT'),(19,'Pending','Đơn đang chờ quản lý phê duyệt','LEAVE_REQUEST'),(20,'Approved','Đơn đã được chấp thuận','LEAVE_REQUEST'),(21,'Rejected','Đơn bị từ chối','LEAVE_REQUEST'),(22,'Canceled','Đơn đã bị hủy bởi nhân viên','LEAVE_REQUEST'),(23,'Pending','Quyết định đang chờ cấp trên phê duyệt','EMPLOYMENT_HISTORY'),(24,'Approved','Quyết định đã được duyệt, chờ ngày có hiệu lực','EMPLOYMENT_HISTORY'),(25,'Effective','Quyết định đã chính thức đi vào hiệu lực','EMPLOYMENT_HISTORY'),(26,'Rejected','Quyết định bị cấp trên từ chối','EMPLOYMENT_HISTORY'),(27,'Canceled','Quyết định đã bị hủy bỏ trước khi thực hiện','EMPLOYMENT_HISTORY'),(28,'Active','Hoạt động','DEPARTMENT'),(29,'Inactive','Vô hiệu','DEPARTMENT');
/*!40000 ALTER TABLE `status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `supplier`
--

DROP TABLE IF EXISTS `supplier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `phone` varchar(15) COLLATE utf8mb4_bin NOT NULL,
  `address` varchar(255) COLLATE utf8mb4_bin NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `status_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_supplier_status` (`status_id`),
  CONSTRAINT `fk_supplier_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier`
--

LOCK TABLES `supplier` WRITE;
/*!40000 ALTER TABLE `supplier` DISABLE KEYS */;
INSERT INTO `supplier` VALUES (1,'Nhà cung cấp A','0903344554','99 An Dương Vương, Phường 16, Quận 8, TP Hồ Chí Minh','supplierA@example.com',11),(2,'Nhà cung cấp B','0903344556','04 Tôn Đức Thắng, Phường Bến Nghé, Quận 1, TP Hồ Chí Minh','supplierB@example.com',11),(3,'Nhà cung cấp C','0903344557','123 Nguyễn Thị Minh Khai, Quận 3, TP Hồ Chí Minh','supplierC@example.com',11),(4,'Nhà cung cấp D','0903344558','456 Lê Lợi, Quận 1, TP Hồ Chí Minh','supplierD@example.com',11),(5,'Nhà cung cấp E','0903344559','789 Trường Chinh, Quận Tân Bình, TP Hồ Chí Minh','supplierE@example.com',11),(6,'Nhà cung cấp F','0903344560','101 Nguyễn Văn Cừ, Quận 5, TP Hồ Chí Minh','supplierF@example.com',11),(7,'Nhà cung cấp G','0903344561','202 Phan Văn Trị, Quận Bình Thạnh, TP Hồ Chí Minh','supplierG@example.com',11),(8,'Nhà cung cấp H','0903344562','303 Nguyễn Huệ, Quận 1, TP Hồ Chí Minh','supplierH@example.com',11),(9,'Nhà cung cấp I','0903344563','404 Lê Văn Sỹ, Quận 3, TP Hồ Chí Minh','supplierI@example.com',11),(10,'Nhà cung cấp J','0903344564','505 Bến Vân Đồn, Quận 4, TP Hồ Chí Minh','supplierJ@example.com',11),(11,'Nhà cung cấp K','0903344565','606 Đinh Tiên Hoàng, Quận Bình Thạnh, TP Hồ Chí Minh','supplierK@example.com',11),(12,'Nhà cung cấp L','0903344566','707 Trần Hưng Đạo, Quận 1, TP Hồ Chí Minh','supplierL@example.com',11),(13,'Nhà cung cấp M','0903344567','808 Hoàng Văn Thụ, Quận Tân Bình, TP Hồ Chí Minh','supplierM@example.com',11),(14,'Nhà cung cấp N','0903344568','909 Nguyễn Thái Sơn, Quận Gò Vấp, TP Hồ Chí Minh','supplierN@example.com',11),(15,'Nhà cung cấp O','0903344569','1001 Lạc Long Quân, Quận 11, TP Hồ Chí Minh','supplierO@example.com',11),(16,'Nhà cung cấp P','0903344570','1102 Âu Cơ, Quận Tân Phú, TP Hồ Chí Minh','supplierP@example.com',11),(17,'Nhà cung cấp Q','0903344571','1203 Trần Quốc Toản, Quận 3, TP Hồ Chí Minh','supplierQ@example.com',11),(18,'Nhà cung cấp R','0903344572','1304 Ngô Quyền, Quận 10, TP Hồ Chí Minh','supplierR@example.com',11),(19,'Nhà cung cấp S','0903344573','1405 Đinh Bộ Lĩnh, Quận Bình Thạnh, TP Hồ Chí Minh','supplierS@example.com',11),(20,'Nhà cung cấp T','0903344574','1506 Huỳnh Tấn Phát, Quận 7, TP Hồ Chí Minh','supplierT@example.com',11);
/*!40000 ALTER TABLE `supplier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tax`
--

DROP TABLE IF EXISTS `tax`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tax` (
  `id` int NOT NULL AUTO_INCREMENT,
  `employee_id` int NOT NULL,
  `num_dependents` int DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee` (`employee_id`),
  CONSTRAINT `fk_tax_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tax`
--

LOCK TABLES `tax` WRITE;
/*!40000 ALTER TABLE `tax` DISABLE KEYS */;
INSERT INTO `tax` VALUES (1,1,0),(2,2,0),(3,3,0),(4,4,0),(5,5,0),(6,7,0),(7,8,0),(8,9,0),(9,10,0),(10,11,0),(11,12,0),(12,13,0),(13,6,0);
/*!40000 ALTER TABLE `tax` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `time_sheet`
--

DROP TABLE IF EXISTS `time_sheet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `time_sheet` (
  `id` int NOT NULL AUTO_INCREMENT,
  `employee_id` int NOT NULL,
  `check_in` datetime NOT NULL,
  `check_out` datetime DEFAULT NULL,
  `work_hours` decimal(10,2) DEFAULT '0.00',
  `ot_hours` decimal(5,2) DEFAULT '0.00',
  PRIMARY KEY (`id`),
  KEY `time_sheet_ibfk_1` (`employee_id`),
  CONSTRAINT `time_sheet_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

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

-- Dump completed on 2026-02-24 17:09:41
