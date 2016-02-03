import React, {Component, PropTypes} from 'react';
import EmptyMessage from '../shared/EmptyMessage.jsx';
import Loader from '../shared/Loader.jsx';
import {Link} from 'react-router';

class RepoListing extends Component {

  render() {
    if (this.props.loading) {
      return (
        <Loader align='top-center' />
      );
    }
    
    const {filteredRepos} = this.props;
    
    if (filteredRepos.size === 0) {
      return (
        <EmptyMessage> No repositories for this organization</EmptyMessage>
      );
    }
    
    const repositories = filteredRepos.map((repo, i) => {
      return (
        <Link to={repo.get('blazarRepositoryPath')} className="repo-listing__repo" key={i}>
          {repo.get('repository')}
        </Link>
      );
    });

    return (
      <div className="repo-listing">
        {repositories}
      </div>
    );
  }
}

RepoListing.propTypes = {
  loading: PropTypes.bool,
  filteredRepos: PropTypes.object.isRequired
};

export default RepoListing;
