import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import Icon from '../../shared/Icon.jsx';
import { getCompareUrl } from '../../../utils/commitInfoHelpers';

const CompareCommitsLink = ({commitInfo, className}) => {
  return (
    <a
      className={className}
      href={getCompareUrl(commitInfo)}
      target="_blank"
      onClick={(e) => e.stopPropagation()}
    >
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
