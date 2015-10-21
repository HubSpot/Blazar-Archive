import React, {Component, PropTypes} from 'react';
import Classnames from 'classnames';
import BuildStates from '../../constants/BuildStates.js';
import TableHead from './TableHead.jsx';
import Pagination from './Pagination.jsx';
import Progress from '../../utils/progress';
import {bindAll, has} from 'underscore';
import Loader from '../shared/Loader.jsx';

function TableMaker(RenderedComponent, options) {
  
  class Table extends Component {
    
    constructor() {      
      bindAll(this, 'buildTable', 'getRows', 'changePage');
      this.state = {
        page: 0,
        rowsPerPage: 15
      }
    }

    changePage(page) {
      this.setState({
        page: page
      })
    }

    buildTable(tableOptions) {
      return (
        <div className='table-wrapper'>
          <table className="fixed-table table table-hover table-striped">
            <TableHead
              columnNames={tableOptions.columnNames}
            />
            <tbody>
              {this.getRows(tableOptions.data, tableOptions.rowComponent)}
            </tbody>
          </table>
          {options.paginate ? Pagination(tableOptions.data, this.state, this.changePage) : null}
        </div>
      );
    }

    getRows(data, TableRow) {
      const pageStart = this.state.page * this.state.rowsPerPage;
      const pageEnd = pageStart + this.state.rowsPerPage;      
      const currentRows = options.paginate ? data.slice(pageStart, pageEnd) : data;

      return currentRows.map((item, i) => {
        if (has(item, 'build')) {
          if (item.build.state === BuildStates.IN_PROGRESS && options.showProgress) {
            const progress = data.length > 1 ? Progress(item.build.startTimestamp, data) : false;
            return (
              <TableRow
                data={item}
                key={i}
                progress={progress}
              />
            );
          }
        }

        return (
          <TableRow
            data={item}
            key={i}
          />
        );

      });
    }

    render() {
      if (this.props.loading) {
        return(
          <Loader align='left' />
        )
      }

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
