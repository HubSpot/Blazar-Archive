import React, {Component, PropTypes} from 'react';
import BranchContainer from '../components/branch/BranchContainer.jsx';
import HeaderContainer from '../components/header/HeaderContainer.jsx';

class Branch extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div>
        <HeaderContainer 
          params={this.props.params}
        />
        <BranchContainer
          params={this.props.params}
        />
      </div>
    );
  }
}

Branch.propTypes = {
  params: PropTypes.object.isRequired
};

export default Branch;
