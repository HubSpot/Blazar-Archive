import React, {Component, PropTypes} from 'react';
import Org from './Org.jsx';
import PageContainer from '../shared/PageContainer.jsx';
import OrgActions from '../../actions/orgActions';

class OrgContainer extends Component {

  constructor(props) {
    super(props);

    this.state = {
      repos: [],
      loading: true
    };
  }

  componentDidMount() {
    OrgActions.loadRepos(this.props.params);
  }

  componentWillReceiveProps(nextprops) {
    console.log('hi');
    OrgActions.loadRepos(nextprops.params);
  }

  componentWillUnmount() {
    OrgActions.updatePollingStatus(false);
    this.unsubscribe();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  render() {
    return (
      <PageContainer>
        <Org
          params={this.props.params}
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
