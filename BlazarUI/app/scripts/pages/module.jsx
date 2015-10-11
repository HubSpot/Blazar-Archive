import React, {Component, PropTypes} from 'react';
import {clone} from 'underscore';
import ModuleContainer from '../components/module/ModuleContainer.jsx';
import HeaderContainer from '../components/header/HeaderContainer.jsx';

class Module extends Component {

  render() {
    return (
      <div>
        <HeaderContainer 
          params={this.props.params}
        />
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
