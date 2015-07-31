/*global config*/
import React, {Component, PropTypes} from 'react';
import BuildingIcon from '../shared/BuildingIcon.jsx';
let Link = require('react-router').Link;

class Module extends Component {

  render() {
    let {inProgressBuild, gitInfo, module} = this.props.repo;
    let moduleLink = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${inProgressBuild.buildNumber}`;

    return (
      <Link to={moduleLink} className='sidebar__repo-module'>
        <BuildingIcon result={inProgressBuild.state} size='small' />
        {module.name}
      </Link>
    );
  }
}

Module.propTypes = {
  repo: PropTypes.object,
  name: PropTypes.string,
  link: PropTypes.string
};

export default Module;
