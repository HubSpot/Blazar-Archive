import BranchState from './BranchState.jsx';
import { connect } from 'react-redux';

const mapStateToProps = (state) => {
  const polledState = state.branchState.get('polledState');
  return {
    activeModules: polledState.filter(moduleState => moduleState.getIn(['module', 'active'])),
    inactiveModules: polledState.filter(moduleState => !moduleState.getIn(['module', 'active']))
  };
};

export default connect(mapStateToProps)(BranchState);
