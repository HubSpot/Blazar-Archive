import React, {Component, PropTypes} from 'react';
import ButtonGroup from 'react-bootstrap/lib/ButtonGroup';
import Button from 'react-bootstrap/lib/Button';
import Icon from '../shared/Icon.jsx';

class StarredToggle extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <ButtonGroup className="sidebar__filter-buttons" bsSize="large">
        <Button id="starred" className="sidebar__filter-buttons-btn" active={this.props.showStarred} onClick={this.props.onClick}><Icon name="star"></Icon></Button>
        <Button id="all" className="sidebar__filter-buttons-btn" active={!this.props.showStarred} onClick={this.props.onClick}>All</Button>
      </ButtonGroup>
    );
  }
}

StarredToggle.propTypes = {
  onClick: PropTypes.func.isRequired,
  showStarred: PropTypes.bool.isRequired
};

export default StarredToggle;
