import React from 'react';

class ProjectSidebarListItem extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    let link = `/#project/${this.props.project}`
    return (
      <li>
        <a href={link}>{ this.props.project }</a>
      </li>
    );
  }
}

ProjectSidebarListItem.propTypes = {
  project : React.PropTypes.string
}


export default ProjectSidebarListItem;