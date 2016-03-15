import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import SearchFilter from '../shared/SearchFilter.jsx';
import SidebarToggle from './SidebarToggle.jsx';

class SidebarFilter extends Component {

  constructor(props, context) {
    super(props, context);

    this.state = {
      inputValue: ''
    };

    bindAll(this, 'setInputValue', 'toggleFilter');
  }

  setInputValue(value) {
    this.props.updateResults(value);
  }

  toggleFilter(filter) {
    this.props.setToggleState(filter);
  }

  render() {
    if (this.props.loading) {
      return null;
    }

    const selectorClass = `sidebar__filter-selector ${this.props.toggleFilterState}`;

    return (
      <div>
        <SidebarToggle
          toggleFilter={this.toggleFilter} 
          toggleFilterState={this.props.toggleFilterState} />
        <div className={selectorClass} />
        <div className='sidebar__filter-search'>
          <SearchFilter
            ref="buildFilterSearch"
            placeholder='Filter repositories...'
            onChange={this.setInputValue} />
        </div>
      </div>
    );

  }
}

SidebarFilter.contextTypes = {
  router: PropTypes.object.isRequired
};

SidebarFilter.propTypes = {
  updateResults: PropTypes.func.isRequired,
  loading: PropTypes.bool.isRequired,
  filterText: PropTypes.string.isRequired,
  setToggleState: PropTypes.func.isRequired,
  toggleFilterState: PropTypes.string.isRequired
};

export default SidebarFilter;
