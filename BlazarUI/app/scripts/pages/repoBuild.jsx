import React, {PropTypes} from 'react';
import RepoBuildContainer from '../components/repo-build/RepoBuildContainer.jsx';

const RepoBuild = ({params}) => (
  <div>
    <RepoBuildContainer params={params} />
  </div>
);

RepoBuild.propTypes = {
  params: PropTypes.object.isRequired
};

export default RepoBuild;
