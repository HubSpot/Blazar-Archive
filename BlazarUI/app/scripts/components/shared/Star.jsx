import React, {Component, PropTypes} from 'react';
import Icon from '../shared/Icon.jsx';
import { bindAll } from 'underscore';
import classnames from 'classnames';

class Star extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'handleClick');

    this.state = {
      isStarredWithState: null
    }
  }

  componentWillReceiveProps(nextProps) {
    if (!this.props.updateWithState) {
      return;
    }

    this.setState({
      isStarredWithState: nextProps.isStarred
    });

  }

  handleClick(event) {
    event.stopPropagation();

    if (this.props.disabled) {
      return;
    }

    const starInfo = {
      moduleId: this.props.moduleId,
      moduleName: this.props.moduleName,
      modulePath: this.props.modulePath
    };

    let starredState = !this.props.isStarred

    // we want to show change w/out waiting for new props
    if (this.props.updateWithState) {

      if (this.state.isStarredWithState === null) { 
        starredState = !this.props.isStarred;
      } else {
        starredState = !this.state.isStarredWithState;
      }

      this.setState({
        isStarredWithState: starredState
      });

    }

    this.props.toggleStar(!starredState, starInfo);
  }

  getStarredState() {
    return this.state.isStarredWithState !== null ? this.state.isStarredWithState : this.props.isStarred;
  }

  getContainerClassNames() {
    const starredState = this.getStarredState();

    return classnames([
       'star',
       this.props.className,
       {disabled: this.props.disabled},
       {selected: starredState},
       {unselected: !starredState}
    ]);
  }

  getIconClassNames() {
    const starredState = this.getStarredState();

    return classnames([
       {'star': starredState},
       {'star-o': !starredState}
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
  className: PropTypes.string,
  isStarred: PropTypes.bool.isRequired,
  toggleStar: PropTypes.func.isRequired,
  moduleId: PropTypes.number.isRequired,
  modulePath: PropTypes.string.isRequired,
  moduleName: PropTypes.string.isRequired,
  disabled: PropTypes.bool,
  updateWithState: PropTypes.bool
};

export default Star;
