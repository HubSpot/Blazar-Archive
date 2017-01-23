import { connect } from 'react-redux';
import { sortBy } from 'underscore';
import BuildBranchModal from '../shared/BuildBranchModal.jsx';
import {
  hideBranchBuildModal,
  updateBranchBuildSelectedModuleIds,
  toggleBuildDownstreamModules,
  toggleTriggerInterProjectBuild,
  toggleResetCache,
  triggerBuild
} from '../../redux-actions/buildBranchFormActions';

const mapStateToProps = (state, ownProps) => {
  const buildBranchForm = state.buildBranchForm;
  return {
    showModal: buildBranchForm.get('showModal'),
    selectedModuleIds: buildBranchForm.get('selectedModuleIds').toArray(),
    buildDownstreamModules: buildBranchForm.get('buildDownstreamModules'),
    triggerInterProjectBuild: buildBranchForm.get('triggerInterProjectBuild'),
    resetCache: buildBranchForm.get('resetCache'),
    modules: sortBy(ownProps.modules, (module) => module.name)
  };
};

export default connect(mapStateToProps, {
  closeModal: hideBranchBuildModal,
  onUpdateSelectedModuleIds: updateBranchBuildSelectedModuleIds,
  onCheckboxUpdate: toggleBuildDownstreamModules,
  onTriggerInterProjectBuild: toggleTriggerInterProjectBuild,
  onResetCacheUpdate: toggleResetCache,
  triggerBuild
})(BuildBranchModal);
