import React, {Component, PropTypes} from 'react';

class BuildingIcon extends Component {

  getClassNames() {
    if (this.props.result === "IN_PROGRESS" && this.props.prevBuildState != '') {
      return `building-icon sidebar__active-building-icon building-icon--${this.props.result}-laststatus-${this.props.prevBuildState} ${this.props.classNames} building-icon--${this.props.size}`;
    }

    return `building-icon sidebar__active-building-icon building-icon--${this.props.result} ${this.props.classNames} building-icon--${this.props.size}`;
  }

  getInnerClassNames() {
    return `building-icon-inner building-icon-inner--${this.props.size}`;
  }

  render() {
    return (
      <div className={this.getClassNames()}><div className={this.getInnerClassNames()}></div></div>
    );
  }

}

BuildingIcon.defaultProps = {
  classNames: '',
  size: 'medium'
};

BuildingIcon.propTypes = {
  result: PropTypes.string,
  classNames: PropTypes.string,
  size: PropTypes.oneOf(['medium', 'small'])
};

export default BuildingIcon;
