CREATE DATABASE  IF NOT EXISTS `bookweb` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `bookweb`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: bookweb
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
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
INSERT INTO `books` VALUES ('23a4343d-cb1f-4b23-9b0d-352781001e62','O Zahir','o-zahir','Cuộc tìm kiếm sự tự do và ý nghĩa tình yêu của một nhà văn nổi tiếng.','9786045635249',120000.00,10.00,'/uploads/books/1775136012110_2.jpg',NULL,NULL,350,45,0.00,1,'active','2026-03-31 22:14:44','2026-04-02 13:20:12','859e1c7c-1ab7-4b83-977d-80abcb495931','ab1b6fe1-b067-4064-9c65-28de21c321de'),('2a43a5ba-d92e-4073-884b-42326d621a75','Vũ Trụ Trong Vỏ Hạt Dẻ','v-tr-trong-v-ht-d','Những khám phá mới nhất về vật lý lý thuyết và cơ học lượng tử.','9786042123470',160000.00,12.00,'/uploads/books/1775136186859_12.webp',NULL,NULL,215,55,0.00,4,'active','2026-03-31 22:25:38','2026-04-06 17:10:30','e1242b41-4fa0-435d-8347-5a6f51942fdd','218d2996-6efc-4f80-a766-8d198d3504e0'),('2a8bec0f-5b3e-482b-ae0b-dd5b04bb3af5','Lược Sử Thời Gian','lc-s-thi-gian','Khám phá những câu hỏi lớn nhất về vũ trụ và nguồn gốc vạn vật.','9786042123456',3000.00,0.00,'/uploads/books/1775136266299_7.jpg',NULL,NULL,285,75,0.00,2,'active','2026-03-31 22:23:39','2026-04-02 13:25:56','e1242b41-4fa0-435d-8347-5a6f51942fdd','218d2996-6efc-4f80-a766-8d198d3504e0'),('2c0e8475-2b19-4b5c-8b6a-10f923442b60','Lợi Thế Ngôn Từ','li-th-ngn-t','Cách sử dụng ngôn ngữ để tạo ấn tượng và thuyết phục người đối diện.','9786045880557',72000.00,10.00,'/uploads/books/1775136123410_6.webp',NULL,NULL,280,85,0.00,1,'active','2026-03-31 22:19:52','2026-04-02 13:22:03','847373d4-d40b-4640-8f6e-6cd2d9578e39','4bebdf8c-b2b4-439b-8a4b-f5fbbf31fe47'),('4f1a86c9-13f7-48e3-a51e-45f516ee1a37','Bản Thiết Kế Vĩ Đại','bn-thit-k-v-i','Cách nhìn mới về lý thuyết vạn vật và sự hình thành vũ trụ.','9786042123463',145000.00,5.00,'/uploads/books/1775136177205_11.jpg',NULL,NULL,230,40,0.00,1,'active','2026-03-31 22:24:35','2026-04-02 13:22:57','e1242b41-4fa0-435d-8347-5a6f51942fdd','218d2996-6efc-4f80-a766-8d198d3504e0'),('6a94ee01-c5c3-4b6e-aea4-97f41ec411c8','Bay Đêm','bay-m','Sự dũng cảm và trách nhiệm của những phi công tiên phong.','9786042087678',3000.00,0.00,'/uploads/books/1775136242565_15.webp',NULL,NULL,180,23,4.00,21,'active','2026-03-31 22:27:57','2026-04-06 17:04:13','8c2557d7-8fb1-4a5b-90bd-03ad931f496c','f03d9551-c1d8-4164-9ee4-c97d89cfa81d'),('79773a03-8bcf-48d0-8180-b137e3443482','Nhà Giả Kim','nh-gi-kim','Câu chuyện về hành trình theo đuổi vận mệnh của chàng chăn cừu Santiago.','9786045635232',79000.00,20.00,'/uploads/books/1774995199517_1.jpg',NULL,NULL,228,150,0.00,2,'active','2026-03-31 22:13:19','2026-04-02 13:19:12','859e1c7c-1ab7-4b83-977d-80abcb495931','ab1b6fe1-b067-4064-9c65-28de21c321de'),('848ddcdc-ea07-4e4f-a8f1-bb35b3e1e731','Hoàng Tử Bé','hong-t-b','Cuộc du hành của một vị hoàng tử nhỏ qua các hành tinh khác nhau.','9786042087654',65000.00,20.00,'/uploads/books/1775135975801_1.jpg',NULL,NULL,120,500,0.00,6,'active','2026-03-31 22:26:15','2026-04-06 15:44:56','8c2557d7-8fb1-4a5b-90bd-03ad931f496c','f03d9551-c1d8-4164-9ee4-c97d89cfa81d'),('912a06f0-d425-4067-a412-2dfe0820eb75','Ngoại tình','ngoi-tnh','Những suy tư về hạnh phúc và sự nhàm chán trong hôn nhân hiện đại.','9786045635256',3000.00,0.00,'/uploads/books/1775136038874_3.webp',NULL,NULL,312,60,0.00,2,'active','2026-03-31 22:16:02','2026-04-02 13:26:08','859e1c7c-1ab7-4b83-977d-80abcb495931','ab1b6fe1-b067-4064-9c65-28de21c321de'),('9f33ac67-296f-44c0-aaa2-e27401ea6ab0','Doanh Nghiệp Thế Kỷ 21','doanh-nghip-th-k-21','Tầm nhìn về mô hình kinh doanh mạng lưới trong thời đại mới.','9786043350147',85000.00,30.00,'/uploads/books/1775136157034_9.webp',NULL,NULL,250,140,0.00,1,'active','2026-03-31 22:22:54','2026-04-02 13:22:37','1e1e638a-62de-4376-b707-90c46bb59cb9','e1af0ece-e956-49e1-9c74-242c52f2e8d9'),('a3b0b491-370d-4f96-887a-31d62dd68eec','Cha Giàu Cha Nghèo','cha-giu-cha-ngho','Bài học về tư duy tài chính và cách làm giàu từ hai người cha.','9786043350123',98000.00,15.00,'/uploads/books/1775136308713_10.jpg',NULL,NULL,344,210,0.00,1,'active','2026-03-31 22:20:46','2026-04-02 13:25:08','1e1e638a-62de-4376-b707-90c46bb59cb9','e1af0ece-e956-49e1-9c74-242c52f2e8d9'),('b7235133-0394-46e4-8e8c-591f4114622f','Đắc Nhân Tâm','c-nhn-tm','Cuốn sách kinh điển về nghệ thuật giao tiếp và thu phục lòng người.','9786045880533',86000.00,25.00,'/uploads/books/1775136056995_4.webp',NULL,NULL,320,300,0.00,1,'active','2026-03-31 22:17:19','2026-04-02 13:20:57','847373d4-d40b-4640-8f6e-6cd2d9578e39','4bebdf8c-b2b4-439b-8a4b-f5fbbf31fe47'),('cd4bbde0-6c9c-4d1e-ab59-73fe9f484951','Cõi Người Ta','ci-ngi-ta','Những hồi ức về những chuyến bay đầy mạo hiểm và triết lý sống.','9786042087661',85000.00,15.00,'/uploads/books/1775136232574_14.jpg',NULL,NULL,240,25,4.00,27,'active','2026-03-31 22:27:13','2026-04-06 17:10:03','8c2557d7-8fb1-4a5b-90bd-03ad931f496c','f03d9551-c1d8-4164-9ee4-c97d89cfa81d'),('d581a885-b029-48c9-878c-826959b6e78f','Quẳng Gánh Lo Đi Và Vui Sống','qung-gnh-lo-i-v-vui-sng','Phương pháp để loại bỏ lo lắng và tận hưởng cuộc sống mỗi ngày.','9786045880540',78000.00,20.00,'/uploads/books/1775136087009_5.webp',NULL,NULL,300,120,0.00,2,'active','2026-03-31 22:18:37','2026-04-02 13:21:27','847373d4-d40b-4640-8f6e-6cd2d9578e39','4bebdf8c-b2b4-439b-8a4b-f5fbbf31fe47'),('fe574cea-b5cd-4e66-ad0a-2bf80758d2e3','Dạy Con Làm Giàu - Tập 2','dy-con-lm-giu-tp-2','Sử dụng kim tứ đồ để hiểu về các nguồn thu nhập khác nhau.','9786043350130',115000.00,10.00,'/uploads/books/1775136148014_8.webp',NULL,NULL,420,95,0.00,1,'active','2026-03-31 22:21:55','2026-04-02 13:22:28','1e1e638a-62de-4376-b707-90c46bb59cb9','e1af0ece-e956-49e1-9c74-242c52f2e8d9');
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
INSERT INTO `categories` VALUES ('1e1e638a-62de-4376-b707-90c46bb59cb9','Kinh tế & Quản trị','kinh-t-qun-tr','','','active','2026-03-31 22:11:05','2026-04-06 14:56:50'),('847373d4-d40b-4640-8f6e-6cd2d9578e39','Kỹ năng sống','k-nng-sng','',NULL,'active','2026-03-31 22:10:57','2026-03-31 22:10:57'),('859e1c7c-1ab7-4b83-977d-80abcb495931','Văn học Kinh điển','vn-hc-kinh-in','',NULL,'active','2026-03-31 22:10:42','2026-03-31 22:10:42'),('8c2557d7-8fb1-4a5b-90bd-03ad931f496c','Thiếu nhi','thiu-nhi','',NULL,'active','2026-03-31 22:11:17','2026-03-31 22:11:17'),('e1242b41-4fa0-435d-8347-5a6f51942fdd','Khoa học & Vũ trụ','khoa-hc-v-tr','',NULL,'active','2026-03-31 22:11:12','2026-03-31 22:11:12');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `favorites`
--

