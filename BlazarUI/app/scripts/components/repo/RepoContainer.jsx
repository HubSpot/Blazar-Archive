import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import PageContainer from '../shared/PageContainer.jsx';

import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Loader from '../shared/Loader.jsx';
import Icon from '../shared/Icon.jsx';
import BranchFilter from './BranchFilter.jsx';
import BranchesTable from './BranchesTable.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';
import {getFilteredModules} from '../Helpers.js'

import RepoStore from '../../stores/repoStore';
import RepoActions from '../../actions/repoActions';

class RepoContainer extends Component {

  constructor() {
    bindAll(this, 'onStatusChange', 'updateFilters');

    this.state = {
      branches: [],
      loading: true,
      filters: {
        branch: [],
        module: []
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
    RepoActions.loadBranches(false);
    this.unsubscribeFromRepo();
  }

  onStatusChange(state) {
    this.setState(state);
  }
  
  updateFilters(newFilters) {
    this.setState({
      filters: newFilters,
    });
  }

  render() {
    return (
      <PageContainer>
        <UIGrid>
          <UIGridItem size={12}>
            <Headline>
              <Icon type="octicon" name="repo" classNames="headline-icon" />
              <span>{this.props.params.repo}</span>
              <HeadlineDetail>
                All Modules
              </HeadlineDetail>
            </Headline>
            <BranchFilter 
              branches={this.state.branches}
              branchFilter={this.state.branchFilter}
              filters={this.state.filters}
              updateFilters={this.updateFilters}
              loading={this.state.loading}
            />
            <BranchesTable 
              modules={getFilteredModules(this.state.filters, this.state.branches)}
              loading={this.state.loading}
            />
          </UIGridItem>
        </UIGrid>
      </PageContainer>
    );
  }
}

RepoContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default RepoContainer;
