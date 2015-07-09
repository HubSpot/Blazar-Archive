import React from 'react';

class Sidebar extends React.Component {

  constructor(props){
    super(props);
  }


  render() {
    return (
      <div className='sidebar'>
        <h2 className='sidebar__headline'>{this.props.headline}</h2>
        {this.props.children}
      </div>
    );
  }
}

export default Sidebar;

// add required props..