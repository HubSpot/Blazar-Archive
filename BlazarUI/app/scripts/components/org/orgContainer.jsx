import React, {Component, PropTypes} from 'react';
import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';

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
    OrgActions.stopPolling();
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
