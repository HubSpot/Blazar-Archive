import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';

// To do: Typeahead in input box
class SearchFilter extends Component {

  constructor() {
    bindAll(this, 'handleChange', 'handleKeyup');
  }

  componentDidMount() {
    window.addEventListener('keyup', this.handleKeyup);
    this.refs.searchFilterInput.getDOMNode().focus();
  }

  componentWillUnmount() {
    window.removeEventListener('keyup', this.handleKeyup);
  }

  handleKeyup(e) {
    if (e.which === 84) {
      this.refs.searchFilterInput.getDOMNode().focus();
    }
  }

  handleChange() {
    this.props.onChange(this.refs.searchFilterInput.getDOMNode().value);
  }

  render() {

    return (
      <input
        type="text"
        ref="searchFilterInput"
        className="search-input form-control"
        placeholder='Filter modules...'
        value={this.props.inputValue}
        onChange={this.handleChange}
      />
    );
  }
}

SearchFilter.propTypes = {
  placeholder: PropTypes.string.isRequired,
  inputValue: PropTypes.string.isRequired,
  options: PropTypes.array.isRequired,
  onChange: PropTypes.func.isRequired,
  showStarred: PropTypes.bool.isRequired
};

export default SearchFilter;
