import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import Modal from 'react-bootstrap/lib/Modal';
import Button from 'react-bootstrap/lib/Button';

import Icon from '../shared/Icon.jsx';

let initialState = {
  showModal: false
}

class MalformedFileNotification extends Component {
	
  constructor() {
    this.state = initialState;

    bindAll(this, 'openModal', 'closeModal');
  }

  openModal() {
    console.log("opening modal");

    this.setState({
      showModal: true
    });
  }

  closeModal() {
    console.log("closing modal");
    this.setState({
      showModal: false
    });
  }

  renderModal() {
    return (
      <Modal show={this.state.showModal} onHide={this.closeModal}>
        <Modal.Header closeButton>
          <Modal.Title>
            <strong>{this.props.malformedFiles.length}</strong> Malformed Files
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Body of modal!!!!!!
        </Modal.Body>
        <Modal.Footer>
          <Button bsStyle="primary" onClick={this.closeModal}>Done</Button>
        </Modal.Footer>
      </Modal>
    );
  }

  renderAlert() {
    return (
      <Icon onClick={this.openModal} type="mega-octicon" name="alert" classNames="malformed-alert-icon" />
    );
  }

  render() {
    if (this.props.loading || this.props.malformedFiles.length === 0) {
      console.log("loading or no files");
      return (
        <div />
      );
    }

    console.log("rendering icon");

    return (
      <div className="malformed-alert">
        {this.renderAlert()}
        {this.renderModal()}
      </div>
    );
  }
}

MalformedFileNotification.propTypes = {
  malformedFiles: PropTypes.array.isRequired,
  loading: PropTypes.bool.isRequired
}

export default MalformedFileNotification;