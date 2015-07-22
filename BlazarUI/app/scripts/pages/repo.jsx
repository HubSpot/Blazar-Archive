import React from 'react';
import RepoContainer from '../components/repo/RepoContainer.jsx';

class Repo extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div>
        <RepoContainer
          params={this.props.params}
        />
      </div>
    );
  }
}

Repo.propTypes = {
  params: React.PropTypes.object.isRequired
};

export default Repo;
