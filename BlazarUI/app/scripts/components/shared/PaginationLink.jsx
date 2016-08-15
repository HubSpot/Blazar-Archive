import React, {Component, PropTypes} from 'react';
import {dataTagValue} from '../Helpers';
import classNames from 'classnames';

class PaginationLink extends Component {

  constructor(props) {
    super(props);

    this.handleClick = this.handleClick.bind(this);
  }

  handleClick(e) {
    const page = dataTagValue(e, 'page');
    const active = dataTagValue(e, 'active');

    if (active === 'false') {
      return;
    }

    this.props.changePage(parseInt(page, 10));
    e.preventDefault();
  }

  getRenderedClassNames() {
    return classNames({
      'pagination-active-link': this.props.activePage === this.props.page,
      'disabled': this.props.disabled
    });
  }

  getListClassNames() {
    return classNames({
      'pagination-active-link': this.props.activePage === this.props.page,
      'disabled': this.props.disabled
    });
  }

  render() {
    return (
      <li className={this.getListClassNames()}>
        <a data-active={!this.props.disabled} onClick={this.handleClick} href="#" data-page={this.props.page} className={this.getRenderedClassNames()}>
          {this.props.label || this.props.page + 1}
        </a>
      </li>
    );
  }
}

PaginationLink.defaultProps = {
  disabled: false
};

PaginationLink.propTypes = {
  disabled: PropTypes.bool,
  changePage: PropTypes.func.isRequired,
  label: PropTypes.string,
  activePage: PropTypes.number,
  page: PropTypes.number
};

export default PaginationLink;
