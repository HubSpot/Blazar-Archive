import React, {Component, PropTypes} from 'react';
import TableMaker from '../shared/TableMaker.jsx';
import OrgTableRow from './OrgTableRow.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';

class OrgTable extends Component {

  render() {
    if (this.props.hide) {
      return null;
    }

    if (this.props.repos.size === 0) {
      return (
        <EmptyMessage> No repositories </EmptyMessage>
      );
    }

    console.log(this.props.repos.toJS());

    return this.props.buildTable({
      data: this.props.repos,
      columnNames: [' ', 'Repository', '# of Active Branches'],
      rowComponent: OrgTableRow
    });
  }
}

OrgTable.propTypes = {
  loading: PropTypes.bool,
  repos: PropTypes.array.isRequired
};

export default TableMaker(OrgTable, {showProgress: false});
