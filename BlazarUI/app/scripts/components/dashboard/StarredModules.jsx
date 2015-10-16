import React, {Component, PropTypes} from 'react';
const Link = require('react-router').Link;
import Alert from 'react-bootstrap/lib/Alert';
import Loader from '../shared/Loader.jsx';
import TableHead from '../shared/TableHead.jsx';
import StarredModulesTableRow from './StarredModulesTableRow.jsx';

class StarredModules extends Component {

  getRows() {
    return this.props.modulesBuildHistory.map((item, i) => {
      return (
        <StarredModulesTableRow 
          key={i}
          item={item}
        />
      );
    });
  }

  render() {  
    if (this.props.loadingModulesBuildHistory || this.props.loadingStars) {
      return (
        <Loader align='top-center' />
      );
    }
    
    if (this.props.modulesBuildHistory.length === 0) {
      return(
        <Alert bsStyle='info'>
          <p>You have no starred modules.</p>
          <p>ProTip: Starring modules allows you to quickly navigate to them from the sidebar as well as view recent build history in the dashboard. 
            Star modules by clicking on the module name in the sidebar, then clicking the star next to the modules name.
          </p>
        </Alert>
      )
    }

    const columnNames = [
      {label: '', key: 'buildNumber'},
      {label: 'Module', key: 'module'},
      {label: 'Latest Build', key: 'latestBuild'},
      {label: 'Start Time', key: 'startTime'},
      {label: 'Commit', key: 'commit'},
      {label: 'Commit Message', key: 'commitMessage'},
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
  loadingModulesBuildHistory: PropTypes.bool,
  loadingStars: PropTypes.bool,
  modulesBuildHistory: PropTypes.array,
};

export default StarredModules;
