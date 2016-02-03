import React, {Component, PropTypes} from 'react';
import classNames from 'classnames';
import BuildStates from '../../constants/BuildStates'

class BuildingIcon extends Component {

  getClassNames() {
    let prevBuildStateModifier = ``;

    if (this.props.result === BuildStates.IN_PROGRESS && this.props.prevBuildState) {
      prevBuildStateModifier = `-laststatus-${this.props.prevBuildState}`;
    }

    return classNames([
        'building-icon',
        'sidebar__active-building-icon',
        `building-icon--${this.props.size}`,
        `building-icon--${this.props.result}${prevBuildStateModifier}`,
        this.props.classNames
      ]);
  }

  getInnerClassNames() {
    return classNames([
      'building-icon-inner',
      `building-icon-inner--${this.props.size}`
    ]);
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
