import React, {Component, PropTypes} from 'react';
import {bindAll, debounce} from 'underscore';
import Icon from '../shared/Icon.jsx';

// To do: Typeahead in input box
class SearchFilter extends Component {

  constructor() {
    bindAll(this, 'handleChange');
    this.state = {
      searchValue: ''
    }
  }

  componentWillMount() {
    this.handleSearchDebounced = debounce(function () {
      this.props.onChange(this.refs.searchFilterInput.getDOMNode().value);
    }, 250);
    
  }
    
  componentDidMount() {
    window.addEventListener('keyup', this.handleKeyup);
    this.focusInput();
  }

  componentWillUnmount() {
    window.removeEventListener('keyup', this.handleKeyup);
  }

  focusInput() {
    this.refs.searchFilterInput.getDOMNode().focus();
  }

  handleChange() {    
    this.setState({
      searchValue: this.refs.searchFilterInput.getDOMNode().value
    });
    this.handleSearchDebounced();
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
          value={this.state.searchValue}
          onChange={this.handleChange}
        />
      </div>
    );
  }
}

SearchFilter.propTypes = {
  placeholder: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired
};

export default SearchFilter;
