import React, {PropTypes} from 'react';
import OrgContainer from '../components/org/orgContainer.jsx';

const Org = ({params}) => (
  <div>
    <OrgContainer params={params} />
  </div>
);

Org.propTypes = {
  params: PropTypes.object.isRequired
};

export default Org;
