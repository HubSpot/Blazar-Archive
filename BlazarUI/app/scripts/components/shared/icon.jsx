import React, {Component, PropTypes} from 'react';

class Icon extends Component {

  getClassNames() {

    let classNames =  `${(this.props.prefix ? this.props.prefix + '-' : '')}${this.props.type} ${this.props.type}-${this.props.name} ${this.props.classNames}`;
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
  name: PropTypes.string.isRequired,
  prefix: PropTypes.string,
  classNames: PropTypes.string,
  title: PropTypes.string
};

export default Icon;
