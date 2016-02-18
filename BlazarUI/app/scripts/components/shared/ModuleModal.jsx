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
      <div>
        {this.props.modules.map(this.renderModule)}
        <br />
        <br />
        <br />
        {this.renderDownstreamToggle()}
      </div>
    );
  }

  getModuleIdsAndBuild() {
    // TODO: more specific/no $
    const moduleIds = $('input[name=module-checkbox]:checked').map(function() {
      return parseInt($(this).val());
    }).get();

    const downstreamToggle = $('input[name=downstream-checkbox]:checked').size() > 0 ? 'WITHIN_REPOSITORY' : 'NONE';

    this.props.okayGoBuild(moduleIds, downstreamToggle);
  }

  render() {
    return (
      <Modal dialogClassName='module-modal' bsSize='large' show={this.props.showModal} onHide={this.props.whenDone}>
        <Modal.Body>
          {this.renderModuleList()}
        </Modal.Body>
        <Modal.Footer>
          <Button onClick={this.props.whenDone}>Close</Button>
          <Button onClick={this.getModuleIdsAndBuild.bind(this)} className='btn btn-primary'>BUILD THAT</Button>
        </Modal.Footer>
      </Modal>
    );
  }
}

ModuleModal.propTypes = {
  whenDone: PropTypes.func.isRequired,
  okayGoBuild: PropTypes.func.isRequired,
  showModal: PropTypes.bool.isRequired,
  modules: PropTypes.instanceOf(Immutable.List)
};

export default ModuleModal;