import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import Icon from '../../shared/Icon.jsx';

const CompareCommitsLink = ({commitInfo, className}) => {
  const previousCommitUrl = commitInfo.getIn(['previous', 'url']);
  const currentCommitId = commitInfo.getIn(['current', 'id']);
  const commitUrl = `${previousCommitUrl.replace('/commit/', '/compare/')}...${currentCommitId}`;
  return (
    <a className={className} href={commitUrl} target="_blank" onClick={(e) => e.stopPropagation()}>
      <Icon type="octicon" name="diff" />
    </a>
  );
};

CompareCommitsLink.propTypes = {
  commitInfo: ImmutablePropTypes.mapContains({
    current: ImmutablePropTypes.map,
    previous: ImmutablePropTypes.map
  }),
  className: PropTypes.string
};

export default CompareCommitsLink;
