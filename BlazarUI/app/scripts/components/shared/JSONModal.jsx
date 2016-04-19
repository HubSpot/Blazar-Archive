import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import ClassNames from 'classnames';
import json2html from 'json-to-html';
import Modal from 'react-bootstrap/lib/Modal';
import Button from 'react-bootstrap/lib/Button';

class JSONModal extends Component {

  constructor() {
    bindAll(this, 'handleTriggerJsonModal', 'modal', 'closeModal', 'openModal');

    this.state = {
      showModal: false
    };
  }

  modal() {
    return (
      <Modal dialogClassName='json-modal' bsSize='large' show={this.state.showModal} onHide={this.closeModal}>
        <Modal.Body>
          <pre className='json-modal__inner-content' dangerouslySetInnerHTML={this.getJSONMarkup()} />
        </Modal.Body>
        <Modal.Footer>
          <Button onClick={this.closeModal}>Close</Button>
        </Modal.Footer>
      </Modal>
    );
  }
  
  getJSONMarkup() {
    return {__html: json2html(this.props.json)};
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

  getClassNames() {
    return ClassNames([
      'btn btn-default',
      this.props.classname
    ]);
  }
  
  handleTriggerJsonModal() {
    this.setState({
      showModal: true
    })
  }

  render() {
    return (
      <span>
        <button id='json-modal-button' onClick={this.handleTriggerJsonModal} className={this.getClassNames()}>JSON</button>
        {this.modal()}
      </span>
    );
  }
}

JSONModal.propTypes = {
  json: PropTypes.object.isRequired
};

export default JSONModal;
