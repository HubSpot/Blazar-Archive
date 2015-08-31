import React, {Component, PropTypes} from 'react';
import BuildsSidebarListItem from './BuildsSidebarListItem.jsx';
import SidebarFilter from './SidebarFilter.jsx';
import fuzzy from 'fuzzy';
import {bindAll, filter, contains} from 'underscore';
import SectionLoader from '../shared/SectionLoader.jsx';
import StarredProvider from '../StarredProvider';
import LazyRender from '../shared/LazyRender.jsx';

class BuildsSidebar extends Component {

  constructor() {
    bindAll(this, 'updateResults', 'moduleExpandChange', 'updateStarred');

    this.state = {
      filterText: '',
      isFiltering: false,
      expandedRepos: [],
      showStarred: true
    };
  }

  updateResults(input) {
    let showStarred = this.state.showStarred;
    this.setState({
      filterText: input,
      showStarred: showStarred
    });
  }

  updateStarred(showStarred) {
    let filterText = this.state.filterText;
    this.setState({
      filterText: filterText,
      showStarred: showStarred
    });
  }

  getModulesList() {
    let modules = this.props.builds.modules;
    let filterText = this.state.filterText;

    if (filterText.length === 0) {
      return modules;
    }

    let filteredModules = filter(modules, (build) => {
      return build.module.toLowerCase().indexOf(filterText.toLowerCase()) !== -1;
    });

    return filteredModules;
  }

  getFilteredRepos() {
    let matches = [];
    const list = this.props.builds.grouped;

    let options = {
      extract: function(el) {
        let m = '';
        el.modules.forEach( (build) => {
          m += build.module.name;
        });
        return m;
      }
    };

    let results = fuzzy.filter(this.state.filterText, list, options);

    results.map( (el) => {
      if (this.state.showStarred && StarredProvider.hasStar({ repo: el.original.repository, branch: el.original.branch }) === -1) {
        return;
      }
      matches.push({
        filterText: this.state.filterText,
        key: el.original.repository,
        repo: el.original
      });

    });

    return matches;
  }

  moduleExpandChange(id) {

    let expandedIndex = this.state.expandedRepos.indexOf(id);

    if (expandedIndex !== -1) {
      let newExpandedRepos = this.state.expandedRepos.slice();
      newExpandedRepos.splice(expandedIndex, 1);

      this.setState({
        expandedRepos: newExpandedRepos
      });

    } else {
      this.setState({
        expandedRepos: this.state.expandedRepos.concat(id)
      });
    }

  }

  render() {
    if (this.props.loading) {
      return (
        <SectionLoader />
      );
    }

    let filteredRepos = this.getFilteredRepos();
    let expandedState = this.state.expandedRepos;
    let filteredRepoComponents = filteredRepos.map( (item) => {
      let shouldExpand = contains(expandedState, item.repo.id);
      return (
        <BuildsSidebarListItem
          key={item.repo.repoModuleKey}
          isExpanded={shouldExpand}
          filterText={item.filterText}
          repo={item.repo}
          moduleExpandChange={this.moduleExpandChange} />
      );
    });

    let list = '';
    if (this.state.showStarred) {
      list = (
        <div className="sidebar__list">
          {filteredRepoComponents}
        </div>
      );
    } else {
      let h = Math.max(document.documentElement.clientHeight, window.innerHeight || 0) - 80;
      list = (
        <LazyRender maxHeight={h} className="sidebar__list">
          {filteredRepoComponents}
        </LazyRender>
      );
    }

    return (
      <div>
        <div className="sidebar__filter">
          <SidebarFilter
            loading={this.props.loading}
            repos={this.props.builds.grouped}
            modules={this.getModulesList()}
            filterText={this.state.filterText}
            updateResults={this.updateResults}
            updateStarred={this.updateStarred}
            showStarred={this.state.showStarred}
          />
        </div>
        {list}
      </div>
    );
  }
}

BuildsSidebar.propTypes = {
  loading: PropTypes.bool,
  builds: PropTypes.object
};

export default BuildsSidebar;
