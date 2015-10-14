import React, {Component, PropTypes} from 'react';
import {dataTagValue} from '../Helpers';

class PaginationLink extends Component {

  constructor() {      
    this.handleClick = this.handleClick.bind(this);
  }

  handleClick(e) {
    const page = dataTagValue(e, 'page');
    this.props.changePage(parseInt(page));
    e.preventDefault();
  }
  
  getRenderedClassNames() {
    if (this.props.activePage === this.props.page) {
      return 'pagination-active-link';
    }
    else {
      return null;  
    }
  }

  render() {
    return (
      <li>
        <a onClick={this.handleClick} href='#' data-page={this.props.page} className={this.getRenderedClassNames()}>
          {this.props.label || this.props.page + 1}
        </a>
      </li>
    );
  }
}

PaginationLink.propTypes = {
  changePage: PropTypes.func.isRequired,
  label: PropTypes.string,
  activePage: PropTypes.number
}

export default PaginationLink;
