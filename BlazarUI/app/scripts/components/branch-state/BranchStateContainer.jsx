import BranchState from './BranchState.jsx';
import { connect } from 'react-redux';
import { withRouter } from 'react-router';

const mapStateToProps = (state, ownProps) => {
  const polledState = state.branchState.get('moduleStates');
  return {
    activeModules: polledState.filter(moduleState => moduleState.getIn(['module', 'active'])),
    inactiveModules: polledState.filter(moduleState => !moduleState.getIn(['module', 'active'])),
    branchId: parseInt(ownProps.params.branchId, 10),
    loadingModuleStates: state.branchState.get('loading')
  };
};

export default withRouter(connect(mapStateToProps)(BranchState));
