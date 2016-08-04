import React, {PropTypes} from 'react';
import BuildContainer from '../components/build/BuildContainer.jsx';
import HeaderContainer from '../components/header/HeaderContainer.jsx';

const Project = ({params}) => (
  <div>
    <HeaderContainer
      params={params}
    />
    <BuildContainer
      params={params}
    />
  </div>
);

Project.propTypes = {
  params: PropTypes.object.isRequired
};

export default Project;
