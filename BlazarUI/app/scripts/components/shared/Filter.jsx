import React, {PropTypes} from 'react';
import Select from 'react-select';

const Filter = ({loading, hide, placeholder, className, value, options, handleFilterChange}) => {
  if (loading || hide) {
    return null;
  }

  return (
    <div className="filter-container">
      <Select
        placeholder={placeholder || 'Filter'}
        className={className}
        name="filter"
        value={value}
        options={options}
        onChange={handleFilterChange}
      />
    </div>
  );
};

Filter.propTypes = {
  value: PropTypes.string.isRequired,
  options: PropTypes.array.isRequired,
  loading: PropTypes.bool,
  hide: PropTypes.bool,
  className: PropTypes.string,
  handleFilterChange: PropTypes.func,
  placeholder: PropTypes.string
};

export default Filter;
