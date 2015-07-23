import React, {Component, PropTypes} from 'react';

import Repo from './Repo.jsx';
import PageContainer from '../layout/PageContainer.jsx';

import RepoStore from '../../stores/repoStore';
import RepoActions from '../../actions/repoActions';

class RepoContainer extends Component {

  constructor() {
    this.state = {
      branches: [],
      loading: true
    };
  }

  componentDidMount() {
    this.unsubscribe = RepoStore.listen(this.onStatusChange.bind(this));
    RepoActions.loadBranches(this.props.params);
  }

  componentWillUnmount() {
    this.unsubscribe();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  componentWillReceiveProps(nextprops) {
    RepoActions.loadBranches(nextprops.params);
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
