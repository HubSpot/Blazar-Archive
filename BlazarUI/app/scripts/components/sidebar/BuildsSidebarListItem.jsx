// To do
// Deprecate...

import React, {Component, PropTypes} from 'react';
import { bindAll } from 'underscore';
import SidebarItem from './SidebarItem.jsx';
import BuildingIcon from '../shared/BuildingIcon.jsx';
import Icon from '../shared/Icon.jsx';
import Star from '../shared/Star.jsx';

import { Link } from 'react-router';

class BuildsSidebarListItem extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'handleModuleExpand', 'toggleStar');

    this.state = {
      expanded: false
    };
    
  }

  toggleStar(starState) {
    this.props.persistStarChange(!starState, this.props.repo.repository, this.props.repo.branch);
  }

  componentWillReceiveProps() {
    this.setState({
      expanded: this.props.isExpanded
    });
  }

  handleModuleExpand() {
    this.props.moduleExpandChange(this.props.repo.id);
  }

  getModulesClassNames() {
    let classNames = 'sidebar__modules';
    if (this.props.isExpanded) {
      classNames += ' expanded';
    }
    return classNames;
  }

  getExpandStatus() {
    return this.props.isExpanded ? 'chevron-down' : 'chevron-right';
  }

  render() {
    let config = window.config;
    let repo = this.props.repo;
    let modules = this.props.repo.modules;
    let moduleList = [];
    let repoLink = `${config.appRoot}/builds/${repo.host}/${repo.organization}/${repo.repository}`;
    let branchLink = `${config.appRoot}/builds/${repo.host}/${repo.organization}/${repo.repository}/${repo.branch}`;

    console.log('dets: ', this.props.repo);

    modules.forEach( (build) => {
      moduleList.push(
        <SidebarItem key={build.modulePath} repo={build} />
      );
    });

    function getRepoBuildState() {
      if (repo.isBuilding) {
        return <BuildingIcon result='IN_PROGRESS' />;
      }
    }

    return (
      <div className='sidebar__repo-container'>
        {moduleList}
      </div>
    );


  }
}

BuildsSidebarListItem.propTypes = {
  repo: PropTypes.object,
  project: PropTypes.object,
  filterText: PropTypes.string,
  isExpanded: PropTypes.bool,
  moduleExpandChange: PropTypes.func,
  persistStarChange: PropTypes.func,
  isStarred: PropTypes.bool.isRequired
};

export default BuildsSidebarListItem;
