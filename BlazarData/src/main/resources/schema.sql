CREATE TABLE `build_definitions` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `host` varchar(250) NOT NULL,
  `organization` varchar(250) NOT NULL,
  `repository` varchar(250) NOT NULL,
  `branch` varchar(250) NOT NULL,
  `name` varchar(250) NOT NULL,
  `path` varchar(500) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8 DEFAULT CHARSET=utf8;
