CREATE TABLE `branches` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `host` varchar(250) NOT NULL,
  `organization` varchar(250) NOT NULL,
  `repository` varchar(250) NOT NULL,
  `repositoryId` int(11) unsigned NOT NULL,
  `branch` varchar(250) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE INDEX (`repositoryId`, `branch`)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8 DEFAULT CHARSET=utf8;

CREATE TABLE `modules` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `branchId` int(11) unsigned NOT NULL,
  `name` varchar(250) NOT NULL,
  `path` varchar(250) NOT NULL,
  `glob` varchar(250) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `lastBuild` bigint(20) unsigned,
  `inProgressBuild` bigint(20) unsigned,
  PRIMARY KEY (`id`),
  UNIQUE INDEX (`branchId`, `name`)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8 DEFAULT CHARSET=utf8;

CREATE TABLE `builds` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `moduleId` int(11) unsigned NOT NULL,
  `buildNumber` int(11) unsigned NOT NULL,
  `state` varchar(40) NOT NULL,
  `startTimestamp` bigint(20) unsigned NOT NULL,
  `endTimestamp` bigint(20) unsigned,
  `sha` char(40) NOT NULL,
  `log` varchar(250),
  PRIMARY KEY (`id`),
  UNIQUE INDEX (`moduleId`, `buildNumber`)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8 DEFAULT CHARSET=utf8;
