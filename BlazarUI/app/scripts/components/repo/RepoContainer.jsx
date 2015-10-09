import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import Repo from './Repo.jsx';
import PageContainer from '../shared/PageContainer.jsx';

import RepoStore from '../../stores/repoStore';
import RepoActions from '../../actions/repoActions';

class RepoContainer extends Component {

  constructor() {
    bindAll(this, 'updateBranchToggleState', 'onStatusChange');

    this.state = {
      branches: [],
      loading: true,
      branchToggleStates: {
        master: true
      }
    };
  }

  componentDidMount() {
    this.unsubscribeFromRepo = RepoStore.listen(this.onStatusChange);
    RepoActions.loadBranches(this.props.params);
  }

  componentWillReceiveProps(nextprops) {
    RepoActions.loadBranches(nextprops.params);
  }

  componentWillUnmount() {
    this.unsubscribeFromRepo();
  }

  updateBranchToggleState(branch) {
    let updated = this.state.branchToggleStates; 
    updated[branch] = !updated[branch];

    this.setState({
      branchToggleStates: updated
    });

  }

  onStatusChange(state) {
    this.setState(state);
  }

  render() {

    return (
      <PageContainer>
        <Repo
          params={this.props.params}
          branches={this.state.branches}
          loading={this.state.loading}
          branchToggleStates={this.state.branchToggleStates}
          updateBranchToggleState={this.updateBranchToggleState}
        />
      </PageContainer>
    );
  }
}

RepoContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default RepoContainer;
