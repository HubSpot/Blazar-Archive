export const getCompareUrl = (commitInfo) => {
  const previousCommitUrl = commitInfo.getIn(['previous', 'url']);
  const currentCommitId = commitInfo.getIn(['current', 'id']);

  if (!previousCommitUrl || !currentCommitId) {
    return '';
  }

  return `${previousCommitUrl.replace('/commit/', '/compare/')}...${currentCommitId}`;
};
