import React, {Component, PropTypes} from 'react';
import RepoContainer from '../components/repo/RepoContainer.jsx';

class Repo extends Component {

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
  params: PropTypes.object.isRequired
};

export default Repo;
