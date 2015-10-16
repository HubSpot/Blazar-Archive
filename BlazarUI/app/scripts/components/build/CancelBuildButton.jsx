import React, {Component, PropTypes} from 'react';
import {Button} from 'react-bootstrap';
import Icon from '../shared/Icon.jsx';
import Modal from 'react-bootstrap/lib/Modal';
import {bindAll, contains} from 'underscore';
import FINAL_BUILD_STATES from '../../constants/finalBuildStates';

class CancelBuildButton extends Component {

  constructor() {
    bindAll(this, 'handleCancelBuild', 'cancelBuild', 'cancelModal', 'closeCancelModal');
    this.state = {
      cancelling: false,
      showModal: false
    }
  }

  handleCancelBuild() {
    this.setState({
      showModal: true
    });
  }
  
  cancelBuild() {
    this.props.triggerCancelBuild(this.props.build.build.id, this.props.build.module.id);

    this.setState({
      cancelling: true,
      showModal: false
    });    
  }
  
  cancelModal() {
    return (
      <Modal show={this.state.showModal} onHide={this.closeCancelModal}>
        <Modal.Header closeButton>
          <Modal.Title>Are you sure you want to cancel build #{this.props.build.build.id}</Modal.Title>
        </Modal.Header>
        <Modal.Footer>
          <Button onClick={this.closeCancelModal}>No, nevermind</Button>
          <Button onClick={this.cancelBuild} bsStyle="danger">Yes, cancel build</Button>
        </Modal.Footer>
      </Modal>
    )
  }
  
  closeCancelModal() {
    this.setState({
      showModal: false
    });
  }

  render() {
    
    if (this.props.build.build.state === undefined || contains(FINAL_BUILD_STATES, this.props.build.build.state)) {
      return null;
    }
    
    if (this.props.buildCancelTriggered || this.state.cancelling) {
      return (
        <div>
          <Button bsStyle="danger" disabled>
            <Icon for="spinner" /> Cancelling
          </Button>
          {this.cancelModal()}
        </div>
      );
    } else {
      return (
        <div>
          <Button bsStyle="danger" onClick={this.handleCancelBuild}>
            Cancel Build
          </Button>
          {this.cancelModal()}
        </div>
      );
    }
  }
}
// 
CancelBuildButton.propTypes = {
  triggerCancelBuild: PropTypes.func.isRequired,
  build: PropTypes.object.isRequired
};

export default CancelBuildButton;
