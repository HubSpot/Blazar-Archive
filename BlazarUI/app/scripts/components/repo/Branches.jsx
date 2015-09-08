import React, {Component, PropTypes} from 'react';

import ModulesTable from '../branch/ModulesTable.jsx';
import Collapsable from '../shared/Collapsable.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';

class Branches extends Component {

  getBranches() {
    const branches = this.props.branches;
    const branchList = [];

    branches.forEach((branch, i) => {

      let table = (
        <ModulesTable modules={branch.modules} />
      )

      if (!branch.hasBuiltOnBlazar){
        table = (
          <EmptyMessage>
            No build history.
          </EmptyMessage>
        )
      }

      branchList.push(
        <Collapsable
          key={i}
          header={branch.branch}
          iconType='octicon'
          iconName='git-branch'
          initialToggleStateOpen={branch.branch === 'master'}
        >
          {table}
        </Collapsable>
      )
    });

    return branchList;
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
