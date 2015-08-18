import React, {Component} from 'react';
import PageContainer from '../components/shared/PageContainer.jsx';

class NotFound extends Component {

  render() {
    return (
      <PageContainer>
        <h1 className="notfound__heading">There's nothing here‽‽‽</h1>
        That's a 404.
      </PageContainer>
    );
  }

}

export default NotFound;
