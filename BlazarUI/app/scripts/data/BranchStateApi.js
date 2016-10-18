import Resource from '../services/ResourceProvider';
import $ from 'jquery';

function fetchModuleStates(branchId) {
  return new Resource({
    url: `${window.config.apiRoot}/branches/state/${branchId}/modules`,
  }).send();
}

function fetchModuleBuildHistory(moduleId, offset, pageSize) {
  const propertyFilters = [
    'moduleBuildInfos.moduleBuild',
    '!moduleBuildInfos.moduleBuild.buildConfig',
    '!moduleBuildInfos.moduleBuild.resolvedConfig',
    'moduleBuildInfos.branchBuild.branchId',
    'moduleBuildInfos.branchBuild.buildTrigger',
    'moduleBuildInfos.branchBuild.commitInfo',
    'moduleBuildInfos.branchBuild.id',
    'remaining'
  ];

  const params = {
    property: propertyFilters,
    fromBuildNumber: offset,
    pageSize,
  };

  return new Resource({
    url: `${window.config.apiRoot}/builds/history/module/${moduleId}`,
    data: $.param(params, true)
  }).send();
}

export default {
  fetchModuleStates,
  fetchModuleBuildHistory
};
