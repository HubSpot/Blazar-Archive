import React from 'react';
import Build from './Build.jsx';
import PageContainer from '../layout/PageContainer.jsx'

class BuildContainer extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <PageContainer>
        <Build
          params={this.props.params}
        />
      </PageContainer>
    );
  }
}

export default BuildContainer;