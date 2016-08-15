import React, {PropTypes} from 'react';
import DashboardContainer from '../components/dashboard/DashboardContainer.jsx';

const Home = ({params}) => (
  <div>
    <DashboardContainer params={params} />
  </div>
);

Home.propTypes = {
  params: PropTypes.object.isRequired
};

export default Home;
