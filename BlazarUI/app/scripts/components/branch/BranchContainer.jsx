import React, {Component, PropTypes} from 'react';
import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import BranchBuildHistoryTable from './BranchBuildHistoryTable.jsx';
import Loader from '../shared/Loader.jsx';
import Icon from '../shared/Icon.jsx';
import GenericErrorMessage from '../shared/GenericErrorMessage.jsx';

import BranchStore from '../../stores/branchStore';
import BranchActions from '../../actions/branchActions';

let initialState = {
  builds: null,
  loading: true
};

class BranchContainer extends Component {

  constructor() {
    this.state = initialState;
  }

  componentDidMount() {
    this.setup(this.props.params);
  }

  componentWillReceiveProps(nextprops) {
    this.tearDown();
    this.setup(nextprops.params);
    this.setState(initialState);
  }

  componentWillUnmount() {
    this.tearDown();
  }

  onStatusChange(state) {
    this.setState(state);
  }
    
  setup(params) {
    this.unsubscribeFromBranch = BranchStore.listen(this.onStatusChange.bind(this));
    BranchActions.loadBranchBuilds(params);
  }
  
  tearDown() {
    BranchActions.stopPolling();
    this.unsubscribeFromBranch();
  }
  
  getRenderedContent() {
    if (this.state.error) {
      return (
        <GenericErrorMessage
          message={this.state.error}
        />
      );
    }
    
    else {
      return (
        <BranchBuildHistoryTable
          data={this.state.builds}
          loading={this.state.loading}
          {...this.state}
          {...this.props}
        />
      );
    }
    
  }

  render() {
    return (
      <PageContainer>
        <UIGrid>
          <UIGridItem size={12}>
            <Headline>
              <Icon type="octicon" name="git-branch" classNames="headline-icon" />
              {this.props.params.repo} - {this.props.params.branch}
              <HeadlineDetail>
                Branch Builds
              </HeadlineDetail>
            </Headline>
            {this.getRenderedContent()}
          </UIGridItem>
        </UIGrid>
      </PageContainer>
    );
  }
}


BranchContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default BranchContainer;
