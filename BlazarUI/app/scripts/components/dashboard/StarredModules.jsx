import React, {Component, PropTypes} from 'react';
const Link = require('react-router').Link;
import Alert from 'react-bootstrap/lib/Alert';
import Loader from '../shared/Loader.jsx';
import TableHead from '../shared/TableHead.jsx';
import StarredModulesTableRow from './StarredModulesTableRow.jsx';

class StarredModules extends Component {

  getRows() {
    return this.props.starredBuilds.map((item, i) => {
      //console.log('item: ', item);
      //console.log('i: ', i);
      return (
        <StarredModulesTableRow 
          key={i}
          item={item}
        />
      );
    });
  }

  render() {  
    //console.log(this.props.starredBuilds.toString());

    /*if (this.props.loadingModulesBuildHistory || this.props.loadingStars) {
      return (
        <Loader align='top-center' />
      );
    }*/
    
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
      {label: 'Build State', key: ''},
      {label: 'Repo', key: ''},
      {label: 'Branch', key: ''},
      {label: 'Build Number', key: ''},
      {label: 'Start Time', key: ''},
      {label: 'Commit', key: ''},
      {label: 'Commit Message', key: ''}
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
  starredBuilds: PropTypes.array
  /*loadingModulesBuildHistory: PropTypes.bool,
  loadingStars: PropTypes.bool,
  modulesBuildHistory: PropTypes.array,*/
};

export default StarredModules;
