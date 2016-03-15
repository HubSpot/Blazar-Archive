import React, {Component, PropTypes} from 'react';
import {bindAll, debounce} from 'underscore';
import Icon from '../shared/Icon.jsx';

import SidebarFilterStore from '../../stores/sidebarFilterStore';

// To do: Typeahead in input box
class SearchFilter extends Component {

  constructor() {
    bindAll(this, 'handleChange', 'onStoreChange');
    this.state = {
      searchValue: ''
    };
  }

  componentWillMount() {
    this.handleSearchDebounced = debounce(function () {
      this.props.onChange(this.refs.searchFilterInput.value);
    }, 250);
  }
    
  componentDidMount() {
    window.addEventListener('keyup', this.handleKeyup);
    this.unsubscribeFromSidebarFilter = SidebarFilterStore.listen(this.onStoreChange);
    this.focusInput();
  }

  componentWillUnmount() {
    window.removeEventListener('keyup', this.handleKeyup);
    this.unsubscribeFromSidebarFilter();
  }

  onStoreChange(state) {
    this.setState(state);
    this.handleSearchDebounced();
  }

  focusInput() {
    this.refs.searchFilterInput.focus();
  }

  handleChange() {    
    this.setState({
      searchValue: this.refs.searchFilterInput.value
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
          placeholder='Filter repositories...'
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
