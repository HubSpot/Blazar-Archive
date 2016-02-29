import React, {Component} from 'react';
import PageContainer from '../components/shared/PageContainer.jsx';
import { IndexLink } from 'react-router';

class NotFound extends Component {

  render() {
    return (
      <PageContainer classNames='not-found-container'>
        <h1 className="notfound__heading">404. Page Not Found. <br/> Sorry about that.</h1>
        <p>
          <IndexLink to='/'>Head back to the dashboard</IndexLink>
        </p>
      </PageContainer>
    );
  }

}

export default NotFound;
