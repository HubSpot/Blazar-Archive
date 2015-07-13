import React from 'react';
import Branch from './Branch.jsx';
import PageContainer from '../layout/PageContainer.jsx'

class BranchContainer extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <PageContainer>
        <Branch
          params={this.props.params}
        />
      </PageContainer>
    );
  }
}

export default BranchContainer;