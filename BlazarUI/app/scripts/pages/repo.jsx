import React, {Component, PropTypes} from 'react';
import RepoContainer from '../components/repo/RepoContainer.jsx';
import HeaderContainer from '../components/header/HeaderContainer.jsx';

class Repo extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    console.log("at the page");
    return (
      <div>
        <HeaderContainer 
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
