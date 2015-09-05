import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';

// To do: Typeahead in input box
class SearchFilter extends Component {

  constructor() {
    bindAll(this, 'handleChange', 'handleKeyup');
  }

  componentDidMount() {
    window.addEventListener('keyup', this.handleKeyup);
    this.focusInput();
  }

  componentDidUpdate() {
    this.focusInput();
  }

  componentWillUnmount() {
    window.removeEventListener('keyup', this.handleKeyup);
  }

  focusInput() {
    this.refs.searchFilterInput.getDOMNode().focus();
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
  onChange: PropTypes.func.isRequired
};

export default SearchFilter;