DROP TABLE IF EXISTS `favorites`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorites` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `userId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `bookId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `createdAt` datetime NOT NULL,
  `updatedAt` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `favorites_user_id_book_id` (`userId`,`bookId`),
  KEY `bookId` (`bookId`),
  CONSTRAINT `favorites_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `favorites_ibfk_2` FOREIGN KEY (`bookId`) REFERENCES `books` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorites`
--

LOCK TABLES `favorites` WRITE;
/*!40000 ALTER TABLE `favorites` DISABLE KEYS */;
/*!40000 ALTER TABLE `favorites` ENABLE KEYS */;
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
INSERT INTO `orderitems` VALUES ('27dc4ccb-0512-453c-b45c-5a533ea34cbf',1,85000.00,0.15,'2026-04-06 15:55:07','2026-04-06 15:55:07','a3a49b5f-ddec-4ec3-9399-1cd69d6d7e9e','cd4bbde0-6c9c-4d1e-ab59-73fe9f484951'),('302efae5-b585-4656-ab57-4a7f8d95ae23',1,85000.00,0.15,'2026-04-06 15:48:55','2026-04-06 15:48:55','8898f68d-92cc-423e-a784-594973587730','cd4bbde0-6c9c-4d1e-ab59-73fe9f484951'),('7ee2c7f9-fdb3-4d1d-a2e7-adac3a72b577',1,3000.00,0.00,'2026-04-06 17:04:13','2026-04-06 17:04:13','c33359cd-e22e-49db-9a25-a0ee5c971298','6a94ee01-c5c3-4b6e-aea4-97f41ec411c8'),('8cfd7925-a98b-4d1f-bf23-1099cc4df23d',1,85000.00,0.15,'2026-04-06 15:55:38','2026-04-06 15:55:38','b6537b31-0f81-4cdb-8494-9789343e65c6','cd4bbde0-6c9c-4d1e-ab59-73fe9f484951'),('a6d234e4-6000-42a3-b8e3-ac11d4fd078e',1,3000.00,0.00,'2026-04-06 14:50:27','2026-04-06 14:50:27','05a52bab-3f31-4cd7-a952-a19de559acd1','6a94ee01-c5c3-4b6e-aea4-97f41ec411c8'),('e0f5fddd-3604-4db8-a466-7683988601c9',1,85000.00,0.15,'2026-04-06 17:10:03','2026-04-06 17:10:03','6ed5c953-6ba5-4cad-86da-0fda22ab5587','cd4bbde0-6c9c-4d1e-ab59-73fe9f484951'),('e38d2f78-9c7e-449f-bd9c-ff9cbbdedd85',1,85000.00,0.15,'2026-04-06 15:32:39','2026-04-06 15:32:39','0e458539-5053-443f-b859-c33d73955019','cd4bbde0-6c9c-4d1e-ab59-73fe9f484951');
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
  `status` enum('pending','processing','shipped','delivered','cancelled','confirmed') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `notes` text COLLATE utf8mb4_unicode_ci,
  `createdAt` datetime NOT NULL,
  `updatedAt` datetime NOT NULL,
  `userId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `customerName` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `customerEmail` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `customerPhone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reviewed` tinyint(1) NOT NULL DEFAULT '0',
  `voucherCode` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `voucherDiscount` decimal(10,2) NOT NULL DEFAULT '0.00',
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
INSERT INTO `orders` VALUES ('05a52bab-3f31-4cd7-a952-a19de559acd1','ORD-1775487027634',3000.00,0.00,'thu duc, HCM, hcm',0.00,'cash_on_delivery','confirmed',NULL,'2026-04-06 14:50:27','2026-04-06 15:29:45','64cadbdf-7108-490b-8d48-933cffcd76fb','lam phu','toanle251004@gmail.com','0946020447',1,NULL,0.00),('0e458539-5053-443f-b859-c33d73955019','ORD-1775489559200',85000.00,12750.00,'thu duc, HCM, hcm',0.00,'cash_on_delivery','confirmed',NULL,'2026-04-06 15:32:39','2026-04-06 15:43:07','64cadbdf-7108-490b-8d48-933cffcd76fb','lam phu','toanle251004@gmail.com','0946020447',1,NULL,0.00),('6ed5c953-6ba5-4cad-86da-0fda22ab5587','ORD-1775495403555',72250.00,12750.00,'thu duc, HCM, hcm',0.00,'cash_on_delivery','pending',NULL,'2026-04-06 17:10:03','2026-04-06 17:10:03','64cadbdf-7108-490b-8d48-933cffcd76fb','lam phu','toanle251004@gmail.com','0946020447',0,NULL,0.00),('8898f68d-92cc-423e-a784-594973587730','ORD-1775490535347',85000.00,12750.00,'thu duc, HCM, La Xuan Oai',0.00,'cash_on_delivery','confirmed',NULL,'2026-04-06 15:48:55','2026-04-06 15:49:29','64cadbdf-7108-490b-8d48-933cffcd76fb','lam phu','toanle251004@gmail.com','0946020447',1,NULL,0.00),('a3a49b5f-ddec-4ec3-9399-1cd69d6d7e9e','ORD-1775490907420',85000.00,12750.00,'thu duc, HCM, hcm',0.00,'cash_on_delivery','confirmed',NULL,'2026-04-06 15:55:07','2026-04-06 15:56:13','64cadbdf-7108-490b-8d48-933cffcd76fb','lam phu','toanle251004@gmail.com','0946020447',1,NULL,0.00),('b6537b31-0f81-4cdb-8494-9789343e65c6','ORD-1775490938920',85000.00,12750.00,'thu duc, HCM, hcm',0.00,'cash_on_delivery','pending',NULL,'2026-04-06 15:55:38','2026-04-06 15:55:38','64cadbdf-7108-490b-8d48-933cffcd76fb','lam phu','toanle251004@gmail.com','0946020447',0,NULL,0.00),('c33359cd-e22e-49db-9a25-a0ee5c971298','ORD-1775495053855',2850.00,0.00,'thu duc, HCM, hcm',0.00,'cash_on_delivery','pending',NULL,'2026-04-06 17:04:13','2026-04-06 17:04:13','64cadbdf-7108-490b-8d48-933cffcd76fb','lam phu','toanle251004@gmail.com','0946020447',0,'GIAMGIA',150.00);
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
  `imageUrl` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
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
INSERT INTO `reviews` VALUES ('4f608fdd-9af7-4ea5-a6fc-cb9535fbd3d7',5,'sách','hay',0,'approved','2026-04-06 15:43:07','2026-04-06 15:43:07','64cadbdf-7108-490b-8d48-933cffcd76fb','cd4bbde0-6c9c-4d1e-ab59-73fe9f484951',NULL),('52c4108c-0422-418d-8d04-da41fea126be',3,'hehe','hehe',0,'approved','2026-04-06 15:56:13','2026-04-06 15:59:45','64cadbdf-7108-490b-8d48-933cffcd76fb','cd4bbde0-6c9c-4d1e-ab59-73fe9f484951','/uploads/reviews/1775490973007_3.jpg'),('cd40d987-1a40-400b-931a-d823fb4c3dfa',4,'hihi','hehe',0,'approved','2026-04-06 15:49:29','2026-04-06 15:49:29','64cadbdf-7108-490b-8d48-933cffcd76fb','cd4bbde0-6c9c-4d1e-ab59-73fe9f484951',NULL);
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
INSERT INTO `users` VALUES ('64cadbdf-7108-490b-8d48-933cffcd76fb','toanle251004@gmail.com','$2a$10$aJ3yPnEaaUrJkwR6RPcmN.Ijm5dQ.Wkv0qEltfvyAT05Zh3TBgOQe','lam','phu',NULL,NULL,NULL,'user','active',NULL,NULL,'2026-04-06 17:03:55',NULL,NULL,'2026-04-06 14:49:09','2026-04-06 17:03:55'),('b6509f27-46b4-4d11-8190-edf3d2e73edf','admin@bookweb.com','$2a$10$wBeVeVnu0w57MtnNpUeoieDFT3H4rg8Wh8MwvCI3dshqo.J3EdMPq','Admin','BookWeb','1900-2255-39',NULL,NULL,'admin','active',NULL,NULL,'2026-04-06 14:54:23',NULL,NULL,'2026-03-29 15:43:39','2026-04-06 14:54:23');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vouchers`
--

DROP TABLE IF EXISTS `vouchers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vouchers` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `type` enum('percent','fixed') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'percent',
  `value` decimal(10,2) NOT NULL,
  `minOrderValue` decimal(10,2) NOT NULL DEFAULT '0.00',
  `maxDiscount` decimal(10,2) DEFAULT NULL,
  `totalUsageLimit` int DEFAULT NULL,
  `usedCount` int NOT NULL DEFAULT '0',
  `perUserLimit` int NOT NULL DEFAULT '1',
  `startDate` datetime NOT NULL,
  `endDate` datetime NOT NULL,
  `isActive` tinyint(1) NOT NULL DEFAULT '1',
  `createdAt` datetime NOT NULL,
  `updatedAt` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vouchers`
