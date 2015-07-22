import React from 'react';
import {bindAll} from 'underscore';
import SearchFilter from '../shared/SearchFilter.jsx';

class SidebarFilter extends React.Component {

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
    console.log('handleFocus() from SidebarFilter.jsx');
    this.props.filterInputFocus(true);
  }

  handleBlur() {
    this.props.filterInputFocus(false);
  }

  render() {
    if (this.props.loading) {
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
  router: React.PropTypes.func.isRequired
};

SidebarFilter.propTypes = {
  updateResults: React.PropTypes.func.isRequired,
  modules: React.PropTypes.array.isRequired,
  loading: React.PropTypes.bool.isRequired,
  filterText: React.PropTypes.string.isRequired,
  filterInputFocus: React.PropTypes.func.isRequired
};

export default SidebarFilter;
