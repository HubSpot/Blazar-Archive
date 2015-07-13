import React from 'react';
import BuildContainer from '../components/build/BuildContainer.jsx'

class Project extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <div>

        <BuildContainer
          params={this.props.params}
        />

      </div>
    );
  }
}

export default Project;