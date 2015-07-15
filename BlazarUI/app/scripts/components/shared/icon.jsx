import React from 'react';

class Icon extends React.Component {

  getClassNames() {
    let classNames =  `fa fa-${this.props.name} ${this.props.classNames}`;
    return classNames;
  }

  render() {
    return (
      <i className={this.getClassNames()}></i>
    );
  }

}

Icon.defaultProps = {
  classNames: ''
};

Icon.propTypes = {
  name: React.PropTypes.string.isRequired,
  classNames: React.PropTypes.string
};

export default Icon;
