import React, {Component, PropTypes} from 'react';
import Immutable from 'immutable'
const Link = require('react-router').Link;
import Alert from 'react-bootstrap/lib/Alert';
import Loader from '../shared/Loader.jsx';
import TableHead from '../shared/TableHead.jsx';
import StarredModulesTableRow from './StarredModulesTableRow.jsx';

class StarredModules extends Component {

  getRows() {
    return this.props.starredBuilds.map((item, i) => {
      return (
        <StarredModulesTableRow
          key={i}
          item={item}
        />
      );
    });
  }

  render() {
    
    if (!this.props.starredBuilds || this.props.starredBuilds.size === 0) {
      return(
        <Alert bsStyle='info'>
          <p>You have no starred modules.</p>
          <p>ProTip: Starring modules allows you to quickly navigate to them from the sidebar as well as view recent build history in the dashboard. 
            Star modules by clicking on the module name in the sidebar, then clicking the star next to the modules name.
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
      'Commit',
      'Commit Message'
    ];

    return (
      <table className="table table-hover table-striped table-border">
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

StarredModules.propTypes = {
  starredBuilds: PropTypes.instanceOf(Immutable.List)
};

export default StarredModules;
