import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import PageContainer from '../shared/PageContainer.jsx';

import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Icon from '../shared/Icon.jsx';
import BranchFilter from './BranchFilter.jsx';
import BranchesTable from './BranchesTable.jsx';
import {getFilteredBranches, filterInactiveBuilds} from '../Helpers';
import GenericErrorMessage from '../shared/GenericErrorMessage.jsx';

import BuildsStore from '../../stores/buildsStore';

class RepoContainer extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'onStatusChange', 'updateFilters');

    this.state = {
      builds: [],
      loading: true,
      filters: {
        branch: [],
        repo: []
      },
      error: null
    };
  }

  componentDidMount() {
    this.unsubscribeFromBuilds = BuildsStore.listen(this.onStatusChange);
    this.onMount();
  }

  componentWillReceiveProps(nextprops) {
    this.setState({
      filters: {
        branch: [],
        repo: []
      },
      error: null,
      builds: this.getFilteredBuilds(nextprops, BuildsStore.getBuilds().all)
    });
  }

  componentWillUnmount() {
    this.unsubscribeFromBuilds();
  }

  onMount() {
    const builds = BuildsStore.getBuilds().all;

    this.setState({
      builds: this.getFilteredBuilds(this.props, builds),
      loading: !builds.length
    });
  }

  onStatusChange(status) {
    if (status.error && this.state.loading) {
      this.setState({
        loading: false,
        error: 'Unable to load builds.'
      });
    } else if (status.builds) {
      this.setState({
        loading: false,
        builds: this.getFilteredBuilds(this.props, status.builds.all),
        error: null
      });
    }
  }

  updateFilters(newFilters) {
    this.setState({
      filters: newFilters,
    });
  }

  getFilteredBuilds(props, builds) {
    return builds.filter(build => {
      return build.gitInfo.repository.toLowerCase() === props.params.repo.toLowerCase();
    });
  }

  render() {
    return (
      <PageContainer documentTitle={this.props.params.repo}>
        <UIGrid>
          <UIGridItem size={12}>
            <Headline>
              <Icon type="octicon" name="repo" classNames="headline-icon" />
              <span>{this.props.params.repo}</span>
              <HeadlineDetail>
                Branches
              </HeadlineDetail>
            </Headline>
            <GenericErrorMessage message={this.state.error} />
            <BranchFilter
              hide={this.state.error}
              updateFilters={this.updateFilters}
              {...this.state}
              branches={this.state.builds}
            />
            <BranchesTable
              hide={this.state.error}
              {...this.state}
              branches={filterInactiveBuilds(getFilteredBranches(this.state.filters, this.state.builds))}
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
