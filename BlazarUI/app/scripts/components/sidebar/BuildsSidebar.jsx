import React, {Component, PropTypes} from 'react';
import $ from 'jquery';
import LazyRender from '../shared/LazyRender.jsx';
import SidebarFilter from './SidebarFilter.jsx';
import fuzzy from 'fuzzy';
import {bindAll, filter, contains, has} from 'underscore';
import SectionLoader from '../shared/SectionLoader.jsx';
import Helpers from '../ComponentHelpers';
import MutedMessage from '../shared/MutedMessage.jsx';
import SidebarItem from '../sidebar/SidebarItem.jsx';

let Link = require('react-router').Link;


class BuildsSidebar extends Component {

  constructor() {
    bindAll(this, 'updateResults', 'setToggleState', 'toggleStar');

    this.state = {
      filterText: '',
      toggleFilterState: 'starred'
    };
  }

  updateResults(input) {
    this.setState({
      filterText: input
    });
  }

  setToggleState(toggleState) {
    const filterText = this.state.filterText;

    this.setState({
      filterText: filterText,
      toggleFilterState: toggleState
    });

  }

  toggleStar(isStarred, moduleInfo) {
    this.props.persistStarChange(isStarred, moduleInfo)
  }

  markStarredModules(modules) {
    modules.map( (module) => {      
      module.module.isStarred = false;
      this.props.stars.forEach( (star) => {
        if (star.moduleId === module.module.id) {
          module.module.isStarred = true;
          return;
        }
      });
    });

    return modules;
  }

  filterByToggle(modules) {
    if (this.state.toggleFilterState === 'starred') {
      const starredModules = Helpers.getStarredModules(this.props.stars, modules);
      return starredModules;
    }

    if (this.state.toggleFilterState === 'building') {
      modules = filter(modules, (module) => {
        return has(module, 'inProgressBuild');
      });
    }

    return modules;
  }

  getFilterMatches() {
    const allBuilds = this.props.builds.all;
    const options = {
      extract: function(el) {
        return el.module.name;
      }
    };

    const results = fuzzy.filter(this.state.filterText, allBuilds, options);
    const matches = results.map(function(el) { return el.original; });
    return matches;
  }

  buildModuleComponents(modules) {
    return modules.map( (module) => {
      if (has(module, 'module')){
        return (
          <SidebarItem 
            key={module.module.id} 
            build={module} 
            isStarred={module.module.isStarred}
            toggleStar={this.toggleStar} 
            persistStarChange={this.props.persistStarChange} />
        )
      }
    });
  }

  render() {

    const matches = this.getFilterMatches();
    const markStarred = this.markStarredModules(matches);
    const filteredByToggle = this.filterByToggle(markStarred);
    const moduleComponents = this.buildModuleComponents(filteredByToggle);

    let sidebarMessage;

    if (this.props.loading) {
      return (
        <SectionLoader />
      );
    }

    if (moduleComponents.length === 0 && this.state.filterText.length > 0) {
      sidebarMessage = (
        <MutedMessage roomy={true}>No matches for {this.state.filterText}.</MutedMessage>
      )
    } 

    // to do: better setup for initial page load
    if (moduleComponents.length === 0 && this.state.toggleFilterState === 'starred') {
      sidebarMessage = (
        <MutedMessage roomy={true}>No starred modules</MutedMessage>
      )
    }

    const headerHeight = $('#primary-nav').height() + $('.sidebar__filter').height();
    const containerHeight = $(window).height() - headerHeight;

    return (
      <div>
        <div className="sidebar__filter">
          <SidebarFilter
            loading={this.props.loading}
            builds={this.props.builds.all}
            filterText={this.state.filterText}
            updateResults={this.updateResults}
            setToggleState={this.setToggleState}
            toggleFilterState={this.state.toggleFilterState}
          />
        </div>

        <div className='sidebar__list'>
          <LazyRender childHeight={71} maxHeight={containerHeight}>
            {moduleComponents}
          </LazyRender>
          {sidebarMessage}
        </div>

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
