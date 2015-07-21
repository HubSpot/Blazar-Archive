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
        let match = repo.name.toLowerCase().indexOf(this.state.filterText.toLowerCase());
        if (match === -1) {
          return;
        }
      }

      sidebarRepoList.push(
        <ProjectsSidebarListItem filterText={this.state.filterText} key={repo.name} repo={repo} />
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
