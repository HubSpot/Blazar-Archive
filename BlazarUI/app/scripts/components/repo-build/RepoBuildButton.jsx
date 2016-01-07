import React, {Component, PropTypes} from 'react';
import {Button} from 'react-bootstrap';
import Icon from '../shared/Icon.jsx';
import {bindAll} from 'underscore';

class RepoBuildButton extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'build');
  }

  build() {
    this.props.triggerBuild();
  }

  render() {
    
    if (this.props.loading) {
      return null;
    }

    if (this.props.buildTriggering) {
      return (
        <Button bsStyle="primary" disabled>
          <Icon for="spinner" /> Build Now
        </Button>
      );
    } else {
      return (
        <Button bsStyle="primary" onClick={this.build}> Build Now</Button>
      );
    }
  }
}

RepoBuildButton.propTypes = {
  triggerBuild: PropTypes.func.isRequired,
  buildTriggering: PropTypes.bool,
  loading: PropTypes.bool.isRequired
};

export default RepoBuildButton;
