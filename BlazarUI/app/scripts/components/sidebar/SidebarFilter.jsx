import React from 'react';
import Typeahead from 'react-typeahead-component';
import {bindAll} from 'underscore';
// import SidebarFilterOption from './SidebarFilterOption.jsx';



class SidebarFilterOption extends React.Component {

  render() {
    return (
      <div>
        {this.renderOption()}
      </div>
    );
  }

  renderOption() {
    let optionData = this.props.data.module;
    let inputValue = this.props.userInputValue;

    if (optionData.indexOf(inputValue) === 0) {
      return (
        <span>
          {inputValue}
          <strong>
            {optionData.slice(inputValue.length)}
          </strong>
        </span>
      );
    }
    return optionData;
  }

}

SidebarFilterOption.propTypes = {
  data: React.PropTypes.any,
  inputValue: React.PropTypes.string,
  isSelected: React.PropTypes.bool,
  userInputValue: React.PropTypes.string
};





class SidebarFilter extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      inputValue: ''
    };
    bindAll(this, 'handleOptionClick', 'handleChange', 'handleFocus', 'handleBlur', 'onKeyDown');
  }

  setInputValue(value) {
    this.props.updateResults(value);
  }

  // to do: use react link so we dont have
  // a full page refresh
  linkToBuild(link) {
    window.location = link;
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

SidebarFilter.propTypes = {
  updateResults: React.PropTypes.func.isRequired,
  modules: React.PropTypes.array.isRequired,
  loading: React.PropTypes.bool.isRequired,
  filterText: React.PropTypes.string.isRequired,
  filterInputFocus: React.PropTypes.func.isRequired
};

export default SidebarFilter;
