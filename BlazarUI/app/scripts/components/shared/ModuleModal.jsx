import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import Immutable from 'immutable';
import ReactTooltip from 'react-tooltip';
import Modal from 'react-bootstrap/lib/Modal';
import Button from 'react-bootstrap/lib/Button';
import Checkbox from './Checkbox.jsx';
import Loader from './Loader.jsx';
import Icon from './Icon.jsx';
import ModuleSelectWrapper from './ModuleSelectWrapper.jsx';

let initialState = {
  showDownstreamTooltip: false,
  showCacheTooltip: false
}

class ModuleModal extends Component {

  constructor(props) {
    super(props);

    bindAll(this, 'updateDownstreamModules', 'updateResetCache', 'updateTriggerInterProjectBuild', 'updateSelectedModules', 'getModuleIdsAndBuild', 'maybeStartBuild');
  }

  getModuleIdsAndBuild() {
    this.props.triggerBuild();
    this.props.closeModal();
  }

  updateSelectedModules(modules) {
    const finalModules = modules.split(',');
    this.props.onSelectUpdate(finalModules);
  }

  updateResetCache() {
    this.props.onResetCacheUpdate();
  }

  updateTriggerInterProjectBuild() {
    this.props.onTriggerInterProjectBuild();
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
        <a data-tip data-for='downstreamTooltip'>
          <Icon
            type='fa'
            name='question-circle'
            classNames='checkbox-tooltip'
          />
        </a>
        <ReactTooltip
          id='downstreamTooltip'
          place='bottom'
          type='dark'
          effect='solid'>
          Build dependent downstream modules within the same repository
        </ReactTooltip>
      </div>
    );
  }
  renderInterProjectToggle() {
    return (
      <div className="inter-project-checkbox-wrapper">
        <Checkbox
        label=' Trigger Inter-Project Build'
        name='triggerInterProjectBuild-checkbox'
        checked={false}
        onCheckboxUpdate={this.updateTriggerInterProjectBuild}
        />
      <a data-tip data-for='interProjectTooltip'>
        <Icon
          type='fa'
          name='question-circle'
          classNames='checkbox-tooltip'
        />
      </a>
      <ReactTooltip
        id='interProjectTooltip'
        place='bottom'
        type='dark'
        effect='solid'>
        Trigger Inter-Repository build of all dependent modules
        </ReactTooltip>
      </div>
      )
  }

  renderResetCache() {
    return (
      <div className="cache-checkbox-wrapper">
        <Checkbox
          label=' Reset Cache'
          name='cache-checkbox'
          checked={false}
          onCheckboxUpdate={this.updateResetCache}
        />
        <div className='tooltip-wrapper'>
          <a data-tip data-for='cache-tooltip'>
            <Icon
              type='fa'
              name='question-circle'
              classNames='checkbox-tooltip'
            />
          </a>
         <ReactTooltip
            className='cache-tooltip'
            id='cache-tooltip'
            place='bottom'
            type='dark'
            effect='solid'>
            Reset data cached from the previous build
          </ReactTooltip>
        </div>
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
          <div className='checkbox-wrapper'>
            {this.renderDownstreamToggle()}
            {this.renderResetCache()}
            {this.renderInterProjectToggle()}
          </div>
        </Modal.Body>
        <Modal.Footer>
          <Button id='module-modal-nevermind-button' onClick={this.props.closeModal}>Nevermind</Button>
          <Button id='module-modal-build-button' onClick={this.getModuleIdsAndBuild} className='btn btn-primary'>Build</Button>
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
  onResetCacheUpdate: PropTypes.func.isRequired,
  onTriggerInterProjectBuild:  PropTypes.func.isRequired,
  showModal: PropTypes.bool.isRequired,
  loadingModules: PropTypes.bool,
  modules: PropTypes.array.isRequired
};

export default ModuleModal;
