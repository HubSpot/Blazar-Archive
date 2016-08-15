import React, {Component, PropTypes} from 'react';
import Select from 'react-select';

class ModuleSelectWrapper extends Component {

  constructor(props) {
    super(props);

    this.state = {};
  }

  shouldComponentUpdate(nextProps) {
    return nextProps.modules.length !== this.props.modules.length;
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
          value={this.createModuleSelectOptions(this.props.modules)}
          options={this.createModuleSelectOptions(this.props.modules)}
          onChange={this.props.onSelectUpdate}
        />
      </div>
    );
  }
}

ModuleSelectWrapper.propTypes = {
  modules: PropTypes.array.isRequired,
  onSelectUpdate: PropTypes.func.isRequired
};

export default ModuleSelectWrapper;
