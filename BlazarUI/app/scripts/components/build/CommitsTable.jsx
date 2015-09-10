import React, {Component, PropTypes} from 'react';
import TableHead from '../shared/TableHead.jsx';
import CommitsTableRow from './CommitsTableRow.jsx';
import MutedMessage from '../shared/MutedMessage.jsx';

class CommitsTable extends Component {

  getRows() {

    return this.props.commits.map((commit, i) =>
      <CommitsTableRow
        commit={commit}
        key={i}
      />
    );
  }

  render() {
    if (this.props.commits.length === 0) {
      return (
        <MutedMessage>
          No new commits since previous build
        </MutedMessage>
      )
    }

    const columnNames = [
      {label: '', key: 'actions'},
      {label: '', key: 'timestamp'},
      {label: '', key: 'message'},
      {label: '', key: 'sha'},
      {label: '', key: 'author'}
    ];

    return (
      <table className="table table-hover table-striped">
        <TableHead
          columnNames={columnNames}
          classNames='hide'
        />
        <tbody>
          {this.getRows()}
        </tbody>
      </table>

    );

  }

}


CommitsTable.propTypes = {
  commits: PropTypes.array.isRequired
};

export default CommitsTable;
