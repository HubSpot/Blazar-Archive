import { connect } from 'react-redux';
import ModuleModal from '../shared/ModuleModal.jsx';
import {
  hideBranchBuildModal,
  updateBranchBuildSelectedModuleIds,
  toggleBuildDownstreamModules,
  toggleTriggerInterProjectBuild,
  toggleResetCache,
  triggerBuild
} from '../../redux-actions/buildBranchFormActions';

const mapStateToProps = (state) => {
  const buildBranchForm = state.buildBranchForm;
  return {
    showModal: buildBranchForm.get('showModal'),
    selectedModuleIds: buildBranchForm.get('selectedModuleIds').toArray(),
    buildDownstreamModules: buildBranchForm.get('buildDownstreamModules'),
    triggerInterProjectBuild: buildBranchForm.get('triggerInterProjectBuild'),
    resetCache: buildBranchForm.get('resetCache')
  };
};

export default connect(mapStateToProps, {
  closeModal: hideBranchBuildModal,
  onUpdateSelectedModuleIds: updateBranchBuildSelectedModuleIds,
  onCheckboxUpdate: toggleBuildDownstreamModules,
  onTriggerInterProjectBuild: toggleTriggerInterProjectBuild,
  onResetCacheUpdate: toggleResetCache,
  triggerBuild
})(ModuleModal);
