import React, {Component, PropTypes} from 'react';
import LazyRender from '../shared/LazyRender.jsx';
import SidebarItem from './SidebarItem.jsx';
import {sortBuildsByRepoAndBranch, filterInactiveBuilds} from '../Helpers.js';

class SidebarRepoList extends Component {

  constructor(props) {
    super(props);

    this.state = {
      extraChildHeight: 4
    };

    this.refreshSidebar = this.refreshSidebar.bind(this);
  }

  refreshSidebar(extraChildDelta) {
    this.setState({
      extraChildHeight: this.state.extraChildHeight + extraChildDelta
    });
  }

  renderBuildsList(builds) {
    let buildKeys = Object.keys(builds);

    if (this.props.filterText === '') {
      buildKeys = buildKeys.sort();
    }

    return buildKeys.map((repo, i) => {
      const branchesMap = builds[repo];

      let sortedBuilds =
        filterInactiveBuilds(
          Object.keys(branchesMap).map((branch) => {
            return branchesMap[branch];
          })
        );

      if (this.props.filterText === '') {
        sortedBuilds = sortBuildsByRepoAndBranch(sortedBuilds);
      }

      if (sortedBuilds.length === 0) {
        return (<span key={i} />);
      }

      return (
        <SidebarItem
          key={i}
          builds={sortedBuilds}
          onExpand={this.refreshSidebar}
          repository={sortedBuilds[0].gitInfo.repository}
        />
      );
    }).filter((build) => {
      return build.type !== 'span';
    });
  }

  render() {
    const {filteredBuilds, loading} = this.props;

    if (loading) {
      return null;
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
  filteredBuilds: PropTypes.object.isRequired,
  loading: PropTypes.bool.isRequired,
  filterText: PropTypes.string.isRequired
};

export default SidebarRepoList;
