import React, {Component, PropTypes} from 'react';
import classNames from 'classnames';
import BuildStates from '../../constants/BuildStates';
import {getBuildStatusIconClassNames} from '../Helpers.js';

class BuildingIcon extends Component {

  getInnerClassNames() {
    return classNames([
      'building-icon-inner',
      `building-icon-inner--${this.props.size}`
    ]);
  }

  render() {
    return (
      <div className={getBuildStatusIconClassNames(this.props.result, this.props.prevBuildState)}><div className={this.getInnerClassNames()}></div></div>
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
