import React, { PropTypes} from 'react';
import {Button} from 'react-bootstrap';
import Modal from 'react-bootstrap/lib/Modal';

const CancelBuildModal = ({show, onHide, onConfirm, buildNumber}) => {
  return (
    <Modal show={show} onHide={onHide}>
      <Modal.Header closeButton={true}>
        <Modal.Title>
          Are you sure you want to cancel <strong> build #{buildNumber}</strong>?
        </Modal.Title>
      </Modal.Header>
      <Modal.Footer>
        <Button id="cancel-modal-no-btn" onClick={onHide}>No, nevermind</Button>
        <Button id="cancel-modal-yes-btn" onClick={onConfirm} bsStyle="danger">Yes, cancel build</Button>
      </Modal.Footer>
    </Modal>
  );
};

CancelBuildModal.propTypes = {
  show: PropTypes.bool,
  onHide: PropTypes.func.isRequired,
  onConfirm: PropTypes.func.isRequired,
  buildNumber: PropTypes.number.isRequired
};

export default CancelBuildModal;
