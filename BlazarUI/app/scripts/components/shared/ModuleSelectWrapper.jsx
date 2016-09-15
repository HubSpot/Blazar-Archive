import React, {Component, PropTypes} from 'react';
import Select from 'react-select';

class ModuleSelectWrapper extends Component {

  constructor(props) {
    super(props);
    this.handleChange = this.handleChange.bind(this);
  }

  shouldComponentUpdate(nextProps) {
    return nextProps.modules.length !== this.props.modules.length;
  }

  handleChange(value) {
    // react-select returns an empty string if nothing is selected
    const moduleIds = value ? value.split(',').map(Number) : [];
    this.props.onUpdateSelectedModuleIds(moduleIds);
  }

  createModuleSelectOptions(modules) {
    return modules.map((m) => {
      return {
        value: m.id || m.value,
        label: m.name || m.label
      };
    });
  }

  render() {
    return (
      <div className="module-select">
        <Select
          placeholder="No modules selected."
          className="module-select__input"
          name="moduleSelect"
          clearAllText="None"
          noResultsText="All modules have been selected."
          multi={true}
          value={this.props.selectedModuleIds}
          options={this.createModuleSelectOptions(this.props.modules)}
          onChange={this.handleChange}
        />
      </div>
    );
  }
}

ModuleSelectWrapper.propTypes = {
  modules: PropTypes.array.isRequired,
  selectedModuleIds: PropTypes.arrayOf(PropTypes.number).isRequired,
  onUpdateSelectedModuleIds: PropTypes.func.isRequired
};

export default ModuleSelectWrapper;
