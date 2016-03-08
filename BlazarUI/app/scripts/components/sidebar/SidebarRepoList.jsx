import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import LazyRender from '../shared/LazyRender.jsx';
import Loader from '../shared/Loader.jsx';
import SidebarItem from './SidebarItem.jsx';
import {has} from 'underscore';
import {sortBuildsByRepoAndBranch, filterInactiveBuilds} from '../Helpers.js';

let initialState = {
  extraChildHeight: 4
}

class SidebarRepoList extends Component {

  constructor() {
    this.state = initialState;
  }

  refreshSidebar(extraChildDelta) {
    this.setState({
      extraChildHeight: this.state.extraChildHeight + extraChildDelta
    });
  }

  renderBuildsList(builds) {
    return Object.keys(builds).sort().map((repo, i) => {
      const branchesMap = builds[repo];

      const sortedBuilds = 
        sortBuildsByRepoAndBranch(
          filterInactiveBuilds(
            Object.keys(branchesMap).map((branch, i) => {
              return branchesMap[branch];
            })
          )
        );

      if (sortedBuilds.length === 0) {
        return (<span key={i} />);
      }

      return (
        <SidebarItem
          key={i}
          builds={sortedBuilds}
          onExpand={this.refreshSidebar.bind(this)}
          repository={sortedBuilds[0].gitInfo.repository}
        />
      );
    }).filter((build) => {
      return build.type !== 'span';
    });
  }

  render() {
    const {changingBuildsType, filteredBuilds, loading} = this.props;

    if (loading) {
      return null;
    }
    
    if (changingBuildsType) {
      return (
        <Loader align='top-center' className='sidebar-loader' />
      );
    }

    return (
      <LazyRender
        childHeight={67}
        maxHeight={this.props.sidebarHeight}
        extraChildHeight={this.state.extraChildHeight} >
        {this.renderBuildsList(filteredBuilds)}
      </LazyRender>
    );

  }
}

SidebarRepoList.propTypes = {
  sidebarHeight: PropTypes.number.isRequired,
  changingBuildsType: PropTypes.bool.isRequired,
  filteredBuilds: PropTypes.object.isRequired,
  loading: PropTypes.bool.isRequired
};

export default SidebarRepoList;
