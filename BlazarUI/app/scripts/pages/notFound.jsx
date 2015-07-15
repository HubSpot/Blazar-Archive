import React from 'react';
import PageContainer from '../components/layout/PageContainer.jsx';

class NotFound extends React.Component {

  render() {
    return (
      <PageContainer>
        <h1>404!</h1>
        Route not found :/
      </PageContainer>
    );
  }

}

export default NotFound;
