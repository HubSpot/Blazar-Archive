import React, {Component, PropTypes} from 'react';
import ProjectsSidebarListItem from './ProjectsSidebarListItem.jsx';
import SidebarFilter from './SidebarFilter.jsx';
import fuzzy from 'fuzzy';
import {bindAll, filter, contains} from 'underscore';
import SectionLoader from '../shared/SectionLoader.jsx';

class ProjectsSidebar extends Component {

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

  getFilteredRepos() {
    let matches = [];
    let list = this.props.builds.grouped;

    // To do: add emphasis on matched letters
    let options = {
      // pre: '<strong>' ,
      // post: '>' ,
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
        <ProjectsSidebarListItem
          key={item.repo.repoModuleKey}
          isExpanded={filteredRepos.length < 4 || shouldExpand}
          filterText={item.filterText}
          repo={item.repo}
          moduleExpandChange={this.moduleExpandChange}
        />
      );

    });

    return (
      <div>
        <div className="sidebar__filter">
          <SidebarFilter
            loading={this.props.loading}
            repos={this.props.builds.grouped}
            modules={this.getModulesList()}
            filterText={this.state.filterText}
            filterInputFocus={this.filterInputFocus}
            updateResults={this.updateResults}
          />
        </div>
        <div className="sidebar__list">
          {filteredRepoComponents}
        </div>
      </div>
    );

  }

}

ProjectsSidebar.propTypes = {
  loading: PropTypes.bool,
  builds: PropTypes.object
};

export default ProjectsSidebar;
