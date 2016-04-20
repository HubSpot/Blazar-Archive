import React, {Component, PropTypes} from 'react';
import {Button} from 'react-bootstrap';
import Icon from '../shared/Icon.jsx';
import Modal from 'react-bootstrap/lib/Modal';
import {bindAll, contains} from 'underscore';
import {browserHistory} from 'react-router';
import FINAL_BUILD_STATES from '../../constants/finalBuildStates';

class CancelBuildButton extends Component {

  constructor(props, context) {
    super(props, context);
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

  buildBranchHistoryPagePath() {
    const {branchId} = this.props.params;

    return `/builds/branch/${branchId}`;
  }
  
  cancelBuild() {
    this.props.triggerCancelBuild();

    this.setState({
      cancelling: true,
      showModal: false
    });

    this.context.router.push(this.buildBranchHistoryPagePath());
  }

  cancelModal() {
    return (
      <Modal show={this.state.showModal} onHide={this.closeCancelModal}>
        <Modal.Header closeButton>
          <Modal.Title>
            Are you sure you want to cancel 
            <strong> build #{this.props.build.buildNumber}</strong> {' '}?
          </Modal.Title>
        </Modal.Header>
        <Modal.Footer>
          <Button id='cancel-modal-yes-button' onClick={this.closeCancelModal}>No, nevermind</Button>
          <Button id='cancel-modal-no-button' onClick={this.cancelBuild} bsStyle="danger">Yes, cancel build</Button>
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
    let cancelButtonContent;
    
    if (this.props.build.state === undefined || contains(FINAL_BUILD_STATES, this.props.build.state)) {
      return null;
    }
    
    if (this.props.buildCancelTriggered || this.state.cancelling) {
      cancelButtonContent = (
        <span>
          <Icon for="spinner" /> Cancelling
        </span>
      );
    }

    else {
      cancelButtonContent = 'Cancel Build'
    }
    
    return (
      <div className='cancel-build-button text-right'>
        <Button 
          bsSize='xsmall' 
          bsStyle="danger" 
          disabled={this.props.buildCancelTriggered || this.state.cancelling}
          onClick={this.handleCancelBuild}
        >
          {cancelButtonContent}
        </Button>
        {this.cancelModal()}
      </div>
    ); 

  }

}

CancelBuildButton.propTypes = {
  triggerCancelBuild: PropTypes.func.isRequired,
  build: PropTypes.object.isRequired
};

CancelBuildButton.contextTypes = {
  router: PropTypes.object.isRequired
};

export default CancelBuildButton;
