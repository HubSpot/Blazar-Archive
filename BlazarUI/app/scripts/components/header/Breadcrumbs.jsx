import React, {Component, PropTypes} from 'react';
import Logo from '../shared/Logo.jsx';
import Breadcrumb from './Breadcrumb.jsx';
import BreadcrumbHost from './BreadcrumbHost.jsx';

class Breadcrumbs extends Component {

  render() {
    const {appRoot} = this.props;  
    const {host, org, repo, branch, buildNumber, moduleName} = this.props.params;
    let links = [];
    
    if (host) {
      links.push(<Breadcrumb param='host' key='host' text={host} dontLink={true} {...this.props} />);
    }
    if (org) {
      links.push(<BreadcrumbHost key='org' {...this.props} {...this.state} isActive={!repo} />);
    }
    if (repo) {
      links.push(<Breadcrumb param='repo' key='repo' text={repo} isActive={!branch} {...this.props} />);
    }
    if (branch) {
      links.push(<Breadcrumb param='branch' key='branch' text={branch} isActive={!buildNumber} {...this.props} />);
    }
    if (buildNumber) {
      links.push(<Breadcrumb param='buildNumber' key='buildNumber' text={buildNumber} isActive={!moduleName} {...this.props} />);
    }
    if (moduleName) {
      links.push(<Breadcrumb param='moduleName' key='moduleName' text={moduleName} isActive={true} {...this.props} />);
    }

    return (
      <div className='breadcrumbs'>
        <Logo crumb={false} />
        {links}
      </div>
    );
  }
}


Breadcrumbs.propTypes = {
  params: PropTypes.object.isRequired,
  appRoot: PropTypes.string.isRequired,
  navigationIsActive: PropTypes.bool
};

export default Breadcrumbs;
