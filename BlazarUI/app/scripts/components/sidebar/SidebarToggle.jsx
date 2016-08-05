import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';

import SidebarFilterActions from '../../actions/sidebarFilterActions';

class SidebarToggle extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'handleToggle');
  }

  handleToggle(e) {
    this.props.toggleFilter(e.target.id);
    SidebarFilterActions.clearSearchValue();
  }

  getButtonClassNames(type) {
    let classNames = `sidebar__filter-buttons-btn ${type}`;
    if (this.props.toggleFilterState === type) {
      classNames += ' active';
    }
    return classNames;
  }

  render() {
    return (
      <div className="sidebar__filter-buttons">
        <a
          id="all"
          className={this.getButtonClassNames('all')}
          onClick={this.handleToggle}
        >
          All
        </a>
        <a
          id="starred"
          className={this.getButtonClassNames('starred')}
          onClick={this.handleToggle}
        >
            Starred
        </a>
        <a
          id="building"
          className={this.getButtonClassNames('building')}
          onClick={this.handleToggle}
        >
            Building
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
