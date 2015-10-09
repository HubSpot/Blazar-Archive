import React, {Component, PropTypes} from 'react';
import RepoContainer from '../components/repo/RepoContainer.jsx';
import PageHeaderContainer from '../components/PageHeader/PageHeaderContainer.jsx';

class Repo extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div>
        <PageHeaderContainer 
          params={this.props.params}
        />
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
