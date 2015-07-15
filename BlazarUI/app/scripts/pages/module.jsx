import React from 'react';
import ModuleContainer from '../components/module/ModuleContainer.jsx';

class Module extends React.Component {

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
  params: React.PropTypes.object.isRequired
};

export default Module;
