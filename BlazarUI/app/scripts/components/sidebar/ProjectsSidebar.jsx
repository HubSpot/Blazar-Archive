import React from 'react';
import ProjectsSidebarListItem from './ProjectsSidebarListItem.jsx';
import SidebarFilter from './SidebarFilter.jsx';
import {bindAll, filter, contains} from 'underscore';


class ProjectsSidebar extends React.Component {

  constructor() {
    bindAll(this, 'updateResults', 'filterInputFocus', 'moduleExpandChange');
    this.state = {
      filterText: '',
      isFiltering: false,
      expandedRepos: []
    };
  }

  updateResults(input) {
    this.setState({
      filterText: input
    });
  }

  getModulesList() {
    let modules = this.props.builds.modules;
    let filterText = this.state.filterText;

    if (filterText.length === 0) {
      return modules;
    }
    // To do: fuzzy search
    let filteredModules = filter(modules, (build) => {
      return build.module.toLowerCase().indexOf(filterText.toLowerCase()) !== -1;
    });
    return filteredModules;
  }

  filterInputFocus(status) {
    // to do: adjust margin below search box
    // based on number of search results
    this.setState({
      isFiltering: status
    });
  }

  getSidebarListClassNames() {
    let moduleLength = '';
    if (this.getModulesList().length < 3) {
      moduleLength = 'short';
    }
    return `sidebar__list isFiltering--${this.state.isFiltering} isFiltering--${moduleLength}`;
  }

  // To do: fuzzy search
  getFilteredRepos() {
    let matches = [];

    this.props.builds.grouped.forEach( (repo) => {

      if (this.state.filterText.length > 0) {

        let repoMatches = repo.repository.toLowerCase().indexOf(this.state.filterText.toLowerCase()) !== -1;

        let anyModuleMatches = false;
        repo.modules.forEach( (mod) => {
          if (mod.module.name.toLowerCase().indexOf(this.state.filterText.toLowerCase()) !== -1) {
            anyModuleMatches = true;
          }
        });

        if (!repoMatches && !anyModuleMatches) {
          return;
        }
      }

      matches.push({
        filterText: this.state.filterText,
        key: repo.repository,
        repo: repo
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
    // To do: replace loading text with animation
    let loading = this.props.loading ? <div>Loading Projects...</div> : '';

    let filteredRepos = this.getFilteredRepos();
    let expandedState = this.state.expandedRepos;

    let filteredRepoComponents = filteredRepos.map( (item) => {

      let shouldExpand = contains(expandedState, item.repo.id);

      return (
        <ProjectsSidebarListItem
          key={item.key}
          isExpanded={filteredRepos.length < 3 || shouldExpand}
          filterText={item.filterText}
          repo={item.repo}
          moduleExpandChange={this.moduleExpandChange}
        />
      );
    });

    return (
      <div>
        {loading}
        <div className='sidebar__filter'>
          <SidebarFilter
            loading={this.props.loading}
            repos={this.props.builds.grouped}
            modules={this.getModulesList()}
            updateResults={this.updateResults}
            filterText={this.state.filterText}
            filterInputFocus={this.filterInputFocus}
          />
        </div>
        <div className={this.getSidebarListClassNames()}>
          {filteredRepoComponents}
        </div>
      </div>
    );

  }

}

ProjectsSidebar.propTypes = {
  loading: React.PropTypes.bool,
  builds: React.PropTypes.object
};

export default ProjectsSidebar;
