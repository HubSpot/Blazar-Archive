import React, {Component, PropTypes} from 'react';
import Button from 'react-bootstrap/lib/Button';
import Icon from '../shared/Icon.jsx';
import {bindAll} from 'underscore';

class SidebarToggle extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'handleToggle');
  }

  handleToggle(e) {
    this.props.toggleFilter(e.target.id);
  }

  getButtonClassNames(state) {
    let classNames = 'sidebar__filter-buttons-btn';
    if (this.props.toggleFilterState === state) {
      classNames += ' active'
    }
    return classNames;
  }

  render() {
    return (
      <div className="sidebar__filter-buttons">
        <a
          id="starred"
          className={this.getButtonClassNames('starred')}
          onClick={this.handleToggle}>
            <Icon name="star" />
        </a>

        <a
          id="all"
          className={this.getButtonClassNames('all')}
          onClick={this.handleToggle}>
          All
        </a>

      </div>
    );
  }
}

SidebarToggle.propTypes = {
  toggleFilter: PropTypes.func.isRequired,
  toggleFilterState: PropTypes.string.isRequired
};

export default SidebarToggle;
