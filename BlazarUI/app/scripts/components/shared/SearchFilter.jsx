import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
      import Icon from '../shared/Icon.jsx';

// To do: Typeahead in input box
class SearchFilter extends Component {

  constructor() {
    bindAll(this, 'handleChange');
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

  handleChange() {
    this.props.onChange(this.refs.searchFilterInput.getDOMNode().value);
  }

  render() {

    return (
      <div>
        <Icon name='search' classNames='search-filter__icon' />
        <input
          type="text"
          ref="searchFilterInput"
          className="search-input form-control"
          placeholder='Filter modules...'
          value={this.props.inputValue}
          onChange={this.handleChange}
        />
      </div>
    );
  }
}

SearchFilter.propTypes = {
  placeholder: PropTypes.string.isRequired,
  inputValue: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired
};

export default SearchFilter;
