-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: bookweb
-- ------------------------------------------------------
-- Server version	8.0.44

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
-- Table structure for table `authors`
--

DROP TABLE IF EXISTS `authors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `authors` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `bio` text COLLATE utf8mb4_unicode_ci,
  `imageUrl` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dateOfBirth` datetime DEFAULT NULL,
  `nationality` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('active','inactive') COLLATE utf8mb4_unicode_ci DEFAULT 'active',
  `createdAt` datetime NOT NULL,
  `updatedAt` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `slug` (`slug`),
  UNIQUE KEY `name_2` (`name`),
  UNIQUE KEY `name_3` (`name`),
  UNIQUE KEY `slug_2` (`slug`),
  UNIQUE KEY `slug_3` (`slug`),
  UNIQUE KEY `name_4` (`name`),
  UNIQUE KEY `name_5` (`name`),
  UNIQUE KEY `slug_4` (`slug`),
  UNIQUE KEY `name_6` (`name`),
  UNIQUE KEY `slug_5` (`slug`),
  UNIQUE KEY `slug_6` (`slug`),
  UNIQUE KEY `name_7` (`name`),
  UNIQUE KEY `slug_7` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `authors`
--

LOCK TABLES `authors` WRITE;
/*!40000 ALTER TABLE `authors` DISABLE KEYS */;
INSERT INTO `authors` VALUES ('218d2996-6efc-4f80-a766-8d198d3504e0','Stephen Hawking','stephen-hawking','',NULL,NULL,'','active','2026-03-31 22:11:58','2026-03-31 22:11:58'),('4bebdf8c-b2b4-439b-8a4b-f5fbbf31fe47','Dale Carnegie','dale-carnegie','',NULL,NULL,'','active','2026-03-31 22:11:45','2026-03-31 22:11:45'),('ab1b6fe1-b067-4064-9c65-28de21c321de','Paulo Coelho','paulo-coelho','',NULL,NULL,'','active','2026-03-31 22:11:38','2026-03-31 22:11:38'),('e1af0ece-e956-49e1-9c74-242c52f2e8d9','Robert Kiyosaki','robert-kiyosaki','',NULL,NULL,'','active','2026-03-31 22:11:51','2026-03-31 22:11:51'),('f03d9551-c1d8-4164-9ee4-c97d89cfa81d','Antoine de Saint-Exupéry','antoine-de-saint-exupry','',NULL,NULL,'','active','2026-03-31 22:12:05','2026-03-31 22:12:05');
/*!40000 ALTER TABLE `authors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `books`
--

DROP TABLE IF EXISTS `books`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `books` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `isbn` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `discount` decimal(5,2) DEFAULT '0.00',
  `coverImage` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `publisher` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `publicationDate` datetime DEFAULT NULL,
  `pages` int DEFAULT NULL,
  `stock` int DEFAULT '0',
  `rating` decimal(3,2) DEFAULT '0.00',
  `views` int DEFAULT '0',
  `status` enum('active','inactive','archived') COLLATE utf8mb4_unicode_ci DEFAULT 'active',
  `createdAt` datetime NOT NULL,
  `updatedAt` datetime NOT NULL,
  `categoryId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `authorId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`),
  UNIQUE KEY `slug_2` (`slug`),
  UNIQUE KEY `slug_3` (`slug`),
  UNIQUE KEY `slug_4` (`slug`),
  UNIQUE KEY `isbn` (`isbn`),
  UNIQUE KEY `isbn_2` (`isbn`),
  UNIQUE KEY `isbn_3` (`isbn`),
  UNIQUE KEY `isbn_4` (`isbn`),
  KEY `categoryId` (`categoryId`),
  KEY `authorId` (`authorId`),
  CONSTRAINT `books_ibfk_1` FOREIGN KEY (`categoryId`) REFERENCES `categories` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `books_ibfk_2` FOREIGN KEY (`authorId`) REFERENCES `authors` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `books_ibfk_3` FOREIGN KEY (`categoryId`) REFERENCES `categories` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `books_ibfk_4` FOREIGN KEY (`authorId`) REFERENCES `authors` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `books_ibfk_5` FOREIGN KEY (`categoryId`) REFERENCES `categories` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `books_ibfk_6` FOREIGN KEY (`categoryId`) REFERENCES `categories` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `books_ibfk_7` FOREIGN KEY (`authorId`) REFERENCES `authors` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `books`
--

LOCK TABLES `books` WRITE;
/*!40000 ALTER TABLE `books` DISABLE KEYS */;
INSERT INTO `books` VALUES ('23a4343d-cb1f-4b23-9b0d-352781001e62','O Zahir','o-zahir','Cuộc tìm kiếm sự tự do và ý nghĩa tình yêu của một nhà văn nổi tiếng.','9786045635249',120000.00,10.00,'/uploads/books/1775136012110_2.jpg',NULL,NULL,350,45,0.00,1,'active','2026-03-31 22:14:44','2026-04-02 13:20:12','859e1c7c-1ab7-4b83-977d-80abcb495931','ab1b6fe1-b067-4064-9c65-28de21c321de'),('2a43a5ba-d92e-4073-884b-42326d621a75','Vũ Trụ Trong Vỏ Hạt Dẻ','v-tr-trong-v-ht-d','Những khám phá mới nhất về vật lý lý thuyết và cơ học lượng tử.','9786042123470',160000.00,12.00,'/uploads/books/1775136186859_12.webp',NULL,NULL,215,55,0.00,1,'active','2026-03-31 22:25:38','2026-04-02 13:23:06','e1242b41-4fa0-435d-8347-5a6f51942fdd','218d2996-6efc-4f80-a766-8d198d3504e0'),('2a8bec0f-5b3e-482b-ae0b-dd5b04bb3af5','Lược Sử Thời Gian','lc-s-thi-gian','Khám phá những câu hỏi lớn nhất về vũ trụ và nguồn gốc vạn vật.','9786042123456',3000.00,0.00,'/uploads/books/1775136266299_7.jpg',NULL,NULL,285,75,0.00,2,'active','2026-03-31 22:23:39','2026-04-02 13:25:56','e1242b41-4fa0-435d-8347-5a6f51942fdd','218d2996-6efc-4f80-a766-8d198d3504e0'),('2c0e8475-2b19-4b5c-8b6a-10f923442b60','Lợi Thế Ngôn Từ','li-th-ngn-t','Cách sử dụng ngôn ngữ để tạo ấn tượng và thuyết phục người đối diện.','9786045880557',72000.00,10.00,'/uploads/books/1775136123410_6.webp',NULL,NULL,280,85,0.00,1,'active','2026-03-31 22:19:52','2026-04-02 13:22:03','847373d4-d40b-4640-8f6e-6cd2d9578e39','4bebdf8c-b2b4-439b-8a4b-f5fbbf31fe47'),('4f1a86c9-13f7-48e3-a51e-45f516ee1a37','Bản Thiết Kế Vĩ Đại','bn-thit-k-v-i','Cách nhìn mới về lý thuyết vạn vật và sự hình thành vũ trụ.','9786042123463',145000.00,5.00,'/uploads/books/1775136177205_11.jpg',NULL,NULL,230,40,0.00,1,'active','2026-03-31 22:24:35','2026-04-02 13:22:57','e1242b41-4fa0-435d-8347-5a6f51942fdd','218d2996-6efc-4f80-a766-8d198d3504e0'),('6a94ee01-c5c3-4b6e-aea4-97f41ec411c8','Bay Đêm','bay-m','Sự dũng cảm và trách nhiệm của những phi công tiên phong.','9786042087678',3000.00,0.00,'/uploads/books/1775136242565_15.webp',NULL,NULL,180,25,0.00,4,'active','2026-03-31 22:27:57','2026-04-02 13:25:46','8c2557d7-8fb1-4a5b-90bd-03ad931f496c','f03d9551-c1d8-4164-9ee4-c97d89cfa81d'),('79773a03-8bcf-48d0-8180-b137e3443482','Nhà Giả Kim','nh-gi-kim','Câu chuyện về hành trình theo đuổi vận mệnh của chàng chăn cừu Santiago.','9786045635232',79000.00,20.00,'/uploads/books/1774995199517_1.jpg',NULL,NULL,228,150,0.00,2,'active','2026-03-31 22:13:19','2026-04-02 13:19:12','859e1c7c-1ab7-4b83-977d-80abcb495931','ab1b6fe1-b067-4064-9c65-28de21c321de'),('848ddcdc-ea07-4e4f-a8f1-bb35b3e1e731','Hoàng Tử Bé','hong-t-b','Cuộc du hành của một vị hoàng tử nhỏ qua các hành tinh khác nhau.','9786042087654',65000.00,20.00,'/uploads/books/1775135975801_1.jpg',NULL,NULL,120,500,0.00,2,'active','2026-03-31 22:26:15','2026-04-02 13:19:35','8c2557d7-8fb1-4a5b-90bd-03ad931f496c','f03d9551-c1d8-4164-9ee4-c97d89cfa81d'),('912a06f0-d425-4067-a412-2dfe0820eb75','Ngoại tình','ngoi-tnh','Những suy tư về hạnh phúc và sự nhàm chán trong hôn nhân hiện đại.','9786045635256',3000.00,0.00,'/uploads/books/1775136038874_3.webp',NULL,NULL,312,60,0.00,2,'active','2026-03-31 22:16:02','2026-04-02 13:26:08','859e1c7c-1ab7-4b83-977d-80abcb495931','ab1b6fe1-b067-4064-9c65-28de21c321de'),('9f33ac67-296f-44c0-aaa2-e27401ea6ab0','Doanh Nghiệp Thế Kỷ 21','doanh-nghip-th-k-21','Tầm nhìn về mô hình kinh doanh mạng lưới trong thời đại mới.','9786043350147',85000.00,30.00,'/uploads/books/1775136157034_9.webp',NULL,NULL,250,140,0.00,1,'active','2026-03-31 22:22:54','2026-04-02 13:22:37','1e1e638a-62de-4376-b707-90c46bb59cb9','e1af0ece-e956-49e1-9c74-242c52f2e8d9'),('a3b0b491-370d-4f96-887a-31d62dd68eec','Cha Giàu Cha Nghèo','cha-giu-cha-ngho','Bài học về tư duy tài chính và cách làm giàu từ hai người cha.','9786043350123',98000.00,15.00,'/uploads/books/1775136308713_10.jpg',NULL,NULL,344,210,0.00,1,'active','2026-03-31 22:20:46','2026-04-02 13:25:08','1e1e638a-62de-4376-b707-90c46bb59cb9','e1af0ece-e956-49e1-9c74-242c52f2e8d9'),('b7235133-0394-46e4-8e8c-591f4114622f','Đắc Nhân Tâm','c-nhn-tm','Cuốn sách kinh điển về nghệ thuật giao tiếp và thu phục lòng người.','9786045880533',86000.00,25.00,'/uploads/books/1775136056995_4.webp',NULL,NULL,320,300,0.00,1,'active','2026-03-31 22:17:19','2026-04-02 13:20:57','847373d4-d40b-4640-8f6e-6cd2d9578e39','4bebdf8c-b2b4-439b-8a4b-f5fbbf31fe47'),('cd4bbde0-6c9c-4d1e-ab59-73fe9f484951','Cõi Người Ta','ci-ngi-ta','Những hồi ức về những chuyến bay đầy mạo hiểm và triết lý sống.','9786042087661',85000.00,15.00,'/uploads/books/1775136232574_14.jpg',NULL,NULL,240,30,0.00,1,'active','2026-03-31 22:27:13','2026-04-02 13:23:52','8c2557d7-8fb1-4a5b-90bd-03ad931f496c','f03d9551-c1d8-4164-9ee4-c97d89cfa81d'),('d581a885-b029-48c9-878c-826959b6e78f','Quẳng Gánh Lo Đi Và Vui Sống','qung-gnh-lo-i-v-vui-sng','Phương pháp để loại bỏ lo lắng và tận hưởng cuộc sống mỗi ngày.','9786045880540',78000.00,20.00,'/uploads/books/1775136087009_5.webp',NULL,NULL,300,120,0.00,2,'active','2026-03-31 22:18:37','2026-04-02 13:21:27','847373d4-d40b-4640-8f6e-6cd2d9578e39','4bebdf8c-b2b4-439b-8a4b-f5fbbf31fe47'),('fe574cea-b5cd-4e66-ad0a-2bf80758d2e3','Dạy Con Làm Giàu - Tập 2','dy-con-lm-giu-tp-2','Sử dụng kim tứ đồ để hiểu về các nguồn thu nhập khác nhau.','9786043350130',115000.00,10.00,'/uploads/books/1775136148014_8.webp',NULL,NULL,420,95,0.00,1,'active','2026-03-31 22:21:55','2026-04-02 13:22:28','1e1e638a-62de-4376-b707-90c46bb59cb9','e1af0ece-e956-49e1-9c74-242c52f2e8d9');
/*!40000 ALTER TABLE `books` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('active','inactive') COLLATE utf8mb4_unicode_ci DEFAULT 'active',
  `createdAt` datetime NOT NULL,
  `updatedAt` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `slug` (`slug`),
  UNIQUE KEY `name_2` (`name`),
  UNIQUE KEY `name_3` (`name`),
  UNIQUE KEY `slug_2` (`slug`),
  UNIQUE KEY `slug_3` (`slug`),
  UNIQUE KEY `name_4` (`name`),
  UNIQUE KEY `name_5` (`name`),
  UNIQUE KEY `name_6` (`name`),
  UNIQUE KEY `slug_4` (`slug`),
  UNIQUE KEY `slug_5` (`slug`),
  UNIQUE KEY `slug_6` (`slug`),
  UNIQUE KEY `name_7` (`name`),
  UNIQUE KEY `slug_7` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES ('1e1e638a-62de-4376-b707-90c46bb59cb9','Kinh tế & Quản trị','kinh-t-qun-tr','',NULL,'active','2026-03-31 22:11:05','2026-03-31 22:11:05'),('847373d4-d40b-4640-8f6e-6cd2d9578e39','Kỹ năng sống','k-nng-sng','',NULL,'active','2026-03-31 22:10:57','2026-03-31 22:10:57'),('859e1c7c-1ab7-4b83-977d-80abcb495931','Văn học Kinh điển','vn-hc-kinh-in','',NULL,'active','2026-03-31 22:10:42','2026-03-31 22:10:42'),('8c2557d7-8fb1-4a5b-90bd-03ad931f496c','Thiếu nhi','thiu-nhi','',NULL,'active','2026-03-31 22:11:17','2026-03-31 22:11:17'),('e1242b41-4fa0-435d-8347-5a6f51942fdd','Khoa học & Vũ trụ','khoa-hc-v-tr','',NULL,'active','2026-03-31 22:11:12','2026-03-31 22:11:12');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orderitems`
--

DROP TABLE IF EXISTS `orderitems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orderitems` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `quantity` int NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `discount` decimal(5,2) DEFAULT '0.00',
  `createdAt` datetime NOT NULL,
  `updatedAt` datetime NOT NULL,
  `orderId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `bookId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `orderId` (`orderId`),
  KEY `bookId` (`bookId`),
  CONSTRAINT `orderitems_ibfk_1` FOREIGN KEY (`orderId`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `orderitems_ibfk_2` FOREIGN KEY (`bookId`) REFERENCES `books` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `orderitems_ibfk_3` FOREIGN KEY (`orderId`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `orderitems_ibfk_4` FOREIGN KEY (`bookId`) REFERENCES `books` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `orderitems_ibfk_5` FOREIGN KEY (`orderId`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `orderitems_ibfk_6` FOREIGN KEY (`bookId`) REFERENCES `books` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orderitems`
--

LOCK TABLES `orderitems` WRITE;
/*!40000 ALTER TABLE `orderitems` DISABLE KEYS */;
INSERT INTO `orderitems` VALUES ('0d27534c-c10f-41b3-be29-6d15ec6a12c1',1,1000.00,0.00,'2026-03-31 21:32:22','2026-03-31 21:32:22','aed55559-cd5b-4bf3-88a7-b9fe2e304f07',NULL),('1868751c-d2a8-49bc-98ea-c26c5b193d2d',2,3000.00,0.00,'2026-03-31 22:01:46','2026-03-31 22:01:46','0f09f7dd-134c-4ab8-94db-ae3ca6771546',NULL),('3eb97017-c06d-4060-b40b-a02648e10b5d',1,1000.00,0.00,'2026-03-31 21:32:22','2026-03-31 21:32:22','43cfa7a8-3d1e-4742-8311-bdbda7b275ab',NULL),('43728a6d-8871-4499-9b74-6afb5115be47',1,3000.00,0.00,'2026-03-31 22:00:32','2026-03-31 22:00:32','b3f060c4-2fc1-4835-b44d-62a76550767c',NULL),('7a1d5df3-74d6-4103-8ec5-b7c141e2f59b',1,1000.00,0.00,'2026-03-31 21:37:57','2026-03-31 21:37:57','c10b2f08-e07d-4ab2-85ed-c215893f4c23',NULL),('8a1c124c-5d12-4d1b-9b55-70d9e135896a',1,1000.00,0.00,'2026-03-31 21:15:20','2026-03-31 21:15:20','6191e88d-20d1-443e-9a5a-41eaef76c48f',NULL),('a0beace0-fca5-48ee-8971-8744de8098e6',2,3000.00,0.00,'2026-03-31 22:02:24','2026-03-31 22:02:24','57cdbb7f-2e77-4acc-af20-c9c3d12406ef',NULL),('a98a7579-8526-4d68-9fbb-22857c65ad46',1,3000.00,0.00,'2026-03-31 21:42:32','2026-03-31 21:42:32','556bb9cf-1584-4356-9373-d43a41f8c99f',NULL),('cb5e6f4a-ef58-43a9-9730-c97a75843622',1,3000.00,0.00,'2026-03-31 21:44:52','2026-03-31 21:44:52','a0e0f880-211f-4743-979b-f76acc8cd57e',NULL),('d145e1a4-c6f8-47d5-8094-aba105ef7e34',1,3000.00,0.00,'2026-03-31 21:39:38','2026-03-31 21:39:38','04eb1750-e62c-4adb-bd6a-0e9611025931',NULL);
/*!40000 ALTER TABLE `orderitems` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `orderNumber` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `totalPrice` decimal(10,2) NOT NULL,
  `totalDiscount` decimal(10,2) DEFAULT '0.00',
  `shippingAddress` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `shippingCost` decimal(10,2) DEFAULT '0.00',
  `paymentMethod` enum('credit_card','bank_transfer','cash_on_delivery','vietqr') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('pending','processing','shipped','delivered','cancelled') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `notes` text COLLATE utf8mb4_unicode_ci,
  `createdAt` datetime NOT NULL,
  `updatedAt` datetime NOT NULL,
  `userId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `customerName` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `customerEmail` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `customerPhone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `orderNumber` (`orderNumber`),
  UNIQUE KEY `orderNumber_2` (`orderNumber`),
  UNIQUE KEY `orderNumber_3` (`orderNumber`),
  KEY `userId` (`userId`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`userId`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `orders_ibfk_3` FOREIGN KEY (`userId`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES ('04eb1750-e62c-4adb-bd6a-0e9611025931','ORD-1774993178634',3000.00,0.00,'bac leiu, bac lieu, hcm',0.00,'vietqr','pending',NULL,'2026-03-31 21:39:38','2026-03-31 21:39:38','90e69e25-1080-454c-94fd-f4be12f63314','le toan','37.vantoan@gmail.com','0946020447'),('0f09f7dd-134c-4ab8-94db-ae3ca6771546','ORD-1774994506376',6000.00,0.00,'bac leiu, bac lieu, hcm',0.00,'vietqr','pending',NULL,'2026-03-31 22:01:46','2026-03-31 22:01:46','90e69e25-1080-454c-94fd-f4be12f63314','le toan','37.vantoan@gmail.com','0946020447'),('43cfa7a8-3d1e-4742-8311-bdbda7b275ab','ORD-1774992742685',1000.00,0.00,'bac leiu, bac lieu, hcm',0.00,'vietqr','pending',NULL,'2026-03-31 21:32:22','2026-03-31 21:32:22','90e69e25-1080-454c-94fd-f4be12f63314','le toan','37.vantoan@gmail.com','0946020447'),('556bb9cf-1584-4356-9373-d43a41f8c99f','ORD-1774993352496',3000.00,0.00,'bac leiu, bac lieu, hcm',0.00,'vietqr','pending',NULL,'2026-03-31 21:42:32','2026-03-31 21:42:32','90e69e25-1080-454c-94fd-f4be12f63314','le toan','37.vantoan@gmail.com','0946020447'),('57cdbb7f-2e77-4acc-af20-c9c3d12406ef','ORD-1774994544365',6000.00,0.00,'bac leiu, bac lieu, hcm',0.00,'vietqr','pending',NULL,'2026-03-31 22:02:24','2026-03-31 22:02:24','90e69e25-1080-454c-94fd-f4be12f63314','le toan','37.vantoan@gmail.com','0946020447'),('6191e88d-20d1-443e-9a5a-41eaef76c48f','ORD-1774991720593',1000.00,0.00,'bac leiu, bac lieu, hcm',0.00,'vietqr','pending',NULL,'2026-03-31 21:15:20','2026-03-31 21:15:20','90e69e25-1080-454c-94fd-f4be12f63314','le toan','37.vantoan@gmail.com','0946020447'),('a0e0f880-211f-4743-979b-f76acc8cd57e','ORD-1774993492547',3000.00,0.00,'bac leiu, bac lieu, hcm',0.00,'vietqr','pending',NULL,'2026-03-31 21:44:52','2026-03-31 21:44:52','90e69e25-1080-454c-94fd-f4be12f63314','le toan','37.vantoan@gmail.com','0946020447'),('aed55559-cd5b-4bf3-88a7-b9fe2e304f07','ORD-1774992742650',1000.00,0.00,'bac leiu, bac lieu, hcm',0.00,'vietqr','pending',NULL,'2026-03-31 21:32:22','2026-03-31 21:32:22','90e69e25-1080-454c-94fd-f4be12f63314','le toan','37.vantoan@gmail.com','0946020447'),('b3f060c4-2fc1-4835-b44d-62a76550767c','ORD-1774994432091',3000.00,0.00,'bac leiu, bac lieu, hcm',0.00,'vietqr','pending',NULL,'2026-03-31 22:00:32','2026-03-31 22:00:32','90e69e25-1080-454c-94fd-f4be12f63314','le toan','37.vantoan@gmail.com','0946020447'),('c10b2f08-e07d-4ab2-85ed-c215893f4c23','ORD-1774993077310',1000.00,0.00,'bac leiu, bac lieu, hcm',0.00,'vietqr','pending',NULL,'2026-03-31 21:37:57','2026-03-31 21:37:57','90e69e25-1080-454c-94fd-f4be12f63314','le toan','37.vantoan@gmail.com','0946020447');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `rating` int NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `helpfulCount` int DEFAULT '0',
  `status` enum('approved','pending','rejected') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `createdAt` datetime NOT NULL,
  `updatedAt` datetime NOT NULL,
  `userId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `bookId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`),
  KEY `bookId` (`bookId`),
  CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`bookId`) REFERENCES `books` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `reviews_ibfk_3` FOREIGN KEY (`userId`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `reviews_ibfk_4` FOREIGN KEY (`bookId`) REFERENCES `books` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `reviews_ibfk_5` FOREIGN KEY (`userId`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `reviews_ibfk_6` FOREIGN KEY (`bookId`) REFERENCES `books` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reviews`
--

LOCK TABLES `reviews` WRITE;
/*!40000 ALTER TABLE `reviews` DISABLE KEYS */;
/*!40000 ALTER TABLE `reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `firstName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('user','admin','moderator') COLLATE utf8mb4_unicode_ci DEFAULT 'user',
  `status` enum('active','inactive','blocked') COLLATE utf8mb4_unicode_ci DEFAULT 'active',
  `googleId` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `githubId` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `lastLogin` datetime DEFAULT NULL,
  `resetToken` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `resetTokenExpire` datetime DEFAULT NULL,
  `createdAt` datetime NOT NULL,
  `updatedAt` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `email_2` (`email`),
  UNIQUE KEY `email_3` (`email`),
  UNIQUE KEY `email_4` (`email`),
  UNIQUE KEY `email_5` (`email`),
  UNIQUE KEY `email_6` (`email`),
  UNIQUE KEY `email_7` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('90e69e25-1080-454c-94fd-f4be12f63314','37.vantoan@gmail.com','$2a$10$IPvkUlHVP5P1HbRXagxs8eTZ06LqjIZ1t8Rabkd/mknHm2fbGLEq2','le','toan',NULL,NULL,NULL,'user','active',NULL,NULL,'2026-03-31 22:04:45',NULL,NULL,'2026-03-31 20:20:59','2026-03-31 22:04:45'),('b6509f27-46b4-4d11-8190-edf3d2e73edf','admin@bookweb.com','$2a$10$wBeVeVnu0w57MtnNpUeoieDFT3H4rg8Wh8MwvCI3dshqo.J3EdMPq','Admin','BookWeb','1900-2255-39',NULL,NULL,'admin','active',NULL,NULL,'2026-04-02 13:38:07',NULL,NULL,'2026-03-29 15:43:39','2026-04-02 13:38:07');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-02 20:48:52
