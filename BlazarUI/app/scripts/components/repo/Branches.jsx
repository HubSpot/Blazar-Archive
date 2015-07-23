import React, {Component, PropTypes} from 'react';
import Branch from './Branch.jsx';

class Branches extends Component {

  getBranches() {
    let branches = this.props.branches;
    return branches.map((branch, i) =>
      <Branch
        branch={branches[i]}
        key={i}
      />
    );
  }

  render() {

    if (this.props.loading) {
      return <div></div>;
    }

    return (
      <div>
        {this.getBranches()}
      </div>
    );
  }

}

Branches.propTypes = {
  branches: PropTypes.array,
  loading: PropTypes.bool
};

export default Branches;
