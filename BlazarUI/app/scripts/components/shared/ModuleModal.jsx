import React, {Component, PropTypes} from 'react';
import Immutable from 'immutable';
import Modal from 'react-bootstrap/lib/Modal';
import Button from 'react-bootstrap/lib/Button';
import Checkbox from './Checkbox.jsx';
import ModuleSelectWrapper from './ModuleSelectWrapper.jsx';
import $ from 'jquery';

class ModuleModal extends Component {

  renderModule(moduleObject) {
    let moduleId = moduleObject.get('id');
    let moduleName = moduleObject.get('name');

    return (
      <div>
        <Checkbox
          label={moduleName}
          name='module-checkbox'
          value={moduleId}
          checked={true} />
        <br />
      </div>
    );
  }

  renderDownstreamToggle() {
    return (
      <Checkbox
        label='Build Downstream Modules'
        name='downstream-checkbox' />
    );
  }

  getModuleIdsAndBuild() {
    this.props.triggerBuild();
    this.props.closeModal();
  }

  updateSelectedModules(modules) {
    console.log(modules);
    console.log(modules.split(','));
    this.props.onSelectUpdate(modules.split(','));
  }

  render() {
    return (
      <Modal dialogClassName='module-modal' bsSize='large' show={this.props.showModal} onHide={this.props.closeModal}>
        <Modal.Body>
          <div className='row'>
            <div className='col-md-6'>
              <span className='module-select-header'>
                Choose modules to build
              </span>
              <ModuleSelectWrapper
                modules={this.props.modules}
                onSelectUpdate={this.updateSelectedModules.bind(this)}
              />
            </div>
            <div className='col-mid-6'>
              Toggle goes here
            </div>
          </div>
        </Modal.Body>
        <Modal.Footer>
          <Button onClick={this.props.closeModal}>Close</Button>
          <Button onClick={this.getModuleIdsAndBuild.bind(this)} className='btn btn-primary'>BUILD THAT</Button>
        </Modal.Footer>
      </Modal>
    );
  }
}

ModuleModal.propTypes = {
  closeModal: PropTypes.func.isRequired,
  triggerBuild: PropTypes.func.isRequired,
  onSelectUpdate: PropTypes.func.isRequired,
  showModal: PropTypes.bool.isRequired,
  modules: PropTypes.instanceOf(Immutable.List)
};

export default ModuleModal;