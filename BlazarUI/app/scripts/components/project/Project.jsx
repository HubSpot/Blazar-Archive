import React from 'react';

class Project extends React.Component{

  constructor(props, context) {
   super(props);
  }

  render() {
    return (
      <div className='primary-content'>
        <h2>{this.props.projectId}</h2>
      </div>
    );
  }

}


export default Project;