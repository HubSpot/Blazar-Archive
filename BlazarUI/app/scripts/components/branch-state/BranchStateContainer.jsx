import BranchState from './BranchState.jsx';
import { connect } from 'react-redux';
import { withRouter } from 'react-router';
import branchStateActions from '../../redux-actions/branchStateActions';

const mapStateToProps = (state, ownProps) => {
  const moduleStates = state.branchState.get('moduleStates');
  return {
    activeModules: moduleStates.filter(moduleState => moduleState.getIn(['module', 'active'])),
    inactiveModules: moduleStates.filter(moduleState => !moduleState.getIn(['module', 'active'])),
    branchId: parseInt(ownProps.params.branchId, 10),
    branchInfo: state.branch.get('branchInfo'),
    loadingModuleStates: state.branchState.get('loading'),
    selectedModuleId: state.branchState.get('selectedModuleId'),
    branchNotFound: state.branch.getIn(['error', 'status']) === 404,
    showBetaFeatureAlert: !state.dismissedBetaNotifications.get('branchStatePage')
  };
};

export default withRouter(connect(mapStateToProps, branchStateActions)(BranchState));
