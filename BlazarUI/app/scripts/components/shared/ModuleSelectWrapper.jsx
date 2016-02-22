import React, {Component, PropTypes} from 'react';
import Immutable from 'immutable';
import Select from 'react-select';

class ModuleSelectWrapper extends Component {

  shouldComponentUpdate(nextProps, nextState) {
    return nextProps.modules.size != this.props.modules.size;
  }

  createModuleSelectOptions() {
    const modules = this.props.modules;

    return modules.map((m) => {
      return {
        value: m.get('id'),
        label: m.get('name')
      };
    }).toJS();
  }

	render() {
    return (
      <Select
        placeholder='No modules selected.'
        className='module-select-input'
        name='moduleSelect'
        clearAllText='None'
        multi={true}
        options={this.createModuleSelectOptions()}
        onChange={this.props.onSelectUpdate}
      />
    );
  }
}

ModuleSelectWrapper.propTypes = {
  modules: PropTypes.instanceOf(Immutable.List),
  onSelectUpdate: PropTypes.func.isRequired
};

export default ModuleSelectWrapper;