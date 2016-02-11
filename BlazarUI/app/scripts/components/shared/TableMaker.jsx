import React, {Component, PropTypes} from 'react';
import Classnames from 'classnames';
import BuildStates from '../../constants/BuildStates.js';
import TableHead from './TableHead.jsx';
import Pagination from './Pagination.jsx';
import Progress from '../../utils/progress';
import {bindAll, has} from 'underscore';
import Loader from '../shared/Loader.jsx';

function TableMaker(RenderedComponent, options) {

  let initialState = {
    page: 0,
    rowsPerPage: 25,
    paginate: options.paginate
  };

  class Table extends Component {
    
    constructor() {      
      bindAll(this, 'buildTable', 'getRows', 'changePage');
      this.state = initialState;
    }

    componentWillReceiveProps(nextProps) {
      if (nextProps.params !== this.props.params) {
        this.setState(initialState);
      }
    }

    changePage(page) {
      this.setState({
        page: page
      })
    }


    getRows(data, TableRow, params) {
      const pageStart = this.state.page * this.state.rowsPerPage;
      const pageEnd = pageStart + this.state.rowsPerPage;      
      const currentRows = options.paginate ? data.slice(pageStart, pageEnd) : data;

      return currentRows.map((item, i) => {
        return (
          <TableRow
            data={item}
            params={params}
            key={i}
          />
        );

      });
    }

    buildTable(tableOptions) {
      const {columnNames, data, rowComponent, params} = tableOptions;

      return (
        <div className='table-wrapper'>
          <table className="fixed-table table table-hover table-striped">
            <TableHead
              columnNames={columnNames}
            />
            <tbody>
              {this.getRows(data, rowComponent, params)}
            </tbody>
          </table>
          {options.paginate ? Pagination(data, this.state, this.changePage) : null}
        </div>
      );
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
