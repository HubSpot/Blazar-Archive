import React from 'react';
import Typeahead from 'react-typeahead-component';
import {bindAll} from 'underscore';
import SidebarFilterOption from './SidebarFilterOption.jsx';


class SidebarFilter extends React.Component {

  constructor(props, context) {
    super(props, context);

    this.state = {
      inputValue: ''
    };
    bindAll(this, 'handleOptionClick', 'handleChange', 'handleFocus', 'handleBlur', 'onKeyDown');

  }

  setInputValue(value) {
    this.props.updateResults(value);
  }

  linkToBuild(link) {
    this.context.router.transitionTo(link);
  }

  handleChange(event) {
    this.setInputValue(event.target.value);
  }

  onKeyDown(event, build) {
    if (event.keyCode === 13) {
      this.linkToBuild(build.link);
    }
    if (event.keyCode === 27) {
      this.setInputValue('');
      this.props.filterInputFocus(false);
    }
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
    if (this.props.loading) {
      return <div></div>;
    }

    return (
      <Typeahead
        ref='typeahead'
        placeholder='Filter modules...'
        inputValue={this.props.filterText}
        options={this.props.modules}
        onChange={this.handleChange}
        optionTemplate={SidebarFilterOption}
        onOptionClick={this.handleOptionClick}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
        onKeyDown={this.onKeyDown}
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
