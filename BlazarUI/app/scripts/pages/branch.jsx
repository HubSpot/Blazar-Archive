import React, {PropTypes} from 'react';
import BranchContainer from '../components/branch/BranchContainer.jsx';
import HeaderContainer from '../components/header/HeaderContainer.jsx';

const Branch = ({params}) => (
  <div>
    <HeaderContainer
      params={params}
    />
    <BranchContainer
      params={params}
    />
  </div>
);

Branch.propTypes = {
  params: PropTypes.object.isRequired
};

export default Branch;
