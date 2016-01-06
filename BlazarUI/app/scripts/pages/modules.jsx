import React, {Component, PropTypes} from 'react';
import {clone} from 'underscore';
import ModulesContainer from '../components/modules/ModulesContainer.jsx';
import HeaderContainer from '../components/header/HeaderContainer.jsx';

class Module extends Component {

  render() {
    return (
      <div>
        <HeaderContainer 
          params={this.props.params}
        />
        <ModulesContainer
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
