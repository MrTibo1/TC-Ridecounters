CREATE TABLE IF NOT EXISTS `players` (
    `uuid` char(36) NOT NULL,
    `username` varchar(16) NOT NULL,
    `hidden` bit(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`uuid`)
);

CREATE TABLE IF NOT EXISTS `rides` (
    `id` varchar(16) NOT NULL,
    `name` varchar(128) NOT NULL,
    `altname` varchar(128) DEFAULT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `ridecount_data` (
    `ride_id` varchar(16) NOT NULL,
    `player_uuid` char(36) NOT NULL,
    `timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
    KEY `ride_id` (`ride_id`),
    KEY `player_uuid` (`player_uuid`),
    CONSTRAINT `FK_ridecount_data_players` FOREIGN KEY (`player_uuid`) REFERENCES `players` (`uuid`) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT `FK_ridecount_data_rides` FOREIGN KEY (`ride_id`) REFERENCES `rides` (`id`) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `ridecount_total` (
    `ride_id` varchar(16) NOT NULL,
    `player_uuid` char(36) NOT NULL,
    `total` int(11) NOT NULL DEFAULT 1,
    PRIMARY KEY (`ride_id`,`player_uuid`),
    KEY `FK_ridecount_total_players` (`player_uuid`),
    CONSTRAINT `FK_ridecount_total_players` FOREIGN KEY (`player_uuid`) REFERENCES `players` (`uuid`) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT `FK_ridecount_total_rides` FOREIGN KEY (`ride_id`) REFERENCES `rides` (`id`) ON UPDATE CASCADE ON DELETE CASCADE
);
