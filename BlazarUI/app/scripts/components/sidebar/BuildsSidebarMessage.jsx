import React, {Component, PropTypes} from 'react';
import MutedMessage from '../shared/MutedMessage.jsx';

class BuildsSidebarMessage extends Component {

  render() {
    let message = null;

    if (this.props.numModules === 0 && this.props.filterText.length > 0) {      
        message = (
          <span>
            No {searchType} modules matching <strong>{this.props.filterText}</strong>.
          </span>
        ) 
    }

    else if (this.props.numModules === 0 && this.props.filterText.length === 0) {
      if (this.props.toggleFilterState === 'starred') {
        message = 'No modules have been starred';
      } 

      else if (this.props.toggleFilterState === 'building') {
        message = 'No modules actively building';
      }
    }

    return (
      <MutedMessage roomy={true}>
        {message}
      </MutedMessage>
    )
    
  }

}


BuildsSidebarMessage.propTypes = {
  numModules: PropTypes.number,
  filterText: PropTypes.string,
  toggleFilterState: PropTypes.string
};

export default BuildsSidebarMessage;
