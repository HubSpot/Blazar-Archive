import $ from 'jquery';
import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';
import {bindAll, has, debounce} from 'underscore';
import Immutable from 'immutable';

import Sidebar from './Sidebar.jsx';
import SidebarFilter from './SidebarFilter.jsx';
import SidebarRepoList from './SidebarRepoList.jsx';
import SidebarMessage from './SidebarMessage.jsx';
import Loader from '../shared/Loader.jsx';
import AjaxErrorAlert from '../shared/AjaxErrorAlert.jsx';

import StarStore from '../../stores/starStore';
import BuildsStore from '../../stores/buildsStore';
import BuildsActions from '../../actions/buildsActions';
import sidebarTabProvider from '../../services/sidebarTabProvider';

import {NO_MATCH_MESSAGES} from '../constants';
import {sidebarCombine, getFilterMatches} from '../../utils/buildsHelpers';

// $('.sidebar__filter') is inaccessible at render time,
// so use this default until the window is resized
const defaultSidebarFilterHeight = 79;

class SidebarContainer extends Component {

  constructor(props) {
    super(props);
    
    bindAll(this, 
      'onStoreChange',
      'onStarChange', 
      'updateResults', 
      'setToggleState'
    );

    this.state = {
      builds: undefined,
      loading: true,
      changingBuildsType: false,
      filterText: '',
      toggleFilterState: sidebarTabProvider.getSidebarTab(),
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
    this.unsubscribeFromStars = StarStore.listen(this.onStarChange);
    BuildsActions.loadBuilds();
    window.addEventListener('resize', this.handleResizeDebounced);
  }
  
  componentWillUnmount() {
    BuildsActions.stopListening();
    this.unsubscribeFromBuilds();
    window.removeEventListener('resize', this.handleResizeDebounced);
  }
  
  getSidebarHeight() {
    let filterHeight = $('.sidebar__filter').height() || defaultSidebarFilterHeight;
    return $(window).height() - filterHeight;
  }

  onStoreChange(state) {
    this.setState(state);
  }
  
  onStarChange(state) {
    if (this.state.toggleFilterState === 'starred') {
      BuildsActions.loadBuilds();
    }
  }

  updateResults(input) {
    this.setState({
      filterText: input
    });
  }

  setToggleState(toggleState) {
    BuildsActions.loadBuilds();
    sidebarTabProvider.changeTab(toggleState);

    this.setState({
      filterText: this.state.filterText,
      toggleFilterState: toggleState,
      changingBuildsType: true
    });
  }

  render() {
    const {loading, toggleFilterState, filterText, builds, error} = this.state;

    if (loading) {
      return (
        <Sidebar>
          <Loader align='top-center'/>
        </Sidebar>
      );
    }
    
    if (error) {
      return (
        <Sidebar>
          <AjaxErrorAlert error={error} fixed={true} />
        </Sidebar>
      );
    }

    const searchType = NO_MATCH_MESSAGES[toggleFilterState];
    const filteredBuilds = builds[this.state.toggleFilterState];
    const matches = sidebarCombine(getFilterMatches(filteredBuilds.toJS(), filterText));

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
