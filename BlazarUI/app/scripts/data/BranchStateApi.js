import Resource from '../services/ResourceProvider';
import $ from 'jquery';

function fetchBranchStatus(branchId) {
  return new Resource({
    url: `${window.config.apiRoot}/branches/${branchId}/status`,
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
    'moduleBuildInfos.branchBuild.startTimestamp',
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
  fetchBranchStatus,
  fetchModuleBuildHistory
};
