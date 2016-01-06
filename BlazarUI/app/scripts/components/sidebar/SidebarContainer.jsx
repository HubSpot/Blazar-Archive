import $ from 'jquery';
import React, {Component} from 'react';
import {Link} from 'react-router';
import {bindAll, has, debounce} from 'underscore';

import Sidebar from './Sidebar.jsx';
import SidebarFilter from './SidebarFilter.jsx';
import SidebarRepoList from './SidebarRepoList.jsx';
import SidebarMessage from './SidebarMessage.jsx';
import Loader from '../shared/Loader.jsx';

import BuildsStore from '../../stores/buildsStore';
import BuildsActions from '../../actions/buildsActions';

import {NO_MATCH_MESSAGES} from '../constants';
import {getFilterMatches} from '../../utils/buildsHelpers';

class SidebarContainer extends Component {

  constructor(props) {
    super(props);
    
    bindAll(this, 
      'onStoreChange', 
      'getBuildsOfType', 
      'updateResults', 
      'setToggleState'
    );

    this.state = {
      builds: [],
      loading: true,
      changingBuildsType: false,
      filterText: '',
      toggleFilterState: 'starred',
      sidebarHeight: this.getSidebarHeight()
    };
  }

  componentWillMount() {
    this.handleResizeDebounced = debounce(() => {
      this.setState({
        sidebarHeight: this.getSidebarHeight()
      });
    }, 500);
  }

  componentDidMount() {
    this.unsubscribeFromBuilds = BuildsStore.listen(this.onStoreChange);
    BuildsActions.loadBuilds(this.state.toggleFilterState);
    window.addEventListener('resize', this.handleResizeDebounced);
  }
  
  componentWillUnmount() {
    BuildsActions.stopListening();
    this.unsubscribeFromBuilds();
    window.removeEventListener('resize', this.handleResizeDebounced);
  }
  
  getSidebarHeight() {
    return $(window).height() - $('#primary-nav').height() + $('.sidebar__filter').height();
  }

  // fetch builds based on toggle selection
  getBuildsOfType(type) {
    this.setState({
      changingBuildsType: true
    });
    
    BuildsActions.loadBuilds(type);
  }

  onStoreChange(state) {
    this.setState(state);
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

  render() {
    if (this.state.loading) {
      return (
        <Sidebar>
          <Loader align='top-center' />
        </Sidebar>
      );
    }

    const searchType = NO_MATCH_MESSAGES[this.state.toggleFilterState];
    const matches = getFilterMatches(this.state.builds.toJS(), this.state.filterText);

    return (
      <Sidebar>
        <div className="sidebar__filter">
          <SidebarFilter
            {...this.state}
            updateResults={this.updateResults}
            setToggleState={this.setToggleState}
          />
        </div>
        <div className='sidebar__list'>
          <SidebarRepoList 
            filteredBuilds={matches}
            {...this.state}
            {...this.props}
          />
          <SidebarMessage
            searchType={searchType}
            numberOfBuilds={matches.length}
            {...this.state}
          />
        </div>
      </Sidebar>
    );
  }

}

export default SidebarContainer;
