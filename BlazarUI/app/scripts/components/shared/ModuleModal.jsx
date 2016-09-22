import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import ReactTooltip from 'react-tooltip';
import Modal from 'react-bootstrap/lib/Modal';
import Button from 'react-bootstrap/lib/Button';
import Checkbox from './Checkbox.jsx';
import Icon from './Icon.jsx';
import ModuleSelectWrapper from './ModuleSelectWrapper.jsx';

class ModuleModal extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'handleSubmit', 'maybeSubmit');
  }

  componentWillReceiveProps(nextProps) {
    const openingModal = nextProps.showModal && !this.props.showModal;
    if (openingModal) {
      const moduleIds = nextProps.modules.map((module) => module.id);
      this.props.onUpdateSelectedModuleIds(moduleIds);
    }
  }

  handleSubmit() {
    const {
      branchId,
      selectedModuleIds,
      resetCache,
      buildDownstreamModules,
      triggerInterProjectBuild,
      onBuildStart
    } = this.props;

    this.props.triggerBuild(branchId, {
      selectedModuleIds,
      resetCache,
      buildDownstreamModules,
      triggerInterProjectBuild
    }, onBuildStart);
    this.props.closeModal();
  }

  maybeSubmit(target) {
    if (target.charCode === 13) {
      this.handleSubmit();
    }
  }

  renderDownstreamToggle() {
    return (
      <div className="downstream-checkbox-wrapper">
        <Checkbox
          label=" Build Downstream Modules"
          name="downstream-checkbox"
          checked={this.props.buildDownstreamModules}
          onCheckboxUpdate={this.props.onCheckboxUpdate}
        />
      <a data-tip={true} data-for="downstreamTooltip">
          <Icon
            type="fa"
            name="question-circle"
            classNames="checkbox-tooltip"
          />
        </a>
        <ReactTooltip
          id="downstreamTooltip"
          place="bottom"
          type="dark"
          effect="solid">
          Build dependent downstream modules within the same repository
        </ReactTooltip>
      </div>
    );
  }

  renderInterProjectToggle() {
    return (
      <div className="inter-project-checkbox-wrapper">
        <Checkbox
          label=" Trigger Inter-Project Build"
          name="triggerInterProjectBuild-checkbox"
          checked={this.props.triggerInterProjectBuild}
          onCheckboxUpdate={this.props.onTriggerInterProjectBuild}
        />
        <div className="tooltip-wrapper">
          <a data-tip={true} data-for="triggerInterProjectBuildTooltip">
            <Icon
              type="fa"
              name="question-circle"
              classNames="checkbox-tooltip"
            />
          </a>
          <ReactTooltip
            id="triggerInterProjectBuildTooltip"
            place="bottom"
            type="dark"
            effect="solid">
            Trigger inter-project build of all dependent modules
          </ReactTooltip>
        </div>
      </div>
    );
  }

  renderResetCache() {
    return (
      <div className="cache-checkbox-wrapper">
        <Checkbox
          label=" Reset Cache"
          name="cache-checkbox"
          checked={this.props.resetCache}
          onCheckboxUpdate={this.props.onResetCacheUpdate}
        />
        <div className="tooltip-wrapper">
          <a data-tip={true} data-for="cache-tooltip">
            <Icon
              type="fa"
              name="question-circle"
              classNames="checkbox-tooltip"
            />
          </a>
         <ReactTooltip
           className="cache-tooltip"
           id="cache-tooltip"
           place="bottom"
           type="dark"
           effect="solid">
            Reset data cached from the previous build
          </ReactTooltip>
        </div>
      </div>
    );
  }

  renderModalContent() {
    return (
      <div>
        <Modal.Header>
          <Modal.Title>
            Build Options
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className="row">
            <div className="col-md-6">
              <span className="module-modal__text">Choose modules to build:</span>
              <ModuleSelectWrapper
                modules={this.props.modules}
                selectedModuleIds={this.props.selectedModuleIds}
                onUpdateSelectedModuleIds={this.props.onUpdateSelectedModuleIds}
              />
            </div>
          </div>
          <div className="checkbox-wrapper">
            {this.renderDownstreamToggle()}
            {this.renderResetCache()}
            {this.renderInterProjectToggle()}
          </div>
        </Modal.Body>
        <Modal.Footer>
          <Button id="module-modal-nevermind-button" onClick={this.props.closeModal}>Nevermind</Button>
          <Button id="module-modal-build-button" disabled={!this.props.selectedModuleIds.length} onClick={this.handleSubmit} bsStyle="primary">Build</Button>
        </Modal.Footer>
      </div>
    );
  }

  render() {
    return (
      <Modal dialogClassName="module-modal" bsSize="large" show={this.props.showModal} onKeyPress={this.maybeSubmit} onHide={this.props.closeModal}>
        {this.renderModalContent()}
      </Modal>
    );
  }
}

ModuleModal.propTypes = {
  showModal: PropTypes.bool.isRequired,
  closeModal: PropTypes.func.isRequired,
  triggerBuild: PropTypes.func.isRequired,
  onBuildStart: PropTypes.func,
  branchId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  modules: PropTypes.array.isRequired,

  onUpdateSelectedModuleIds: PropTypes.func.isRequired,
  onCheckboxUpdate: PropTypes.func.isRequired,
  onResetCacheUpdate: PropTypes.func.isRequired,
  onTriggerInterProjectBuild: PropTypes.func.isRequired,

  selectedModuleIds: PropTypes.arrayOf(PropTypes.number).isRequired,
  buildDownstreamModules: PropTypes.bool.isRequired,
  resetCache: PropTypes.bool.isRequired,
  triggerInterProjectBuild: PropTypes.bool.isRequired
};

export default ModuleModal;
