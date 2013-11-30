-- phpMyAdmin SQL Dump
-- version 3.4.11.1deb2
-- http://www.phpmyadmin.net
--
-- Client: localhost
-- Généré le: Mer 27 Novembre 2013 à 22:46
-- Version du serveur: 5.5.31
-- Version de PHP: 5.4.4-14+deb7u5

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de données: `mylife_home`
--
CREATE DATABASE `mylife_home` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `mylife_home`;

GRANT USAGE ON *.* TO 'mylife'@'%' IDENTIFIED BY PASSWORD 'mylife';

GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE, SHOW VIEW ON `mylife_home`.* TO 'mylife'@'%';

-- --------------------------------------------------------

--
-- Structure de la table `core_configuration`
--

CREATE TABLE IF NOT EXISTS `core_configuration` (
  `conf_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `conf_type` varchar(100) NOT NULL,
  `conf_content` longblob NOT NULL,
  `conf_active` tinyint(1) NOT NULL,
  `conf_date` datetime NOT NULL,
  `conf_comment` longtext NOT NULL,
  PRIMARY KEY (`conf_id`),
  KEY `conf_type` (`conf_type`,`conf_active`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=13 ;

-- --------------------------------------------------------

--
-- Structure de la table `core_plugin`
--

CREATE TABLE IF NOT EXISTS `core_plugin` (
  `plugin_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `plugin_name` varchar(100) NOT NULL,
  `plugin_content` longblob NOT NULL,
  `plugin_active` tinyint(1) NOT NULL,
  `plugin_date` datetime NOT NULL,
  `plugin_comment` longtext NOT NULL,
  PRIMARY KEY (`plugin_id`),
  KEY `plugin_active` (`plugin_active`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Structure de la table `core_plugin_persistance`
--

CREATE TABLE IF NOT EXISTS `core_plugin_persistance` (
  `ppers_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `ppers_component_id` varchar(255) NOT NULL,
  `ppers_key` varchar(255) NOT NULL,
  `ppers_value` varchar(255) NOT NULL,
  PRIMARY KEY (`ppers_id`),
  KEY `ppers_object_id` (`ppers_component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Structure de la table `net_link`
--

CREATE TABLE IF NOT EXISTS `net_link` (
  `link_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `link_name` varchar(255) NOT NULL,
  `link_address` varchar(255) NOT NULL,
  `link_port` int(11) NOT NULL,
  PRIMARY KEY (`link_id`),
  UNIQUE KEY `link_name` (`link_name`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=4 ;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
