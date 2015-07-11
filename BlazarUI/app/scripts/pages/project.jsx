import React from 'react';
import ProjectContainer from '../components/project/ProjectContainer.jsx'

class Project extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <div>

        <ProjectContainer
          project={this.props.params}
        />

      </div>
    );
  }
}

export default Project;