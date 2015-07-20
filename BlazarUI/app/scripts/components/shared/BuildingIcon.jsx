import React from 'react';

class BuildingIcon extends React.Component {

  getClassNames() {
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
  result: React.PropTypes.string,
  classNames: React.PropTypes.string,
  size: React.PropTypes.oneOf(['medium', 'small'])
};

export default BuildingIcon;
