import React, {Component, PropTypes} from 'react';
import {Button} from 'react-bootstrap';
import Icon from '../shared/Icon.jsx';

class BuildButton extends Component {

  render() {
    if (this.props.loading) {
      return null;
    }
    
    return (
      <Button 
        bsStyle="primary" 
        onClick={this.props.openModuleModal}
      >
        Build Now
      </Button>
    );
  }
}

BuildButton.propTypes = {
  openModuleModal: PropTypes.func.isRequired,
  loading: PropTypes.bool.isRequired
};

export default BuildButton;
