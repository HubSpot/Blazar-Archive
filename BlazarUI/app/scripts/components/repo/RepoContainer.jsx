import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import PageContainer from '../shared/PageContainer.jsx';
import Immutable from 'immutable';

import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Loader from '../shared/Loader.jsx';
import Icon from '../shared/Icon.jsx';
import BranchFilter from './BranchFilter.jsx';
import BranchesTable from './BranchesTable.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';
import {getFilteredBranches, filterInactiveBuilds, sortBranchesByTimestamp} from '../Helpers.js'
import GenericErrorMessage from '../shared/GenericErrorMessage.jsx';

import RepoStore from '../../stores/repoStore';
import RepoActions from '../../actions/repoActions';

let initialState = {
  branches: Immutable.List.of(),
  loading: true,
  filters: {
    branch: [],
    repo: []
  },
  error: null
};

class RepoContainer extends Component {

  constructor() {
    bindAll(this, 'onStatusChange', 'updateFilters');
    this.state = initialState;
  }

  componentDidMount() {
    this.setup(this.props.params);
  }

  componentWillReceiveProps(nextprops) {
    this.setState(initialState);
    this.tearDown();
    this.setup(nextprops.params);
  }

  componentWillUnmount() {
    this.tearDown();
  }

  onStatusChange(state) {
    if (state.error) {
      state.loading = false;
    }
    this.setState(state);
  }

  setup(params) {
    this.unsubscribeFromRepo = RepoStore.listen(this.onStatusChange);
    RepoActions.loadBranches(params);
  }
  
  tearDown() {
    RepoActions.stopPolling();
    this.unsubscribeFromRepo();
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
                Branches
              </HeadlineDetail>
            </Headline>
            <GenericErrorMessage
              message={this.state.error}
            />
            <BranchFilter
              hide={this.state.error}
              updateFilters={this.updateFilters}
              {...this.state}
            />
            <BranchesTable 
              hide={this.state.error}
              {...this.state}
              branches={sortBranchesByTimestamp(filterInactiveBuilds(getFilteredBranches(this.state.filters, this.state.branches)))}
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
