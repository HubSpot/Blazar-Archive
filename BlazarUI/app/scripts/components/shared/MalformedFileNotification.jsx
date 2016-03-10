import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import Modal from 'react-bootstrap/lib/Modal';
import Button from 'react-bootstrap/lib/Button';
import Alert from 'react-bootstrap/lib/Alert';
import json2html from 'json-to-html';

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
    this.setState({
      showModal: true
    });
  }

  closeModal() {
    this.setState({
      showModal: false
    });
  }

  getJSONMarkup(json) {
    return {__html: json2html(json)};
  }

  renderConfigInfo() {
    return this.props.malformedFiles.map((malformedFile, i) => {
      const {branchId, type, path, details} = malformedFile;

      return (
        <div key={i} className="malformed-files__file">
          <code className='malformed-files__file-name'>{path}</code>
          <pre className='malformed-files__file-contents' dangerouslySetInnerHTML={this.getJSONMarkup(details)} />
        </div>
      );
    });
  }

  renderModal() {
    return (
      <Modal show={this.state.showModal} onHide={this.closeModal}>
        <Modal.Header closeButton>
          <Modal.Title>
            This branch has malformed config files
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          This is the text that describes the issue and what you need to do to fix it.
          {this.renderConfigInfo()}
        </Modal.Body>
        <Modal.Footer>
          <Button bsStyle="primary" onClick={this.closeModal}>Okay</Button>
        </Modal.Footer>
      </Modal>
    );
  }

  renderAlert() {
    return (
      <div onClick={this.openModal} className='malformed-files__alert-wrapper'>
        <Alert bsStyle='danger' className='malformed-files__alert'>
          One or more of your config files for this branch is malformed. Click this alert for more details.
        </Alert>
      </div>
    );
  }

  render() {
    if (this.props.loading || this.props.malformedFiles.length === 0) {
      return (
        <div />
      );
    }

    return (
      <div className="malformed-files">
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