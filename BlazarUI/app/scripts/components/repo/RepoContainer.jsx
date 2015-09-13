import React, {Component, PropTypes} from 'react';

import Repo from './Repo.jsx';
import PageContainer from '../shared/PageContainer.jsx';

import RepoStore from '../../stores/repoStore';
import RepoActions from '../../actions/repoActions';

import BuildsStore from '../../stores/buildsStore';
import BuildsActions from '../../actions/buildsActions';

class RepoContainer extends Component {

  constructor() {
    this.state = {
      branches: [],
      loading: true,
      loadingBuilds: true,
      loadingRepos: false
    };
  }

  componentDidMount() {
    this.unsubscribeFromRepo = RepoStore.listen(this.onStatusChange.bind(this));
    this.unsubscribeFromBuilds = BuildsStore.listen(this.onStatusChange.bind(this));    

    // check if we already have builds in the store
    if (BuildsStore.buildsHaveLoaded) {
      console.log('builds already loaded, lets get them');
      RepoActions.loadBranches(this.props.params)
    }

  }

  componentWillReceiveProps(nextprops) {
    RepoActions.loadBranches(nextprops.params);
  }

  componentWillUnmount() {
    RepoActions.updatePollingStatus(false);
    this.unsubscribeFromRepo();
    this.unsubscribeFromBuilds();
  }

  onStatusChange(state) {
    this.setState(state);

    // load repo branches after we get builds collection
    if (!state.loadingBuilds && !this.state.loadingRepos) {
      RepoActions.loadBranches(this.props.params);
      this.state.loadingRepos = true;
    }

  }

  render() {

    return (
      <PageContainer>
        <Repo
          params={this.props.params}
          branches={this.state.branches}
          loading={this.state.loading}
        />
      </PageContainer>
    );
  }
}


RepoContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default RepoContainer;
