import React, {Component, PropTypes} from 'react';
import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import BranchBuildHistoryTable from './BranchBuildHistoryTable.jsx';
import BranchHeadline from './BranchHeadline.jsx';
import Loader from '../shared/Loader.jsx';
import GenericErrorMessage from '../shared/GenericErrorMessage.jsx';
import BuildButton from './BuildButton.jsx';

import StarStore from '../../stores/starStore';
import StarActions from '../../actions/starActions';

import BranchStore from '../../stores/branchStore';
import BranchActions from '../../actions/branchActions';

let initialState = {
  builds: null,
  stars: [],
  loadingBranches: true,
  loadingStars: true
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
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange.bind(this));
    StarActions.loadStars('repoBuildContainer');
    BranchActions.loadBranchBuilds(params);
  }
  
  tearDown() {
    BranchActions.stopPolling();
    this.unsubscribeFromStars();
    this.unsubscribeFromBranch();
  }
  
  triggerBuild() {
    BranchActions.triggerBuild();
  }
  
  renderTable() {
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
          loading={this.state.loadingBranches}
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
          <UIGridItem size={10}>
            <BranchHeadline
              loading={this.state.loadingStars || this.state.loadingBranches}
              {...this.state}
              {...this.props}
            />
            </UIGridItem>
            <UIGridItem size={2} align='RIGHT'>
              <BuildButton 
                triggerBuild={this.triggerBuild} 
                loading={this.state.loadingBranches}
                error={this.state.error}
              />
            </UIGridItem>
            <UIGrid>
              <UIGridItem size={12} align='RIGHT'>
                {this.renderTable()}
              </UIGridItem>
            </UIGrid>
        </UIGrid>
      </PageContainer>
    );
  }
}


BranchContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default BranchContainer;
