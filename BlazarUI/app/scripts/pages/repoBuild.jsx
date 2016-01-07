import React, {Component, PropTypes} from 'react';
import {clone} from 'underscore';
import RepoBuildContainer from '../components/repo-build/RepoBuildContainer.jsx';
import HeaderContainer from '../components/header/HeaderContainer.jsx';

class RepoBuild extends Component {

  render() {
    return (
      <div>
        <HeaderContainer 
          params={this.props.params}
        />
        <RepoBuildContainer
          params={this.props.params}
        />
      </div>
    );
  }
}

RepoBuild.propTypes = {
  params: PropTypes.object.isRequired
};

export default RepoBuild;
