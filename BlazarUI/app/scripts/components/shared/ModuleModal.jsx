import React, {Component, PropTypes} from 'react';
import Immutable from 'immutable';
import Modal from 'react-bootstrap/lib/Modal';
import Button from 'react-bootstrap/lib/Button';
import ButtonGroup from 'react-bootstrap/lib/ButtonGroup'
import Checkbox from './Checkbox.jsx';
import ModuleSelectWrapper from './ModuleSelectWrapper.jsx';
import $ from 'jquery';

class ModuleModal extends Component {

  getModuleIdsAndBuild() {
    this.props.triggerBuild();
    this.props.closeModal();
  }

  updateSelectedModules(modules) {
    this.props.onSelectUpdate(modules.split(','));
  }

  updateDownstreamModules(isChecked) {
    const enumValue = isChecked ? 'WITHIN_REPOSITORY' : 'NONE';
    this.props.onCheckboxUpdate(enumValue);
  }

  renderDownstreamToggle() {
    return (
      <div className="downstream-checkbox-wrapper">
        <Checkbox
          label=' Build Downstream Modules'
          name='downstream-checkbox'
          checked={true}
          onCheckboxUpdate={this.updateDownstreamModules.bind(this)} 
        />
      </div>
    );
  }

  render() {
    return (
      <Modal dialogClassName='module-modal' bsSize='large' show={this.props.showModal} onHide={this.props.closeModal}>
        <Modal.Header>
          <Modal.Title>
            Build Options
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className='row'>
            <div className='col-md-6 col-md-offset-1'>
              Choose modules to build
              <ModuleSelectWrapper
                modules={this.props.modules}
                onSelectUpdate={this.updateSelectedModules.bind(this)}
              />
            </div>
          </div>
        </Modal.Body>
        <Modal.Footer>
          {this.renderDownstreamToggle()}
          <Button onClick={this.props.closeModal}>Nevermind</Button>
          <Button onClick={this.getModuleIdsAndBuild.bind(this)} className='btn btn-primary'>Build</Button>
        </Modal.Footer>
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
  modules: PropTypes.instanceOf(Immutable.List)
};

export default ModuleModal;