--

LOCK TABLES `vouchers` WRITE;
/*!40000 ALTER TABLE `vouchers` DISABLE KEYS */;
INSERT INTO `vouchers` VALUES ('5ace1710-08f8-45fa-8884-ca6861119fd4','GIAMGIA','ma giam gia','giamgia','percent',5.00,0.00,50000.00,25,1,1,'2026-04-06 16:38:00','2026-04-30 16:39:00',1,'2026-04-06 16:39:11','2026-04-06 17:04:13');
/*!40000 ALTER TABLE `vouchers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `voucherusages`
--

DROP TABLE IF EXISTS `voucherusages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `voucherusages` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `discountAmount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `createdAt` datetime NOT NULL,
  `updatedAt` datetime NOT NULL,
  `voucherId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `userId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `orderId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `voucherId` (`voucherId`),
  KEY `userId` (`userId`),
  KEY `orderId` (`orderId`),
  CONSTRAINT `voucherusages_ibfk_1` FOREIGN KEY (`voucherId`) REFERENCES `vouchers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `voucherusages_ibfk_2` FOREIGN KEY (`userId`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `voucherusages_ibfk_3` FOREIGN KEY (`orderId`) REFERENCES `orders` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `voucherusages`
--

LOCK TABLES `voucherusages` WRITE;
/*!40000 ALTER TABLE `voucherusages` DISABLE KEYS */;
INSERT INTO `voucherusages` VALUES ('86583212-386e-4001-b625-c68279382d56',150.00,'2026-04-06 17:04:13','2026-04-06 17:04:13','5ace1710-08f8-45fa-8884-ca6861119fd4','64cadbdf-7108-490b-8d48-933cffcd76fb','c33359cd-e22e-49db-9a25-a0ee5c971298');
/*!40000 ALTER TABLE `voucherusages` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-07  0:47:34
