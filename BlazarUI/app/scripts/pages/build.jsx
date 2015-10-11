import React, {Component, PropTypes} from 'react';
import BuildContainer from '../components/build/BuildContainer.jsx';
import HeaderContainer from '../components/header/HeaderContainer.jsx';

class Project extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div>
        <HeaderContainer 
          params={this.props.params}
        />
        <BuildContainer
          params={this.props.params}
        />
      </div>
    );
  }
}

Project.propTypes = {
  params: PropTypes.object.isRequired
};

export default Project;
