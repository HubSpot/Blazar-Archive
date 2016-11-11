import BranchState from './BranchState.jsx';
import { connect } from 'react-redux';
import { withRouter } from 'react-router';
import branchStateActions from '../../redux-actions/branchStateActions';
import { getSelectedBranch, getActiveModules, getInactiveModules } from '../../selectors';

const mapStateToProps = (state, ownProps) => {
  return {
    activeModules: getActiveModules(state),
    inactiveModules: getInactiveModules(state),
    branchId: parseInt(ownProps.params.branchId, 10),
    branchInfo: getSelectedBranch(state),
    loadingBranchStatus: state.branchState.get('loading'),
    selectedModuleId: state.branchState.get('selectedModuleId'),
    pendingBranchBuilds: state.branchState.get('queuedBuilds'),
    malformedFiles: state.branchState.get('malformedFiles'),
    branchNotFound: state.branchState.get('branchNotFound'),
    showBetaFeatureAlert: !state.dismissedBetaNotifications.get('branchStatePage'),
    errorMessage: state.branchState.getIn(['error', 'message'])
  };
};

export default withRouter(connect(mapStateToProps, branchStateActions)(BranchState));
