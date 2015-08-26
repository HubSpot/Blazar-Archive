import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import SearchFilter from '../shared/SearchFilter.jsx';
import StarredToggle from './StarredToggle.jsx';

class SidebarFilter extends Component {

  constructor(props, context) {
    super(props, context);

    this.state = {
      inputValue: '',
      showStarred: true
    };
    bindAll(this, 'handleOptionClick', 'handleChange', 'handleFocus', 'handleBlur');

  }

  setInputValue(value) {
    this.props.updateResults(value);
  }

  linkToBuild(link) {
    this.context.router.transitionTo(link);
  }

  handleChange(value) {
    this.setInputValue(value);
  }

  handleOptionClick(event, build) {
    this.linkToBuild(build.link);
  }

  handleFocus() {
    this.props.filterInputFocus(true);
  }

  handleBlur() {
    this.props.filterInputFocus(false);
  }

  handleSelect() {
    let id = event.target.id;
    if (id === 'starred') {
      this.state.showStarred = true;
    } else {
      this.state.showStarred = false;
    }
    this.forceUpdate();
    this.props.updateStarred(this.state.showStarred);
  }

  render() {

    if (this.props.loading || this.props.repos.length === 0) {
      return <div></div>;
    }

    return (
      <div>
        <div className='sidebar__filter-search'>
          <SearchFilter
            ref="buildFilterSearch"
            placeholder='Filter modules...'
            inputValue={this.props.filterText}
            options={this.props.modules}
            onChange={this.handleChange}
            onBlur={this.handleBlur}
            onFocus={this.handleFocus}
            showStarred={this.state.showStarred}
          />
        </div>
      <StarredToggle onClick={this.handleSelect.bind(this)} showStarred={this.state.showStarred}></StarredToggle>
      </div>
    );

  }
}

SidebarFilter.contextTypes = {
  router: PropTypes.func.isRequired
};

SidebarFilter.propTypes = {
  updateResults: PropTypes.func.isRequired,
  modules: PropTypes.array.isRequired,
  loading: PropTypes.bool.isRequired,
  filterText: PropTypes.string.isRequired,
  filterInputFocus: PropTypes.func.isRequired,
  repos: PropTypes.array.isRequired,
  updateStarred: PropTypes.func.isRequired
};

export default SidebarFilter;
