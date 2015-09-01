import React, {Component, PropTypes} from 'react';
import Icon from '../shared/Icon.jsx';
import { bindAll } from 'underscore';


class Star extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'handleClick');
  }

  handleClick(event) {
    event.stopPropagation();
    this.props.toggleStar(!this.props.isStarred);
  }

  render() {
    let className, iconName = '';
    if (this.props.isStarred) {
      className = 'sidebar__star selected';
      iconName = 'star';
    } else {
      className = 'sidebar__star unselected';
      iconName = 'star-o';
    }

    return (
      <span onClick={this.handleClick} className={ className }>
          <Icon name={ iconName }></Icon>
      </span>
    );
  }

}

Star.propTypes = {
  isStarred: PropTypes.bool.isRequired,
  toggleStar: PropTypes.func.isRequired
};

export default Star;
