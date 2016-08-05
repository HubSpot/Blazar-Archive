import React, {PropTypes} from 'react';
import BuildContainer from '../components/build/BuildContainer.jsx';

const Project = ({params}) => (
  <div>
    <BuildContainer params={params} />
  </div>
);

Project.propTypes = {
  params: PropTypes.object.isRequired
};

export default Project;
