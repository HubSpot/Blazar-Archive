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

  render() {
    return (
      <div className="sidebar__filter-buttons">
        <Button
          id="starred"
          className="sidebar__filter-buttons-btn"
          active={this.props.toggleFilterState === 'starred'}
          onClick={this.handleToggle}>
            <Icon name="star" />
        </Button>

        <Button
          id="all"
          className="sidebar__filter-buttons-btn"
          active={this.props.toggleFilterState === 'all'}
          onClick={this.handleToggle}>
            All
        </Button>
      </div>
    );
  }
}

SidebarToggle.propTypes = {
  toggleFilter: PropTypes.func.isRequired,
  toggleFilterState: PropTypes.string.isRequired
};

export default SidebarToggle;
