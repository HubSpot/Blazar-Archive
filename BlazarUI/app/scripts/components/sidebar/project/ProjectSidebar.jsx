import React from 'react';

class ProjectSidebar extends React.Component {

  constructor(props){
    super(props);
  }

  render() {

    return (
      <div>
        Stuff for {this.props.projectId} goes here
      </div>
    );
  }
}


export default ProjectSidebar;