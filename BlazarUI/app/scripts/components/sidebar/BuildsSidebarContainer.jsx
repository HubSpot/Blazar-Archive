import $ from 'jquery';
import React, {Component} from 'react';
import SidebarFilter from './SidebarFilter.jsx';
import BuildsSidebarMessage from './BuildsSidebarMessage.jsx';
import {bindAll, has} from 'underscore';
import {getFilterMatches} from '../../utils/buildsHelpers';
import {FILTER_MESSAGES, NO_MATCH_MESSAGES} from '../constants';
import Loader from '../shared/Loader.jsx';
import LazyRender from '../shared/LazyRender.jsx';
import SidebarItem from './SidebarItem.jsx';
let Link = require('react-router').Link;

import StarredBuildsStore from '../../stores/starredBuildsStore';
import StarredBuildsActions from '../../actions/starredBuildsActions';

import Sidebar from './Sidebar.jsx';

class BuildsSidebarContainer extends Component {

  constructor(props) {
    super(props);
    
    bindAll(this, 'persistStarChange', 'onBuildsStatusChange', 'getBuildsOfType', 'updateResults', 'setToggleState');

    this.state = {
      builds: [],
      loadingBuilds: true,
      loading: true,
      changingBuildsType: false,
      filterText: '',
      toggleFilterState: 'starred'
    };
  }

  componentDidMount() {
    this.unsubscribeFromStarredBuilds = StarredBuildsStore.listen(this.onBuildsStatusChange);
    StarredBuildsActions.loadBuilds(this.state.toggleFilterState);
  }

  componentWillUnmount() {
    StarredBuildsActions.stopListening();
    this.unsubscribeFromStarredBuilds();
  }
  
  getBuildsOfType(type) {    
    this.setState({
      changingBuildsType: true
    });
    StarredBuildsActions.loadBuildOfType(type)
  }

  onBuildsStatusChange(state) {  
    if (state.filterHasChanged) {
      state.changingBuildsType = false;
    }
    if (!this.state.loadingStars) {
      state.loading = false;
    }

    this.setState(state);
  }

  persistStarChange(isStarred, starInfo) {
    StarActions.toggleStar(isStarred, starInfo);
  }

  updateResults(input) {
    this.setState({
      filterText: input
    });
  }

  setToggleState(toggleState) {
    this.getBuildsOfType(toggleState);

    this.setState({
      filterText: this.state.filterText,
      toggleFilterState: toggleState
    });
  }

  buildModuleComponents(changingBuildsType, modules) {
    if (changingBuildsType && this.state.toggleFilterState === 'all') {
      return (
        <Loader align='top-center' className='sidebar-loader' />
      );
    }

    const modulesList = modules.map( (module) => {
      if (has(module, 'module')) {
        return (
          <SidebarItem 
            key={module.module.id}
            build={module} 
            isStarred={module.module.isStarred}
          />
        )
      }
    });

    return (
      <LazyRender 
        childHeight={71} 
        maxHeight={$(window).height() - $('#primary-nav').height() + $('.sidebar__filter').height()}>
        {modulesList}
      </LazyRender>
    );
  }
  
  render() {
    if (this.state.loading) {
      return (
        <Loader align='top-center' />
      );
    }
    // filter builds by search input
    const matches = getFilterMatches(this.state.builds, this.state.filterText);
    // build list item components
    const moduleComponentsList = this.buildModuleComponents(this.state.changingBuildsType, matches);
    // pass type of builds we are searching to provide messages
    const searchType = NO_MATCH_MESSAGES[this.state.toggleFilterState];
    return (
      <Sidebar>
        <div className="sidebar__filter">
          <SidebarFilter
            loading={this.state.loading}
            builds={this.state.builds}
            filterText={this.state.filterText}
            updateResults={this.updateResults}
            setToggleState={this.setToggleState}
            toggleFilterState={this.state.toggleFilterState}
          />
        </div>
        <div className='sidebar__list'>
          {moduleComponentsList}
          <BuildsSidebarMessage
            searchType={searchType}
            numModules={moduleComponentsList.length}
            filterText={this.state.filterText}
            toggleFilterState={this.state.toggleFilterState}
          />
        </div>
      </Sidebar>
    );
  }



}

export default BuildsSidebarContainer;
