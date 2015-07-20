import React from 'react';

class Icon extends React.Component {

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
  type: React.PropTypes.oneOf(['fa', 'octicon']),
  name: React.PropTypes.string.isRequired,
  classNames: React.PropTypes.string
};

export default Icon;
