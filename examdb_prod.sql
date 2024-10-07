CREATE DATABASE  IF NOT EXISTS `examdb_prod` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `examdb_prod`;
-- MySQL dump 10.13  Distrib 8.0.32, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: examdb_prod
-- ------------------------------------------------------
-- Server version	8.0.32

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
-- Table structure for table `groups`
--

DROP TABLE IF EXISTS `groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `groups` (
  `id` int NOT NULL AUTO_INCREMENT,
  `group_name` varchar(100) NOT NULL,
  `user_id` int NOT NULL,
  `duration` int NOT NULL,
  `min_users` int NOT NULL,
  `max_users` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_name` (`group_name`,`user_id`),
  KEY `user_fk_idx` (`user_id`),
  CONSTRAINT `user_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `groups`
--

LOCK TABLES `groups` WRITE;
/*!40000 ALTER TABLE `groups` DISABLE KEYS */;
INSERT INTO `groups` VALUES (1,'Developers',1,60,3,5,'2024-05-17 17:44:33','2024-05-17 17:44:33'),(2,'Designers',2,30,2,4,'2024-05-17 17:44:33','2024-05-17 17:44:33'),(3,'Managers',3,45,4,6,'2024-05-17 17:44:33','2024-05-17 17:44:33'),(4,'Marketing',4,90,3,5,'2024-05-17 17:44:33','2024-05-17 17:44:33'),(5,'Sales',5,120,2,3,'2024-05-17 17:44:33','2024-05-17 17:44:33'),(6,'HR',6,60,3,5,'2024-05-17 17:44:33','2024-05-17 17:44:33'),(7,'Support',7,30,2,4,'2024-05-17 17:44:33','2024-05-17 17:44:33'),(8,'IT',8,45,4,6,'2024-05-17 17:44:33','2024-05-17 17:44:33'),(9,'Finance',9,90,3,5,'2024-05-17 17:44:33','2024-05-17 17:44:33'),(10,'Operations',10,120,2,3,'2024-05-17 17:44:33','2024-05-17 17:44:33'),(11,'Students',1,34,2,12,'2024-05-20 21:11:17','2024-05-20 21:11:17'),(12,'Group test 1',22,32,2,8,'2024-05-20 22:13:53','2024-05-20 22:13:53'),(13,'Teachers',1,92,3,12,'2024-05-21 09:08:31','2024-05-21 09:08:31'),(14,'Another group',1,4,4,8,'2024-05-21 09:58:27','2024-05-21 09:58:27'),(15,'Gruppo',1,5,2,5,'2024-05-21 10:07:22','2024-05-21 10:07:22'),(16,'Gruppo test',1,7,3,8,'2024-05-21 10:12:08','2024-05-21 10:12:08'),(17,'Developer',1,7,3,5,'2024-10-06 19:12:46','2024-10-06 19:12:46'),(18,'Designers',1,10,3,3,'2024-10-06 19:13:16','2024-10-06 19:13:16'),(19,'HR',1,43,4,8,'2024-10-06 19:13:44','2024-10-06 19:13:44'),(20,'Testers',1,10,4,8,'2024-10-06 19:16:35','2024-10-06 19:16:35'),(21,'Artists',1,4,4,6,'2024-10-06 19:22:25','2024-10-06 19:22:25');
/*!40000 ALTER TABLE `groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_groups`
--

DROP TABLE IF EXISTS `user_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_groups` (
  `user_id` int NOT NULL,
  `group_id` int NOT NULL,
  PRIMARY KEY (`user_id`,`group_id`),
  KEY `user_groups_group_id_fk_idx` (`group_id`),
  CONSTRAINT `user_groups_group_id_fk` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `user_groups_user_id_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_groups`
--

LOCK TABLES `user_groups` WRITE;
/*!40000 ALTER TABLE `user_groups` DISABLE KEYS */;
INSERT INTO `user_groups` VALUES (2,1),(3,1),(5,1),(2,2),(3,2),(4,2),(5,2),(3,3),(4,3),(5,3),(6,3),(7,3),(8,3),(4,4),(5,4),(6,4),(7,4),(8,4),(5,5),(6,5),(6,6),(7,6),(8,6),(9,6),(10,6),(7,7),(8,7),(9,7),(10,7),(8,8),(9,8),(10,8),(11,8),(12,8),(13,8),(9,9),(10,9),(11,9),(12,9),(13,9),(10,10),(11,10),(2,11),(3,11),(4,11),(6,11),(13,11),(15,11),(16,11),(20,11),(1,12),(5,12),(15,12),(17,12),(18,12),(1,13),(2,13),(4,13),(6,13),(10,13),(13,13),(17,13),(19,13),(2,14),(6,14),(8,14),(9,14),(21,14),(22,14),(1,15),(5,15),(10,15),(1,16),(4,16),(10,16),(13,16),(17,16),(1,17),(9,17),(16,17),(17,17),(18,17),(11,18),(22,18),(23,18),(3,19),(4,19),(8,19),(14,19),(16,19),(18,19),(6,20),(10,20),(14,20),(15,20),(6,21),(10,21),(14,21),(20,21);
/*!40000 ALTER TABLE `user_groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varbinary(256) NOT NULL,
  `salt` varbinary(16) NOT NULL,
  `name` varchar(45) NOT NULL,
  `surname` varchar(45) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'john_doe','john.doe@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','John','Doe','2024-05-17 17:44:33','2024-05-20 23:36:28'),(2,'jane_smith','jane.smith@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Jane','Smith','2024-05-17 17:44:33','2024-05-20 23:36:28'),(3,'alice_johnson','alice.johnson@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Alice','Johnson','2024-05-17 17:44:33','2024-05-20 23:36:28'),(4,'bob_brown','bob.brown@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Bob','Brown','2024-05-17 17:44:33','2024-05-20 23:36:28'),(5,'charlie_davis','charlie.davis@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Charlie','Davis','2024-05-17 17:44:33','2024-05-20 23:36:28'),(6,'david_miller','david.miller@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','David','Miller','2024-05-17 17:44:33','2024-05-20 23:36:28'),(7,'eve_wilson','eve.wilson@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Eve','Wilson','2024-05-17 17:44:33','2024-05-20 23:36:28'),(8,'frank_moore','frank.moore@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Frank','Moore','2024-05-17 17:44:33','2024-05-20 23:36:28'),(9,'grace_taylor','grace.taylor@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Grace','Taylor','2024-05-17 17:44:33','2024-05-20 23:36:28'),(10,'hank_anderson','hank.anderson@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Hank','Anderson','2024-05-17 17:44:33','2024-05-20 23:36:28'),(11,'isabella_jones','isabella.jones@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Isabella','Jones','2024-05-17 17:44:33','2024-05-20 23:36:28'),(12,'jack_white','jack.white@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Jack','White','2024-05-17 17:44:33','2024-05-20 23:36:28'),(13,'lily_green','lily.green@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Lily','Green','2024-05-17 17:44:33','2024-05-20 23:36:28'),(14,'michael_black','michael.black@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Michael','Black','2024-05-17 17:44:33','2024-05-20 23:36:28'),(15,'nancy_king','nancy.king@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Nancy','King','2024-05-17 17:44:33','2024-05-20 23:36:28'),(16,'oscar_clark','oscar.clark@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Oscar','Clark','2024-05-17 17:44:33','2024-05-20 23:36:28'),(17,'patricia_lee','patricia.lee@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Patricia','Lee','2024-05-17 17:44:33','2024-05-20 23:36:28'),(18,'quentin_hall','quentin.hall@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Quentin','Hall','2024-05-17 17:44:33','2024-05-20 23:36:28'),(19,'rachel_scott','rachel.scott@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Rachel','Scott','2024-05-17 17:44:33','2024-05-20 23:36:28'),(20,'steven_adams','steven.adams@example.com',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Steven','Adams','2024-05-17 17:44:33','2024-05-20 23:36:28'),(21,'<script>alert(\"cuai\")</script>','asd@asd.asd',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','test','test2','2024-05-18 10:39:22','2024-05-20 23:36:28'),(22,'Physer','salim@salici.it',_binary '55E\ÀCÑ\ÔΩ$\≈\ﬁ\ﬂ˚?SkOÒd=+≠JKb™X“∞',_binary '\Í\ÎÙ±ı\√h\0RîØ\‚]','Salim','Salici','2024-05-20 22:12:58',NULL),(23,'carlo_verdi','carlo@verdi.it',_binary 'ë\…\÷J?3Æ|úsNl7¡\0º\”\Ìó\ÌV∂3:ºêmDt†',_binary 'ù˜rôπ_≥ùÖèÒ`','Carlo','Verdi','2024-05-20 23:46:20',NULL);
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

-- Dump completed on 2024-10-06 19:37:19
