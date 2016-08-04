import React, {PropTypes} from 'react';
import DashboardContainer from '../components/dashboard/DashboardContainer.jsx';
import HeaderContainer from '../components/header/HeaderContainer.jsx';

const Home = ({params}) => (
  <div>
    <HeaderContainer
      params={params}
    />
    <DashboardContainer
      params={params}
    />
  </div>
);

Home.propTypes = {
  params: PropTypes.object.isRequired
};

export default Home;
