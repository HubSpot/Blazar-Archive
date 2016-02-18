import React, {Component, PropTypes} from 'react';
import Immutable from 'immutable';
import Modal from 'react-bootstrap/lib/Modal';
import Button from 'react-bootstrap/lib/Button';
import Checkbox from './Checkbox.jsx';
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

  renderModuleList() {
    return (
      <div className='module-modal--container'>
        <div className='module-modal--modules'>
          <span className='module-modal--title'>Pick Modules</span>
          {this.props.modules.map(this.renderModule)}
        </div>
        <div className='module-modal--downstream'>
          <span className='module-modal--title'>Toggle Option</span><br />
          {this.renderDownstreamToggle()}
        </div>
      </div>
    );
  }

  getModuleIdsAndBuild() {
    // TODO: more specific/no $
    const moduleIds = $('input[name=module-checkbox]:checked').map(function() {
      return parseInt($(this).val());
    }).get();

    const downstreamToggle = $('input[name=downstream-checkbox]:checked').size() > 0 ? 'WITHIN_REPOSITORY' : 'NONE';

    this.props.triggerBuild(moduleIds, downstreamToggle);
    this.props.closeModal();
  }

  render() {
    return (
      <Modal dialogClassName='module-modal' bsSize='large' show={this.props.showModal} onHide={this.props.closeModal}>
        <Modal.Body>
          {this.renderModuleList()}
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
  showModal: PropTypes.bool.isRequired,
  modules: PropTypes.instanceOf(Immutable.List)
};

export default ModuleModal;