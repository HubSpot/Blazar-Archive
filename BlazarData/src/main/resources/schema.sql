--liquibase formatted sql

--changeset tpetr:1 dbms:mysql
CREATE TABLE `branches_v2` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `host` varchar(250) NOT NULL,
  `organization` varchar(250) NOT NULL,
  `repository` varchar(250) NOT NULL,
  `repositoryId` int(11) unsigned NOT NULL,
  `branch` varchar(250) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `pendingBuildId` bigint(20) unsigned,
  `inProgressBuildId` bigint(20) unsigned,
  `lastBuildId` bigint(20) unsigned,
  `createdTimestamp` bigint(20) unsigned NOT NULL,
  `updatedTimestamp` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX (`repositoryId`, `branch`),
  INDEX (`updatedTimestamp`)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8 DEFAULT CHARSET=utf8;

CREATE TABLE `modules_v2` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `branchId` int(11) unsigned NOT NULL,
  `name` varchar(250) NOT NULL,
  `type` varchar(250) NOT NULL,
  `path` varchar(250) NOT NULL,
  `glob` varchar(250) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `pendingBuildId` bigint(20) unsigned,
  `inProgressBuildId` bigint(20) unsigned,
  `lastBuildId` bigint(20) unsigned,
  `createdTimestamp` bigint(20) unsigned NOT NULL,
  `updatedTimestamp` bigint(20) unsigned NOT NULL,
  `buildpack` mediumtext,
  PRIMARY KEY (`id`),
  UNIQUE INDEX (`branchId`, `name`, `type`),
  INDEX (`updatedTimestamp`)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8 DEFAULT CHARSET=utf8;

CREATE TABLE `module_provides_v2` (
  `moduleId` int(11) unsigned NOT NULL,
  `name` varchar(250) NOT NULL,
  UNIQUE INDEX (`moduleId`, `name`),
  INDEX (`name`)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8 DEFAULT CHARSET=utf8;

CREATE TABLE `module_depends_v2` (
  `moduleId` int(11) unsigned NOT NULL,
  `name` varchar(250) NOT NULL,
  UNIQUE INDEX (`moduleId`, `name`),
  INDEX (`name`)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8 DEFAULT CHARSET=utf8;

CREATE TABLE `repo_builds_v2` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `branchId` int(11) unsigned NOT NULL,
  `buildNumber` int(11) unsigned NOT NULL,
  `state` varchar(40) NOT NULL,
  `startTimestamp` bigint(20) unsigned,
  `endTimestamp` bigint(20) unsigned,
  `commitInfo` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `dependencyGraph` mediumtext,
  `sha` char(40),
  PRIMARY KEY (`id`),
  UNIQUE INDEX (`branchId`, `buildNumber`)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8 DEFAULT CHARSET=utf8;

CREATE TABLE `module_builds_v2` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `moduleId` int(11) unsigned NOT NULL,
  `repoBuildId` bigint(20) unsigned NOT NULL,
  `buildNumber` int(11) unsigned NOT NULL,
  `state` varchar(40) NOT NULL,
  `startTimestamp` bigint(20) unsigned,
  `endTimestamp` bigint(20) unsigned,
  `buildConfig` mediumtext,
  `resolvedConfig` mediumtext,
  `runId` varchar(500),
  `taskId` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE INDEX (`moduleId`, `buildNumber`),
  INDEX (`repoBuildId`)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8 DEFAULT CHARSET=utf8;
