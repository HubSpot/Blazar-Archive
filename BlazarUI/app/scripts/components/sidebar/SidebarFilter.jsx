import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import SearchFilter from '../shared/SearchFilter.jsx';

class SidebarFilter extends Component {

  constructor(props, context) {
    super(props, context);

    this.state = {
      inputValue: ''
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

  render() {

    if (this.props.loading || this.props.repos.length === 0) {
      return <div></div>;
    }

    return (
      <SearchFilter
        ref="buildFilterSearch"
        placeholder='Filter modules...'
        inputValue={this.props.filterText}
        options={this.props.modules}
        onChange={this.handleChange}
        onBlur={this.handleBlur}
        onFocus={this.handleFocus}
      />
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
  repos: PropTypes.array.isRequired
};

export default SidebarFilter;
