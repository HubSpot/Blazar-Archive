import React, {PropTypes} from 'react';
import RepoContainer from '../components/repo/RepoContainer.jsx';

const Repo = ({params}) => (
  <div>
    <RepoContainer params={params} />
  </div>
);

Repo.propTypes = {
  params: PropTypes.object.isRequired
};

export default Repo;
