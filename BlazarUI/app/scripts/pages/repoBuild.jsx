import React, {PropTypes} from 'react';
import RepoBuildContainer from '../components/repo-build/RepoBuildContainer.jsx';
import HeaderContainer from '../components/header/HeaderContainer.jsx';

const RepoBuild = ({params}) => (
  <div>
    <HeaderContainer
      params={params}
    />
    <RepoBuildContainer
      params={params}
    />
  </div>
);

RepoBuild.propTypes = {
  params: PropTypes.object.isRequired
};

export default RepoBuild;
