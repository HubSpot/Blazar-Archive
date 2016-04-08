import React, {Component, PropTypes} from 'react';
import Select from 'react-select-plus';
import {bindAll} from 'underscore';

class Filter extends Component {

  render() {
    if (this.props.loading || this.props.hide) {
      return null;
    }

    return (
      <div className='filter-container'>
        <Select
          placeholder={this.props.placeholder || 'Filter'}
          className={this.props.className}
          name="filter"
          value={this.props.value}
          options={this.props.options}
          onChange={this.props.handleFilterChange}
        />
      </div>
    );
  }
}

Filter.propTypes = {
  value: PropTypes.string.isRequired,
  options: PropTypes.array.isRequired
};

export default Filter;
