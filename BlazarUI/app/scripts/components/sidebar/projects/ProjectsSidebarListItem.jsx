import React from 'react';
import _ from 'jQuery';
import ComponentHelpers from '../../ComponentHelpers';
import config from '../../../config';

class Module extends React.Component {
  render(){
    let name = this.props.name;
    let moduleLink = `${config.appRoot}/${this.props.link}`;

    return <a href={moduleLink} className='sidebar__repo-module'>{name}</a>
  }
}

class ProjectSidebarListItem extends React.Component {

  constructor(props){
    super(props);
    this.state = { expanded: false}
    ComponentHelpers.bindAll(this, ['handleModuleExpand'])
  }

  handleModuleExpand() {
    this.setState( { expanded: !this.state.expanded } )
  }

  getModulesClassNames() {
    let classNames = 'sidebar__modules';
    if (this.state.expanded) { classNames += ' expanded'}
    return classNames;
  }

  render() {
    let modules = [];
    let repo = this.props.repo;
    let moduleDetail = repo[0];
    let repoLink = `${moduleDetail.host}/${moduleDetail.organization}/${moduleDetail.repository}`;

    _.each(repo, (i) => {
      let moduleLink = `${repo[i].host}/${repo[i].organization}/${repo[i].repository}/${repo[i].branch}/${repo[i].module}/${repo[i].buildNumber}`
      modules.push(
        <Module key={i} name={repo[i].module} link={moduleLink} />
      )
    })

    return (
      <div className='sidebar__repo-container'>
        <div className='sidebar__repo-url'>{repoLink}</div>
        <div className='sidebar__repo' onClick={this.handleModuleExpand}>
          <div className="la-ball-scale la-sm sidebar__active-building-icon"><div></div></div>
          {repo.repository}
        </div>
        <div className={this.getModulesClassNames()}>{modules}</div>
      </div>
    );
  }
}

ProjectSidebarListItem.propTypes = {
  project: React.PropTypes.object
}

export default ProjectSidebarListItem;