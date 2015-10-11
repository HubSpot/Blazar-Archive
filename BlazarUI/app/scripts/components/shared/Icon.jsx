import React, {Component, PropTypes} from 'react';
import { ICON_LIST } from '../constants';
import ClassNames from 'classnames'

class Icon extends Component {

  getClassNames() {

    if (this.props.for) {
      return ClassNames([
        'icon',
        this.props.classNames,
        ICON_LIST[this.props.for]
      ]);
    } 
    else {
      return ClassNames([
        'icon',
        this.props.classNames,
        this.props.type,
        `${this.props.type}-${this.props.name}`        
      ]);
    }

    return classNames;
  }

  render() {
    return (
      <i title={this.props.title} className={this.getClassNames()}></i>
    );
  }

}

Icon.defaultProps = {
  type: 'fa',
  classNames: ''
};

Icon.propTypes = {
  type: PropTypes.oneOf(['fa', 'octicon']),
  name: PropTypes.string,
  prefix: PropTypes.string,
  classNames: PropTypes.string,
  title: PropTypes.string,
  for: PropTypes.string
};

export default Icon;
