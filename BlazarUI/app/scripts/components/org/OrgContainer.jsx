import React, {Component, PropTypes} from 'react';
import Org from './Org.jsx';
import PageContainer from '../shared/PageContainer.jsx';
import OrgActions from '../../actions/orgActions';
import OrgStore from '../../stores/orgStore';


class OrgContainer extends Component {

  constructor() {
    this.state = {
      repos: [],
      loading: true
    };
  }

  componentDidMount() {
    this.unsubscribeFromOrg = OrgStore.listen(this.onStatusChange.bind(this));
    OrgActions.loadRepos(this.props.params);
  }

  componentWillReceiveProps(nextprops) {
    OrgActions.loadRepos(nextprops.params);
  }

  componentWillUnmount() {
    OrgActions.updatePollingStatus(false);
    this.unsubscribeFromOrg();
  }

  onStatusChange(state) {
    this.setState(state);
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
