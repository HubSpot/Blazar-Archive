import React, {Component} from 'react';
import PageContainer from '../components/shared/PageContainer.jsx';

class NotFound extends Component {

  render() {
    return (
      <PageContainer>
        <h1 className="notfound__heading">404. Page Not Found. <br/ > Sorry about that.</h1>
      </PageContainer>
    );
  }

}

export default NotFound;
