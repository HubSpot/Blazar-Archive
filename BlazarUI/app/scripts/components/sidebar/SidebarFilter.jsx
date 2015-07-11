import React from 'react';

class SidebarFilter extends React.Component {

  constructor(props){
    super(props);
  }

  render() {

    if(this.props.loading){
      return <div></div>
    }

    return (
      <div className="form-group">
        <input type="text" className="form-control" data-action="project-filter" placeholder="Filter projects..." />
      </div>
    );
  }
}

export default SidebarFilter;