import React, {Component, PropTypes} from 'react';
import $ from 'jquery';
import LazyRender from '../shared/LazyRender.jsx';
import BuildsSidebarListItem from './BuildsSidebarListItem.jsx';
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
    bindAll(this, 'updateResults', 'updateStarred');

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

  getBuildsList() {
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



  // getStoredStars(results) {
  //   let matches = [];    

  //   results.map( (el) => {

  //     if (this.state.showStarred && !Helpers.isStarred(this.props.stars, el.original.repository, el.original.branch)){ 
  //       return; 
  //     }

  //     matches.push({
  //       filterText: this.state.filterText,
  //       key: el.original.repository,
  //       repo: el.original
  //     });

  //   }.bind(this));
  //   return matches;
  // }



  getFilteredRepos() {
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



  getFilteredRepoComponents() {
    const filteredRepos = this.getFilteredRepos();

    return filteredRepos.map( (build) => {
      if (has(build, 'module')){
        return (
          <SidebarItem key={build.module.id} build={build} />
        )
      }
    })

  }


  render() {
    let moduleComponents = this.getFilteredRepoComponents();
    let sidebarMessage;

    if (this.props.loading) {
      return (
        <SectionLoader />
      );
    }

    if (moduleComponents.length === 0) {
      sidebarMessage = (
        <MutedMessage roomy={true}>No matches for {this.state.filterText}.</MutedMessage>
      )
    }

    const height = $(window).height();

    return (
      <div>
        <div className="sidebar__filter">
          <SidebarFilter
            loading={this.props.loading}
            builds={this.props.builds.all}
            filterText={this.state.filterText}
            updateResults={this.updateResults}
            updateStarred={this.updateStarred}
            showStarred={this.state.showStarred}
          />
        </div>
        

        <div className='sidebar__list'>
        
          <LazyRender maxHeight={700}>
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
