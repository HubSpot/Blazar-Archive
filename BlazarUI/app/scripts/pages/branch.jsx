import React from 'react';
import BranchContainer from '../components/branch/BranchContainer.jsx';

class Branch extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div>
        <BranchContainer
          params={this.props.params}
        />
      </div>
    );
  }
}

Branch.propTypes = {
  params: React.PropTypes.object.isRequired
};

export default Branch;
