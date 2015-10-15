import React, {Component, PropTypes} from 'react';
import Select from 'react-select';
import {bindAll, uniq, flatten} from 'underscore';
import {uniqueBranches, uniqueModules} from '../Helpers';

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
    if (this.props.loading) {
      return (
        <Loader align='top-center' />
      );
    }

    return (
      <div className='branch-filter'>
        <Select
          onFocus={this.handleFilterFocus}
          onBlur={this.handleFilterBlur}
          placeholder='Filter by branch'
          className='branch-filter-input'
          name="branchFilter"
          value={this.props.filters.branch}
          options={uniqueBranches(this.props.branches)}
          onChange={this.handleBranchFilterChange}
        />
        <Select
          onFocus={this.handleFilterFocus}
          onBlur={this.handleFilterBlur}
          multi={true}
          placeholder='Filter by module'
          className='branch-filter-input'
          name="moduleFilter"
          value={this.props.filters.module}
          options={uniqueModules(this.props.branches)}
          onChange={this.handleModuleFilterChange}
        />
      </div>
    );
  }
}

BranchFilter.propTypes = {
  filters: PropTypes.object.isRequired,
  branches: PropTypes.array,
  updateFilters: PropTypes.func.isRequired
};

export default BranchFilter;
