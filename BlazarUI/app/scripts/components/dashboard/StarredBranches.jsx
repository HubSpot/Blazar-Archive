import React, {Component, PropTypes} from 'react';
import Immutable from 'immutable'
const Link = require('react-router').Link;
import Alert from 'react-bootstrap/lib/Alert';
import Loader from '../shared/Loader.jsx';
import TableHead from '../shared/TableHead.jsx';
import StarredBranchesTableRow from './StarredBranchesTableRow.jsx';
import {filterInactiveBuildsImmutable, sortBuildsByRepoAndBranchImmutable} from '../Helpers.js';

class StarredBranches extends Component {

  getRows() {
    return sortBuildsByRepoAndBranchImmutable(filterInactiveBuildsImmutable(this.props.starredBuilds)).map((item, i) => {
      return (
        <StarredBranchesTableRow
          key={i}
          item={item}
        />
      );
    });
  }

  render() {

    if (this.props.loadingStars || this.props.loadingBuilds) {
      return (
        <Loader align='top-center' />
      );
    }
    
    if (!this.props.starredBuilds || this.props.starredBuilds.size === 0) {
      return(
        <Alert bsStyle='info'>
          <p>You have no starred branches.</p>
          <p>ProTip: Starring branches allows you to quickly navigate to them from the sidebar, as well as view recent build history in the dashboard. 
            To star a branch, click on the branch name in the sidebar. Then, click the star next to the branch name.
          </p>
        </Alert>
      );
    }

    const columnNames = [
      '', // build state
      'Repo',
      'Branch',
      'Build Number',
      'Start Time',
      'Commit'
    ];

    return (
      <table className="table table-hover table-striped">
        <TableHead
          columnNames={columnNames}
        />
        <tbody>
          {this.getRows()}
        </tbody>
      </table>
    );
  }

}

StarredBranches.propTypes = {
  starredBuilds: PropTypes.instanceOf(Immutable.List),
  loadingStars: PropTypes.bool,
  loadingBuilds: PropTypes.bool
};

export default StarredBranches;
