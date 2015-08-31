import React, {Component, PropTypes} from 'react';
import Button from 'react-bootstrap/lib/Button';
import Icon from '../shared/Icon.jsx';
import {bindAll} from 'underscore';

class StarredToggle extends Component {

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
          active={this.props.showStarred}
          onClick={this.handleToggle}>
            <Icon name="star" />
        </Button>
        <Button
          id="all"
          className="sidebar__filter-buttons-btn"
          active={!this.props.showStarred}
          onClick={this.handleToggle}>
            All
        </Button>
      </div>
    );
  }
}

StarredToggle.propTypes = {
  toggleFilter: PropTypes.func.isRequired,
  showStarred: PropTypes.bool.isRequired
};

export default StarredToggle;
