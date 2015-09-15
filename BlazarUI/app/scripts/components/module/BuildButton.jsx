import React, {Component, PropTypes} from 'react';
import {Button} from 'react-bootstrap';
import Icon from '../shared/Icon.jsx';
import $ from 'jquery';

class BuildButton extends Component {

  constructor(props) {
    super(props);
  }

  build() {
    this.props.triggerBuild();
  }

  render() {
    if (this.props.buildTriggering) {
      return (
        <Button bsStyle="primary" disabled>
          <Icon for="spinner" /> Build Now
        </Button>
      );
    } else {
      return (
        <Button bsStyle="primary" onClick={this.build.bind(this)}> Build Now</Button>
      );
    }
  }
}

BuildButton.propTypes = {
  triggerBuild: PropTypes.func.isRequired,
  buildTriggering: PropTypes.bool
};

export default BuildButton;
