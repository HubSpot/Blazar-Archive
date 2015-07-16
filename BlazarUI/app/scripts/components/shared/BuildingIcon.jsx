import React from 'react';

class BuildingIcon extends React.Component {

  getClassNames() {
    return 'la-ball-scale la-sm sidebar__active-building-icon ' + this.props.status;
  }

  render() {
    return (
      <div className={this.getClassNames()}><div></div></div>
    );
  }

}

BuildingIcon.propTypes = {
  status: React.PropTypes.string
};

export default BuildingIcon;
