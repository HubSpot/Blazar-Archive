import React from 'react';
import Repo from './Repo.jsx';
import PageContainer from '../layout/PageContainer.jsx';

class RepoContainer extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      loading: true
    };
  }

  render() {
    return (
      <PageContainer>
        <Repo
          params={this.props.params}
          loading={this.state.loading}
        />
      </PageContainer>
    );
  }
}

RepoContainer.propTypes = {
  params: React.PropTypes.object.isRequired
};

export default RepoContainer;
