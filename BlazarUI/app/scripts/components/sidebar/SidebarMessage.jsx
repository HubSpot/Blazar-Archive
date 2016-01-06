import React, {Component, PropTypes} from 'react';
import MutedMessage from '../shared/MutedMessage.jsx';
import {NO_MATCH_MESSAGES} from '../constants';

class SidebarMessage extends Component {

  render() {
    let message = null;
    // dont display any messages while we are toggling
    if (this.props.dontDisplay) {
      return null;
    }

    if (this.props.numberOfBuilds === 0 && this.props.filterText.length > 0) {
        message = (
          <span>
            No {NO_MATCH_MESSAGES[this.props.toggleFilterState]} repositories matching <strong>{this.props.filterText}</strong>.
          </span>
        ) 
    }

    else if (this.props.numberOfBuilds === 0 && this.props.filterText.length === 0) {
      if (this.props.toggleFilterState === 'starred') {
        message = 'No repositories have been starred';
      } 

      else if (this.props.toggleFilterState === 'building') {
        message = 'No repositories actively building';
      }
    }

    return (
      <MutedMessage roomy={true}>
        {message}
      </MutedMessage>
    )
    
  }

}


SidebarMessage.propTypes = {
  numberOfBuilds: PropTypes.number,
  filterText: PropTypes.string,
  toggleFilterState: PropTypes.string
};

export default SidebarMessage;
