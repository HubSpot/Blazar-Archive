import React, {Component, PropTypes} from 'react';
import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import ReposTable from './ReposTable.jsx';
import Icon from '../shared/Icon.jsx';

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
    this.setState({
      loading: true
    });
    OrgActions.loadRepos(nextprops.params);
  }

  componentWillUnmount() {
    OrgActions.loadRepos(false);
    this.unsubscribeFromOrg();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  render() {
    return (
      <PageContainer>
        <UIGrid>
          <UIGridItem size={12}>
            <Headline>
              <Icon type="octicon" name="organization" classNames="headline-icon" />
              <span>{this.props.params.org}</span>
              <HeadlineDetail>
                Repositories
              </HeadlineDetail>
            </Headline>
            <ReposTable
              repos={this.state.repos}
              org={this.props.params.org}
              host={this.props.params.host}
              loading={this.state.loading}
            />
          </UIGridItem>
        </UIGrid>
      </PageContainer>
    );
  }
}

OrgContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default OrgContainer;
