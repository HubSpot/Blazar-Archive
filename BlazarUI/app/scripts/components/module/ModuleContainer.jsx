import React from 'react';
import Module from './Module.jsx';
import PageContainer from '../layout/PageContainer.jsx'

class ModuleContainer extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <PageContainer>
        <Module
          params={this.props.params}
        />
      </PageContainer>
    );
  }
}

export default ModuleContainer;