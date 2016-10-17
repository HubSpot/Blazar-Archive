--liquibase formatted sql

--changeset tpetr:1

CREATE TABLE `branches` (
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
  `createdTimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedTimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX (`repositoryId`, `branch`),
  INDEX (`updatedTimestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `modules` (
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
  `createdTimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedTimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `buildpack` mediumtext,
  PRIMARY KEY (`id`),
  UNIQUE INDEX (`branchId`, `name`, `type`),
  INDEX (`updatedTimestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `module_provides` (
  `moduleId` int(11) unsigned NOT NULL,
  `name` varchar(250) NOT NULL,
  PRIMARY KEY (`moduleId`, `name`),
  INDEX (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `module_depends` (
  `moduleId` int(11) unsigned NOT NULL,
  `name` varchar(250) NOT NULL,
  PRIMARY KEY (`moduleId`, `name`),
  INDEX (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `repo_builds` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `branchId` int(11) unsigned NOT NULL,
  `buildNumber` int(11) unsigned NOT NULL,
  `state` varchar(40) NOT NULL,
  `buildTrigger` mediumtext NOT NULL,
  `buildOptions` mediumtext,
  `startTimestamp` bigint(20) unsigned,
  `endTimestamp` bigint(20) unsigned,
  `commitInfo` mediumtext,
  `dependencyGraph` mediumtext,
  `sha` char(40),
  PRIMARY KEY (`id`),
  UNIQUE INDEX (`branchId`, `buildNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `module_builds` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `moduleId` int(11) unsigned NOT NULL,
  `repoBuildId` bigint(20) unsigned NOT NULL,
  `buildNumber` int(11) unsigned NOT NULL,
  `state` varchar(40) NOT NULL,
  `startTimestamp` bigint(20) unsigned,
  `endTimestamp` bigint(20) unsigned,
  `buildConfig` mediumtext,
  `resolvedConfig` mediumtext,
  `taskId` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE INDEX (`moduleId`, `buildNumber`),
  INDEX (`repoBuildId`),
  INDEX (`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `malformed_files` (
  `branchId` int(11) unsigned NOT NULL,
  `type` varchar(250) NOT NULL,
  `path` varchar(250) NOT NULL,
  `details` mediumtext,
  PRIMARY KEY (`branchId`, `type`, `path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--changeset jhaber:2 dbms:mysql
ALTER TABLE `branches` ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;

ALTER TABLE `branches` MODIFY `branch` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `branches` MODIFY `updatedTimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE `modules` ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;

ALTER TABLE `modules` MODIFY `updatedTimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE `module_provides` ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;

ALTER TABLE `module_depends` ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;

ALTER TABLE `repo_builds` ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;

ALTER TABLE `repo_builds` MODIFY `commitInfo` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `repo_builds` MODIFY `buildOptions` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `module_builds` ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;

ALTER TABLE `malformed_files` ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;

--changeset jgoodwin:5 runAlways:false
CREATE TABLE `instant_message_configs` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `branchId` BIGINT(20) UNSIGNED NOT NULL,
  `moduleId` BIGINT(20) UNSIGNED,
  `channelName` varchar(21),
  `onFinish` TINYINT(1),
  `onFail` TINYINT(1),
  `onChange` TINYINT(1),
  `onRecover` TINYINT(1),
  `active` TINYINT(1),
  PRIMARY KEY (`id`),
  UNIQUE INDEX (`channelName`, `branchId`, `moduleId`),
  INDEX (`channelName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--changeset jgoodwin:6 runAlways:false
CREATE TABLE inter_project_builds (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `state` VARCHAR(40) NOT NULL,
  `moduleIds` MEDIUMTEXT NOT NULL,
  `buildTrigger` MEDIUMTEXT NOT NULL,
  `startTimestamp` BIGINT(20) UNSIGNED,
  `endTimestamp` BIGINT(20) UNSIGNED,
  `dependencyGraph` MEDIUMTEXT,
  PRIMARY KEY (`id`),
  INDEX(`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE inter_project_build_mappings (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `interProjectBuildId` BIGINT(20),
  `branchId` INT(11),
  `repoBuildId` BIGINT(20),
  `moduleId` INT(11),
  `moduleBuildId` BIGINT(20),
  `state` varchar(40) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX (`moduleBuildId`),
  INDEX (`interProjectBuildId`),
  INDEX (`repoBuildId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--changeset jgoodwin:7 runAlways:false
CREATE TABLE branch_settings (
  `branchId` INT(11),
  `triggerInterProjectBuilds` TINYINT(1),
  `interProjectBuildOptIn` TINYINT(1),
  PRIMARY KEY (`branchId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--changeset jgoodwin:8
ALTER TABLE `module_provides` ADD COLUMN `version` VARCHAR(190);
ALTER TABLE `module_depends` ADD COLUMN `version` VARCHAR(190);

--changeset jgoodwin:9 dbms:mysql
ALTER TABLE `module_provides` MODIFY  `version` VARCHAR(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `module_provides` MODIFY `name` VARCHAR(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `module_depends` MODIFY `version` VARCHAR(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `module_depends` MODIFY `name` VARCHAR(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `branch_settings` ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;
ALTER TABLE `instant_message_configs` ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;
ALTER TABLE `inter_project_build_mappings` ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;
ALTER TABLE `inter_project_builds` ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;
ALTER TABLE `malformed_files` ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;

--changeset jgoodwin:10 dbms:mysql
ALTER TABLE `module_provides` DROP PRIMARY KEY, ADD PRIMARY KEY(`moduleId`, `name`, `version`);
ALTER TABLE `module_depends` DROP PRIMARY KEY, ADD PRIMARY KEY(`moduleId`, `name`, `version`);

--changeset jgoodwin:11
CREATE INDEX build_state on repo_builds (state)
