import React, {Component, PropTypes} from 'react';
import ReactDOM from 'react-dom';
import {isEmpty, bindAll, map} from 'underscore';
import {Link} from 'react-router';
import Alert from 'react-bootstrap/lib/Alert';
import {contains} from 'underscore';
import classNames from 'classnames';

import BuildStates from '../../constants/BuildStates';
import {getInterProjectClassName} from '../../constants/InterProjectConstants';

class InterProjectAlert extends Component {

  constructor(props) {
    super(props);

    this.state = {
      expanded: false
    };

    bindAll(this, 'onClickAlert');
  }

  getClassNames() {
    return classNames(
      'inter-project-alert', {
        'expanded': this.state.expanded
      }
    );
  }

  getStatusColorClassName() {
    const {state} = this.props.upAndDownstreamModules;

    return getInterProjectClassName(state);
  }

  getStatusClassNames() {
    return classNames(
      'inter-project-alert__status',
      this.getStatusColorClassName()
    );
  }

  onClickAlert() {
    this.setState({
      expanded: !this.state.expanded
    });

    if (this.state.expanded) {
      const node = ReactDOM.findDOMNode(this);
      node.scrollTop = 0;
    }
  }

  filterHostAndOrg(name) {
    const {branchInfo} = this.props;
    let revisedName = name;

    if (revisedName.startsWith(branchInfo.host)) {
      revisedName = revisedName.substr(branchInfo.host.length + 1);
    }

    if (revisedName.startsWith(branchInfo.organization)) {
      revisedName = revisedName.substr(branchInfo.organization.length + 1);
    }

    return revisedName;
  }

  renderBuildLinks(repoBuilds) {
    const repoBuildNodes = map(repoBuilds, (name, id) => {
      return (
        <li key={id}>
          <Link to={`/builds/repo-build/${id}`}>
            {this.filterHostAndOrg(name)}
          </Link>
        </li>
      );
    });

    return <ul>{repoBuildNodes}</ul>;
  }

  renderBuildDetails(repoBuilds, buildType) {
    if (isEmpty(repoBuilds)) {
      return null;
    }

    return (
      <div className={`inter-project-alert__${buildType.toLowerCase()}`}>
        {buildType !== 'Failed' ? <h3>{buildType} builds</h3> : null}
        {this.renderBuildLinks(repoBuilds)}
      </div>
    );
  }

  renderCancelledModules() {
    const {cancelledDownstreamModules} = this.props.upAndDownstreamModules;

    if (!cancelledDownstreamModules.length) {
      return null;
    }

    const renderedCancelledModules = cancelledDownstreamModules.map((module, key) => {
      return (
        <li key={key}>
          <span>{module.name}</span>
        </li>
      );
    });

    return (
      <div className="inter-project-alert__cancelled">
        <h3>Cancelled Module Builds</h3>
        <ul>{renderedCancelledModules}</ul>
      </div>
    );
  }

  renderDetails() {
    const {
      upstreamRepoBuilds,
      downstreamRepoBuilds,
      failedRepoBuilds
    } = this.props.upAndDownstreamModules;

    const detailsClassNames = classNames(
      'inter-project-alert__details', {
        'expanded': this.state.expanded
      }
    );

    return (
      <div className={detailsClassNames}>
        {this.renderBuildDetails(failedRepoBuilds, 'Failed')}
        {this.renderBuildDetails(upstreamRepoBuilds, 'Upstream')}
        {this.renderBuildDetails(downstreamRepoBuilds, 'Downstream')}
        {this.renderCancelledModules()}
      </div>
    );
  }

  renderStatus() {
    const {state} = this.props.upAndDownstreamModules;

    return (
      <span className={this.getStatusClassNames()}>{' '}{state.toLowerCase()}</span>
    );
  }

  renderExpandText() {
    return (
      <a className='inter-project-alert__expand'>
        {this.state.expanded ? 'hide' : 'show'} details
      </a>
    );
  }

  renderRootInfo() {
    const {interProjectBuildId, rootRepoBuilds} = this.props.upAndDownstreamModules;

    const rootRepoBuildId = Object.keys(rootRepoBuilds)[0];
    const triggeredBy = rootRepoBuildId ? <Link to={`/builds/repo-build/${rootRepoBuildId}`}>{this.filterHostAndOrg(rootRepoBuilds[rootRepoBuildId])}</Link> : 'this build';

    return (
      <div className="inter-project-alert__root-info">
        <span className="inter-project-alert__build-number">#{interProjectBuildId}</span>
        <span className="inter-project-alert__triggered-by">
          triggered by {triggeredBy}
        </span>
      </div>
    );
  }

  render() {
    const {upAndDownstreamModules} = this.props;

    if (isEmpty(upAndDownstreamModules)) {
      return null;
    }

    const {upstreamRepoBuilds, downstreamRepoBuilds, rootRepoBuilds, interProjectBuildId} = upAndDownstreamModules;

    if (isEmpty(upstreamRepoBuilds) && isEmpty(downstreamRepoBuilds) && isEmpty(rootRepoBuilds)) {
      return null;
    }

    return (
      <Alert
        onClick={this.onClickAlert}
        bsStyle='info'
        className={this.getClassNames()}
      >
        <div className="inter-project-alert__summary">
          <h3>Inter-project build details {this.renderStatus()}</h3>
          {this.renderRootInfo()}
          {this.renderExpandText()}
        </div>
        {this.renderDetails()}
      </Alert>
    );
  }
}

InterProjectAlert.propTypes = {
  upAndDownstreamModules: PropTypes.object.isRequired,
  branchInfo: PropTypes.object.isRequired
};

export default InterProjectAlert;
