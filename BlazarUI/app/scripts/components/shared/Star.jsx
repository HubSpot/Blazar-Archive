import React, {Component, PropTypes} from 'react';
import Icon from '../shared/Icon.jsx';
import { bindAll } from 'underscore';
import classnames from 'classnames';
import StarActions from '../../actions/starActions';
  
class Star extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'handleClick');
  }

  handleClick(event) {
    event.stopPropagation();

    if (this.props.disabled) {
      return;
    }
    StarActions.toggleStar(this.props.id);
  }

  getContainerClassNames() {
    return classnames([
       'star',
       this.props.className,
       {disabled: this.props.disabled},
       {selected: this.props.isStarred},
       {unselected: !this.props.isStarred},
       {loading: this.props.loading}
    ]);
  }

  getIconClassNames() {
    return classnames([
       {'star': this.props.isStarred},
       {'star-o': !this.props.isStarred}
    ]);
  }

  render() {
    return (
      <span onClick={this.handleClick} className={this.getContainerClassNames()}>
          <Icon name={this.getIconClassNames()}></Icon>
      </span>
    );
  }

}

Star.defaultProps = {
  isStarred: false,
  disabled: false
};

Star.propTypes = {
  loading: PropTypes.bool,
  className: PropTypes.string,
  isStarred: PropTypes.bool.isRequired,
  id: PropTypes.number,
  disabled: PropTypes.bool
};

export default Star;
