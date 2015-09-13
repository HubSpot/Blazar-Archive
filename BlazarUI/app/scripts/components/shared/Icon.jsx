import React, {Component, PropTypes} from 'react';
import { ICON_LIST } from '../constants';


class Icon extends Component {

  getClassNames() {

    let classNames = 'icon ';

    if (this.props.for) {
      classNames += `${ICON_LIST[this.props.for]} ${this.props.classNames}`;
    } else {
      classNames += `${(this.props.prefix ? this.props.prefix + '-' : '')}${this.props.type} ${this.props.type}-${this.props.name} ${this.props.classNames}`;
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
