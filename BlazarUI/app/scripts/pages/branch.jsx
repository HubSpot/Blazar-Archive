import React, {PropTypes} from 'react';
import BranchContainer from '../components/branch/BranchContainer.jsx';

const Branch = ({params}) => (
  <div>
    <BranchContainer params={params} />
  </div>
);

Branch.propTypes = {
  params: PropTypes.object.isRequired
};

export default Branch;
