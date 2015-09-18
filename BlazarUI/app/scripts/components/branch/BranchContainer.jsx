import React, {Component, PropTypes} from 'react';
import Branch from './Branch.jsx';
import PageContainer from '../shared/PageContainer.jsx';
import BranchStore from '../../stores/branchStore';
import BranchActions from '../../actions/branchActions';
import BuildsStore from '../../stores/buildsStore';


class BranchContainer extends Component {

  constructor() {
    this.state = {
      modules: [],
      loadingBuilds: true,
      loadingBranchModules: false,
      loading: true
    };
  }

  componentDidMount() {
    this.unsubscribeFromBranch = BranchStore.listen(this.onStatusChange.bind(this));
    this.unsubscribeFromBuilds = BuildsStore.listen(this.onStatusChange.bind(this));
    
    // check if we already have builds in the store
    if (BuildsStore.buildsHaveLoaded) {
      BranchActions.loadModules(this.props.params);
    }

  }

  componentWillReceiveProps(nextprops) {
    BranchActions.loadModules(nextprops.params);
  }

  componentWillUnmount() {
    BranchActions.updatePollingStatus(false);
    this.unsubscribeFromBranch();
    this.unsubscribeFromBuilds();
  }

  onStatusChange(state) {
    this.setState(state);

    // load branch modules after we get the builds collection
    if (!state.loadingBuilds && !this.state.loadingBranchModules) {
      BranchActions.loadModules(this.props.params);
      this.state.loadingBranchModules = true;
    }
  }

  render() {

    return (
      <PageContainer>
        <Branch
          params={this.props.params}
          modules={this.state.modules}
          loading={this.state.loading}
        />
      </PageContainer>
    );
  }
}


BranchContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default BranchContainer;
