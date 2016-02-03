import React, {Component, PropTypes} from 'react';
import Select from 'react-select';
import {bindAll, uniq, flatten} from 'underscore';
import {uniqueBranches} from '../Helpers';

class BranchFilter extends Component {

  constructor() {
    bindAll(this, 'handleBranchFilterChange', 'handleModuleFilterChange', 'handleFilterFocus', 'handleFilterBlur');  
    
    this.filters = {
      branch: [],
      module: []
    };

    this.state = {
      filteringInProgress: false
    }
  }

  shouldComponentUpdate() {
    return !this.state.filteringInProgress;
  }
  
  handleFilterFocus() {
    this.setState({
      filteringInProgress: true
    });
  }
  
  handleFilterBlur() {
    this.setState({
      filteringInProgress: false
    })
  }
  
  handleBranchFilterChange(val, multiVal) {
    this.filters.branch = multiVal;
    this.updateFilterProps()
  }

  handleModuleFilterChange(val, multiVal) {
    this.filters.module = multiVal;
    this.updateFilterProps()
  }
  
  updateFilterProps() {
    this.props.updateFilters(this.filters);
  }

  render() {
    if (this.props.loading || this.props.hide) {
      return null;
    }

    return (
      <div className='filter-container branch-filter'>
        <Select
          onFocus={this.handleFilterFocus}
          onBlur={this.handleFilterBlur}
          placeholder='Filter by branch'
          className='branch-filter-input'
          name="branchFilter"
          value={this.props.filters.branch}
          options={uniqueBranches(this.props.branches.toJS())}
          onChange={this.handleBranchFilterChange}
        />

      </div>
    );
  }
}

BranchFilter.propTypes = {
  filters: PropTypes.object.isRequired,
  branches: PropTypes.object,
  updateFilters: PropTypes.func.isRequired
};

export default BranchFilter;
