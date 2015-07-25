import React, {Component} from 'react';
import PageContainer from '../components/shared/PageContainer.jsx';

class NotFound extends Component {

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
