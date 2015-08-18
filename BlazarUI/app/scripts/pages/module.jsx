import React, {Component, PropTypes} from 'react';
import ModuleContainer from '../components/module/ModuleContainer.jsx';

class Module extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div>
        <ModuleContainer
          params={this.props.params}
        />
      </div>
    );
  }
}

Module.propTypes = {
  params: PropTypes.object.isRequired
};

export default Module;
