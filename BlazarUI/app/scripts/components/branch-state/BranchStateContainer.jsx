import BranchState from './BranchState.jsx';
import { connect } from 'react-redux';
import { withRouter } from 'react-router';
import { selectModule } from '../../redux-actions/branchStateActions';

const mapStateToProps = (state, ownProps) => {
  const polledState = state.branchState.get('moduleStates');
  return {
    activeModules: polledState.filter(moduleState => moduleState.getIn(['module', 'active'])),
    inactiveModules: polledState.filter(moduleState => !moduleState.getIn(['module', 'active'])),
    branchId: parseInt(ownProps.params.branchId, 10),
    loadingModuleStates: state.branchState.get('loading'),
    selectedModuleId: state.branchState.get('selectedModuleId')
  };
};

export default withRouter(connect(mapStateToProps, {selectModule})(BranchState));
