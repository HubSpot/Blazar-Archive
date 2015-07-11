import React from 'react';
import Project from './Project.jsx';
import PageContainer from '../layout/PageContainer.jsx'

class ProjectContainer extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <PageContainer>
        <Project
          project={this.props.project}
        />
      </PageContainer>
    );
  }
}

export default ProjectContainer;