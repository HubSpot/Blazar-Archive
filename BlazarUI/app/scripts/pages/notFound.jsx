import React from 'react';
import PageContainer from '../components/shared/PageContainer.jsx';
import { Link } from 'react-router';

const NotFound = () => (
  <PageContainer classNames="not-found-container">
    <h1 className="notfound__heading">404. Page Not Found. <br /> Sorry about that.</h1>
    <p>
      <Link to="/">Head back to the dashboard</Link>
    </p>
  </PageContainer>
);

export default NotFound;
