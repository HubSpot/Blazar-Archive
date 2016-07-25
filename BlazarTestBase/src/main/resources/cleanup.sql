--liquibase formatted sql
-- changeset TestCleanup:0 runAlways:true
TRUNCATE TABLE `inter_project_build_mappings`;
TRUNCATE TABLE `inter_project_builds`;
TRUNCATE TABLE `branches`;
TRUNCATE TABLE `modules`;
TRUNCATE TABLE `module_depends`;
TRUNCATE TABLE `module_provides`;
TRUNCATE TABLE `repo_builds`;
TRUNCATE TABLE `module_builds`;
TRUNCATE TABLE `malformed_files`;
TRUNCATE TABLE `instant_message_configs`;
TRUNCATE TABLE "queue_items";
