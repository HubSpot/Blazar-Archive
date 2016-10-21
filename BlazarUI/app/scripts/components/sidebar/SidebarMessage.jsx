import React, {PropTypes} from 'react';
import MutedMessage from '../shared/MutedMessage.jsx';
import {NO_MATCH_MESSAGES} from '../constants';

const SidebarMessage = ({numberOfBuilds, filterText, toggleFilterState}) => {
  let message = null;

  if (!numberOfBuilds && filterText.length) {
    message = (
      <span>
        No {NO_MATCH_MESSAGES[toggleFilterState]} repositories matching <strong>{filterText}</strong>.
      </span>
    );
  } else if (!numberOfBuilds && !filterText.length) {
    if (toggleFilterState === 'starred') {
      message = 'No repositories have been starred';
    } else if (toggleFilterState === 'building') {
      message = 'No repositories actively building';
    }
  }

  return <MutedMessage roomy={true}>{message}</MutedMessage>;
};


SidebarMessage.propTypes = {
  numberOfBuilds: PropTypes.number,
  filterText: PropTypes.string,
  toggleFilterState: PropTypes.string
};

export default SidebarMessage;
