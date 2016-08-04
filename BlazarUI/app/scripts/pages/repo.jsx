import React, {PropTypes} from 'react';
import RepoContainer from '../components/repo/RepoContainer.jsx';
import HeaderContainer from '../components/header/HeaderContainer.jsx';

const Repo = ({params}) => (
  <div>
    <HeaderContainer
      params={params}
    />
    <RepoContainer
      params={params}
    />
  </div>
);

Repo.propTypes = {
  params: PropTypes.object.isRequired
};

export default Repo;
