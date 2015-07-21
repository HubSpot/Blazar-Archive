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
    // to do: adjust margin based on
    // number of search results
    this.setState({
      isFiltering: status
    });
  }

  getSidebarListClassNames() {
    return `sidebar__list isFiltering--${this.state.isFiltering}`;
  }

  render() {
    // To do: replace loading text with animation
    let loading = this.props.loading ? <div>Loading Projects...</div> : '';
    let groupedRepos = this.props.builds.grouped;

    let sidebarRepoList = [];

    groupedRepos.forEach( (repo) => {

      // To do: fuzzy search
      if (this.state.filterText.length > 0) {

        // match repo name
        let repoMatch = repo.name.toLowerCase().indexOf(this.state.filterText.toLowerCase());

        // or module name
        let moduleMatches = false;
        repo.modules.forEach( (module) => {
          if (module.module.name.toLowerCase().indexOf(this.state.filterText.toLowerCase()) !== -1){
            moduleMatches = true;
          }
        })

        if (repoMatch === -1 && !moduleMatches) {
          return;
        }
      }

      // expand repos to expose modules
      // if our list is less than 3
      let isExpanded = false;
      if (sidebarRepoList.length < 3) {
        isExpanded = true;
      }

      sidebarRepoList.push(
        <ProjectsSidebarListItem isExpanded={isExpanded} filterText={this.state.filterText} key={repo.name} repo={repo} />
      );

    });


    return (
      <div>
        {loading}
        <div className='sidebar__filter'>
          <SidebarFilter
            loading={this.props.loading}
            repos={groupedRepos}
            modules={this.getModulesList()}
            updateResults={this.updateResults}
            filterText={this.state.filterText}
            filterInputFocus={this.filterInputFocus}
          />
        </div>
        <div className={this.getSidebarListClassNames()}>
          {sidebarRepoList}
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
