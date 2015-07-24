import React, {Component, PropTypes} from 'react';

class Icon extends Component {

  getClassNames() {

    let classNames =  `${this.props.type} ${this.props.type}-${this.props.name} ${this.props.classNames}`;
    return classNames;
  }

  render() {
    return (
      <i className={this.getClassNames()}></i>
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
  classNames: PropTypes.string
};

export default Icon;
