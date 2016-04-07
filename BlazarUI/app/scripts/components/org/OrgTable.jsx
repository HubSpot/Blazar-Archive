import React, {Component, PropTypes} from 'react';
import TableMaker from '../shared/TableMaker.jsx';
import OrgTableRow from './OrgTableRow.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';
import Loader from '../shared/Loader.jsx';
import Immutable from 'immutable';

class OrgTable extends Component {

  render() {
    if (this.props.loading) {
      return (
        <Loader align='top-center' />
      );
    }

    if (this.props.repos.size === 0) {
      return (
        <EmptyMessage> No repositories </EmptyMessage>
      );
    }

    return this.props.buildTable({
      data: this.props.repos,
      columnNames: [' ', 'Repository', 'Latest Master Build', 'Start Time', 'Duration'],
      rowComponent: OrgTableRow
    });
  }
}

OrgTable.propTypes = {
  loading: PropTypes.bool,
  repos: PropTypes.instanceOf(Immutable.List).isRequired
};

export default TableMaker(OrgTable, {showProgress: false});
