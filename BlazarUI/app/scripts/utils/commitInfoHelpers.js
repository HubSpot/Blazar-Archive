export const getCompareUrl = (commitInfo) => {
  const previousCommitUrl = commitInfo.getIn(['previous', 'url']);
  const currentCommitId = commitInfo.getIn(['current', 'id']);
  return `${previousCommitUrl.replace('/commit/', '/compare/')}...${currentCommitId}`;
};
