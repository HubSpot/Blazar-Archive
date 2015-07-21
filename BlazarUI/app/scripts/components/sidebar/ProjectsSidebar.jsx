import React from 'react';
import ProjectsSidebarListItem from './ProjectsSidebarListItem.jsx';
import SidebarFilter from './SidebarFilter.jsx';
import {bindAll, filter} from 'underscore';

class ProjectsSidebar extends React.Component {

  constructor() {
    bindAll(this, 'updateResults', 'filterInputFocus');
    this.state = {
      filterText: '',
      isFiltering: false
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
    return `sidebar__list isFiltering--${this.state.isFiltering}`;
  }

  // To do: fuzzy search
  getFilteredRepos() {
    let matches = [];

    this.props.builds.grouped.forEach( (repo) => {

      if (this.state.filterText.length > 0) {

        let repoMatches = repo.name.toLowerCase().indexOf(this.state.filterText.toLowerCase()) !== -1;

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
        key: repo.name,
        repo: repo
      });

    });

    return matches;
  }


  render() {
    // To do: replace loading text with animation
    let loading = this.props.loading ? <div>Loading Projects...</div> : '';

    let filteredRepos = this.getFilteredRepos();

    let filteredRepoComponents = filteredRepos.map( (item) => {
      return (
        <ProjectsSidebarListItem
          key={item.key}
          isExpanded={filteredRepos.length < 3}
          filterText={item.filterText}
          repo={item.repo}
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
