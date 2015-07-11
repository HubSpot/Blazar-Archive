import React from 'react';

class Sidebar extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    if(this.props.headline){
      var headline = <h2 className='sidebar__headline'>{this.props.headline}</h2>
    }
    return (
      <div className='sidebar'>
        {this.props.headline ? headline : null}
        {this.props.children}
      </div>
    );
  }
}

export default Sidebar;

Sidebar.propTypes = {
  headline: React.PropTypes.string
}
