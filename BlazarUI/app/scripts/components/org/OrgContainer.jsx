import React, {Component, PropTypes} from 'react';
import Org from './Org.jsx';
import PageContainer from '../shared/PageContainer.jsx';
import OrgActions from '../../actions/orgActions';
import OrgStore from '../../stores/orgStore';
import BuildsStore from '../../stores/buildsStore';

class OrgContainer extends Component {

  constructor() {
    this.state = {
      repos: [],
      loadingBuilds: true,
      loadingOrgRepos: false,
      loading: true
    };
  }

  componentDidMount() {
    this.unsubscribeFromOrg = OrgStore.listen(this.onStatusChange.bind(this));
    this.unsubscribeFromBuilds = BuildsStore.listen(this.onStatusChange.bind(this));

    // check if we already have builds in the store
    if (BuildsStore.buildsHaveLoaded) {
      OrgActions.loadRepos(this.props.params);
    }
  }

  componentWillReceiveProps(nextprops) {
    OrgActions.loadRepos(nextprops.params);
  }

  componentWillUnmount() {
    OrgActions.updatePollingStatus(false);
    this.unsubscribeFromOrg();
    this.unsubscribeFromBuilds();
  }

  onStatusChange(state) {
    this.setState(state);

    // load branch modules after we get the builds collection
    if (!state.loadingBuilds && !this.state.loadingOrgRepos) {
      OrgActions.loadRepos(this.props.params);
      this.state.loadingOrgRepos = true;
    }
  }

  render() {
    return (
      <PageContainer>
        <Org
          params={this.props.params}
          repos={this.state.repos}
          loading={this.state.loading}
        />
      </PageContainer>
    );
  }
}

OrgContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default OrgContainer;
