import React, {Component, PropTypes} from 'react';
import Classnames from 'classnames';
import BuildStates from '../../constants/BuildStates.js';
import TableHead from './TableHead.jsx';
import Pagination from './Pagination.jsx';
import Progress from '../../utils/progress';
import {bindAll} from 'underscore';

function TableMaker(RenderedComponent) {
  
  class Table extends Component {
    
    constructor() {      
      bindAll(this, 'buildTable', 'getRows', 'changePage');
      this.state = {
        page: 0,
        rowsPerPage: 10
      }
    }
    
    changePage(page) {
      this.setState({
        page: page
      })
    }

    buildTable(options) {
      return (
        <div className='table-wrapper'>
          <table className="fixed-table table table-hover table-striped">
            <TableHead
              columnNames={options.columnNames}
            />
            <tbody>
              {this.getRows(options.data, options.rowComponent)}
            </tbody>
          </table>
          {Pagination(options.data, this.state, this.changePage)}
        </div>
      );
    }

    getRows(builds, TableRow) {
      const pageStart = this.state.page * this.state.rowsPerPage;
      const pageEnd = pageStart + this.state.rowsPerPage;
      const currentRows = builds.slice(pageStart, pageEnd);

      return currentRows.map((build, i) => {
        if (build.build.state === BuildStates.IN_PROGRESS) {
          const progress = builds.length > 1 ? Progress(build.build.startTimestamp, builds) : false;

          return (
            <TableRow
              build={build}
              key={i}
              progress={progress}
            />
          );
        }

        return (
          <TableRow
            build={build}
            key={i}
          />
        );

      });
    }

    render() {
      return (
        <RenderedComponent 
          {...this.props} 
          {...this.state} 
          getRows={this.getRows}
          buildTable={this.buildTable}
        />
      );
    }

  };
  
  return Table;
};

export default TableMaker;
