import React, {Component, PropTypes} from 'react';
import BranchContainer from '../components/branch/BranchContainer.jsx';
import PageHeaderContainer from '../components/PageHeader/PageHeaderContainer.jsx';

class Branch extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div>
        <PageHeaderContainer 
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
