export const getModuleBuildPath = (branchId, buildNumber, moduleName) => {
  return `/builds/branch/${branchId}/build/${buildNumber}/module/${moduleName}`;
};

export const getBranchBuildPath = (branchId, buildNumber) => {
  return `/builds/branch/${branchId}/build/${buildNumber}`;
};

// a 'repo build' is actually a build of a branch of a repo
export const getBranchBuildByIdPath = (branchBuildId) => {
  return `/builds/repo-build/${branchBuildId}`;
};

export const getBranchBuildHistoryPath = (branchId) => {
  return `/branches/${branchId}/builds`;
};

export const getBranchStatePath = (branchId) => {
  return `/branches/${branchId}/state`;
};

export const getBranchSettingsPath = (branchId) => {
  return `/settings/branch/${branchId}`;
};

export const getRepoPath = (repoId) => {
  return `/builds/repo/${repoId}`;
};
