import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import Immutable from 'immutable';
import Modal from 'react-bootstrap/lib/Modal';
import Button from 'react-bootstrap/lib/Button';
import ButtonGroup from 'react-bootstrap/lib/ButtonGroup'
import Checkbox from './Checkbox.jsx';
import Loader from './Loader.jsx';
import ModuleSelectWrapper from './ModuleSelectWrapper.jsx';

class ModuleModal extends Component {

  constructor(props) {
    super(props);

    bindAll(this, 'updateDownstreamModules', 'updateSelectedModules', 'getModuleIdsAndBuild', 'maybeStartBuild');
  }

  getModuleIdsAndBuild() {
    this.props.triggerBuild();
    this.props.closeModal();
  }

  updateSelectedModules(modules) {
    const finalModules = modules.split(',');
    this.props.onSelectUpdate(finalModules);
  }

  updateDownstreamModules(isChecked) {
    const enumValue = isChecked ? 'WITHIN_REPOSITORY' : 'NONE';
    this.props.onCheckboxUpdate(enumValue);
  }

  maybeStartBuild(target) {
    if (target.charCode === 13) {
      this.props.triggerBuild();
      this.props.closeModal();
    }
  }

  renderDownstreamToggle() {
    return (
      <div className="downstream-checkbox-wrapper">
        <Checkbox
          label=' Build Downstream Modules'
          name='downstream-checkbox'
          checked={true}
          onCheckboxUpdate={this.updateDownstreamModules} 
        />
      </div>
    );
  }

  renderModalContent() {
    if (this.props.loadingModules) {
      return (
        <Modal.Body>
          <Loader align='top-center' />
        </Modal.Body>
      );
    }

    return (
      <div>
        <Modal.Header>
          <Modal.Title>
            Build Options
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className='row'>
            <div className='col-md-6'>
              <span className="module-modal__text">Choose modules to build:</span>
              <ModuleSelectWrapper
                modules={this.props.modules}
                onSelectUpdate={this.updateSelectedModules}
              />
            </div>
          </div>
        </Modal.Body>
        <Modal.Footer>
          {this.renderDownstreamToggle()}
          <Button onClick={this.props.closeModal}>Nevermind</Button>
          <Button onClick={this.getModuleIdsAndBuild} className='btn btn-primary'>Build</Button>
        </Modal.Footer>
      </div>
    );
  }

  render() {
    return (
      <Modal dialogClassName='module-modal' bsSize='large' show={this.props.showModal} onKeyPress={this.maybeStartBuild} onHide={this.props.closeModal}>
        {this.renderModalContent()}
      </Modal>
    );
  }
}

ModuleModal.propTypes = {
  closeModal: PropTypes.func.isRequired,
  triggerBuild: PropTypes.func.isRequired,
  onSelectUpdate: PropTypes.func.isRequired,
  onCheckboxUpdate: PropTypes.func.isRequired,
  showModal: PropTypes.bool.isRequired,
  loadingModules: PropTypes.bool,
  modules: PropTypes.array.isRequired
};

export default ModuleModal;
