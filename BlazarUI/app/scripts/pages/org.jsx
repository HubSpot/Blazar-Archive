import React, {PropTypes} from 'react';
import OrgContainer from '../components/org/orgContainer.jsx';
import HeaderContainer from '../components/header/HeaderContainer.jsx';

const Org = ({params}) => (
  <div>
    <HeaderContainer params={params} />
    <OrgContainer params={params} />
  </div>
);

Org.propTypes = {
  params: PropTypes.object.isRequired
};

export default Org;
