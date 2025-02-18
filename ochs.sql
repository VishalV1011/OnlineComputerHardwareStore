-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 14, 2025 at 08:26 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `ochs`
--

-- --------------------------------------------------------

--
-- Table structure for table `cart`
--

CREATE TABLE `cart` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT 1,
  `price` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `items`
--

CREATE TABLE `items` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `stock` int(11) NOT NULL DEFAULT 0,
  `price` decimal(10,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `items`
--

INSERT INTO `items` (`id`, `name`, `stock`, `price`) VALUES
(6, 'RTX 5070', 199, 1200.00),
(7, 'Skibidi TV', 11, 12003.00);

-- --------------------------------------------------------

--
-- Table structure for table `payments`
--

CREATE TABLE `payments` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `name` varchar(100) NOT NULL,
  `address` varchar(255) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_date` datetime NOT NULL DEFAULT current_timestamp(),
  `status` enum('Completed','Pending') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payments`
--

INSERT INTO `payments` (`id`, `username`, `name`, `address`, `amount`, `payment_date`, `status`) VALUES
(1, 'shreethar2610', '', '', 8400.00, '2025-01-14 07:10:18', 'Completed'),
(2, 'dinesh', '', '', 39200.00, '2025-01-14 00:00:00', 'Completed'),
(3, 'dinesh', '', '', 11200.00, '2025-01-14 00:00:00', 'Completed'),
(4, 'dinesh', '', '', 5600.00, '2025-01-14 00:00:00', 'Completed'),
(5, 'dinesh', 'Dinesh Ramakrishnan', 'SJ-02-11, UTe, Durian Tunggal, Melaka', 2800.00, '2025-01-14 00:00:00', 'Completed');

--
-- Triggers `payments`
--
DELIMITER $$
CREATE TRIGGER `before_payment_delete` BEFORE DELETE ON `payments` FOR EACH ROW BEGIN
    INSERT INTO `payments_archive` (`payment_id`, `username`, `amount`, `payment_date`)
    VALUES (OLD.id, OLD.username, OLD.amount, OLD.payment_date);
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `payments_archive`
--

CREATE TABLE `payments_archive` (
  `id` int(11) NOT NULL,
  `payment_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_date` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payments_archive`
--

INSERT INTO `payments_archive` (`id`, `payment_id`, `username`, `amount`, `payment_date`) VALUES
(1, 8, 'vishal', 12003.00, '2025-01-14 00:00:00');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `username` varchar(50) NOT NULL,
  `password` varchar(256) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `balance` float(10,2) NOT NULL,
  `role` enum('customer','staff') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`username`, `password`, `name`, `address`, `balance`, `role`) VALUES
('dinesh', 'dinesh123', 'Dinesh Ramakrishnan', 'SJ-02-11, UTe, Durian Tunggal, Melaka', 41200.00, 'customer'),
('pravinthiran', '1234abcd', NULL, NULL, 0.00, 'customer'),
('root', 'RootUser!234', NULL, NULL, 0.00, 'staff'),
('shreethar2610', 'abcd1234', NULL, NULL, 1600.00, 'customer'),
('vishal', 'v', 'vishalv', 'utem', 0.00, 'customer');

--
-- Triggers `users`
--
DELIMITER $$
CREATE TRIGGER `before_user_delete` BEFORE DELETE ON `users` FOR EACH ROW BEGIN
  -- Insert the payment details into payments_archive
  INSERT INTO `payments_archive` (`payment_id`, `username`, `amount`, `payment_date`)
  SELECT 
    `id`, `username`, `amount`, `payment_date`
  FROM 
    `payments`
  WHERE 
    `username` = OLD.username;

  -- Optionally: The deletion of payments will already occur automatically due to ON DELETE CASCADE
  -- on the foreign key constraint in the payments table, so no need to delete manually here.
END
$$
DELIMITER ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `cart`
--
ALTER TABLE `cart`
  ADD PRIMARY KEY (`id`),
  ADD KEY `username` (`username`),
  ADD KEY `item_id` (`item_id`);

--
-- Indexes for table `items`
--
ALTER TABLE `items`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `payments`
--
ALTER TABLE `payments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `username` (`username`);

--
-- Indexes for table `payments_archive`
--
ALTER TABLE `payments_archive`
  ADD PRIMARY KEY (`id`),
  ADD KEY `payment_id` (`payment_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `cart`
--
ALTER TABLE `cart`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `items`
--
ALTER TABLE `items`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `payments`
--
ALTER TABLE `payments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `payments_archive`
--
ALTER TABLE `payments_archive`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `cart`
--
ALTER TABLE `cart`
  ADD CONSTRAINT `cart_ibfk_1` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE CASCADE,
  ADD CONSTRAINT `cart_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `payments`
--
ALTER TABLE `payments`
  ADD CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
