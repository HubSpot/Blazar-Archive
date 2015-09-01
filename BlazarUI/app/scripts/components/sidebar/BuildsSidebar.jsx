import React, {Component, PropTypes} from 'react';
import BuildsSidebarListItem from './BuildsSidebarListItem.jsx';
import SidebarFilter from './SidebarFilter.jsx';
import fuzzy from 'fuzzy';
import {bindAll, filter, contains} from 'underscore';
import SectionLoader from '../shared/SectionLoader.jsx';
import StarredProvider from '../StarredProvider';
import LazyRender from '../shared/LazyRender.jsx';
import Helpers from '../ComponentHelpers';

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

  getStoredStars(results) {
    let matches = [];    

    results.map( (el) => {

      if (this.state.showStarred && !Helpers.isStarred(this.props.stars, el.original.repository, el.original.branch)){ 
        return; 
      }

      matches.push({
        filterText: this.state.filterText,
        key: el.original.repository,
        repo: el.original
      });

    }.bind(this));
    return matches;
  }

  getFilteredRepos() {
    const groupedList = this.props.builds.grouped;
    const options = {
      extract: function(el) {
        let m = '';
        el.modules.forEach( (build) => { m += build.module.name; });
        return m;
      }
    };

    return fuzzy.filter(this.state.filterText, groupedList, options);
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

  // Build listing of BuildsSidebarListItem
  getFilteredRepoComponents() {
    const filteredRepos = this.getStoredStars(this.getFilteredRepos());
    const expandedState = this.state.expandedRepos;

    return filteredRepos.map( (item) => {
      const shouldExpand = contains(expandedState, item.repo.id);
      const isStarred = Helpers.isStarred(this.props.stars, item.repo.repository, item.repo.branch)

      return (
        <BuildsSidebarListItem
          isStarred={isStarred}
          persistStarChange={this.props.persistStarChange}
          key={item.repo.repoModuleKey}
          isExpanded={shouldExpand}
          filterText={item.filterText}
          repo={item.repo}
          moduleExpandChange={this.moduleExpandChange} />
      );
    }.bind(this));
  }

  // Final listing of <BuildsSidebarListItem />'s
  getList() {
    const filteredRepoComponents = this.getFilteredRepoComponents();
    let list;
    // Show starred branches only
    if (this.state.showStarred) {
      list = (
        <div className="sidebar__list">
          {filteredRepoComponents}
        </div>
      );
    } else {
      // Show all branches
      let h = Math.max(document.documentElement.clientHeight, window.innerHeight || 0) - 80;
      list = (
        <LazyRender maxHeight={h} className="sidebar__list">
          {filteredRepoComponents}
        </LazyRender>
      );
    }
    return list;
  }

  render() {
    const list = this.getList();

    if (this.props.loading) {
      return (
        <SectionLoader />
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
  loading: PropTypes.bool.isRequired,
  builds: PropTypes.object.isRequired,
  stars: PropTypes.array.isRequired,
  persistStarChange: PropTypes.func.isRequired
};

export default BuildsSidebar;
