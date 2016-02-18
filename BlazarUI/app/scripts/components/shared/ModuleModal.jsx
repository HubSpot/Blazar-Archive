import React, {Component, PropTypes} from 'react';
import Immutable from 'immutable';
import Modal from 'react-bootstrap/lib/Modal';
import Button from 'react-bootstrap/lib/Button';
import $ from 'jQuery';

class ModuleModal extends Component {

  renderModule(moduleObject) {

    let moduleId = moduleObject.get('id');
    let moduleName = moduleObject.get('name');

    return (
      <label>
        <input type='checkbox' name='module-checkbox' value={moduleId} />
        {moduleName}
        <br />
      </label>
    );
  }

  renderModuleList() {
    return (
      <form>
        {this.props.modules.map(this.renderModule)}
      </form>
    );
  }

  getModuleIdsAndBuild() {
    // TODO: more specific/no $
    const moduleIds = $('input:checked').map(function() {
      return parseInt($(this).val());
    }).get();

    /**
      public enum BuildDownstreams {
        NONE, WITHIN_REPOSITORY;
      }
    */

    this.props.okayGoBuild(moduleIds);
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