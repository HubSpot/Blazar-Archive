import React, {Component, PropTypes} from 'react';
import {isEmpty, bindAll} from 'underscore';
import {Link} from 'react-router';
import Alert from 'react-bootstrap/lib/Alert';
import {contains} from 'underscore';
import classNames from 'classnames';

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
  }

  renderBuildLinks(repoBuilds, isRootBuild = false) {
    return Object.keys(repoBuilds).map((value, key) => {
      return (
        <li key={key}>
          <Link to={`/builds/repo-build/${value}`}>
            {repoBuilds[value]} {isRootBuild ? <b>(root build)</b> : null}
          </Link>
        </li>
      );
    });
  }

  renderFailedBuildDetails() {
    const {failedRepoBuilds} = this.props.upAndDownstreamModules;

    if (Object.keys(failedRepoBuilds).length === 0) {
      return null;
    }

    return (
      <div className="inter-project-alert__failed">
        <h4>Failed Builds:</h4>
        <ul>{this.renderBuildLinks(failedRepoBuilds)}</ul>
      </div>
    );
  }

  renderUpstreamBuildDetails() {
    const {upstreamRepoBuilds, rootRepoBuilds}  = this.props.upAndDownstreamModules;

    if (!(Object.keys(upstreamRepoBuilds).length + Object.keys(rootRepoBuilds).length)) {
      return null;
    }

    return (
      <div className="inter-project-alert__upstream">
        <h4>Upstream builds:</h4>
        <ul>
          {this.renderBuildLinks(rootRepoBuilds, true)}
          {this.renderBuildLinks(upstreamRepoBuilds)}
        </ul>
      </div>
    );
  }

  renderDownstreamBuildDetails() {
    const {downstreamRepoBuilds}  = this.props.upAndDownstreamModules;

    if (Object.keys(downstreamRepoBuilds).length === 0) {
      return null;
    }

    return (
      <div className="inter-project-alert__downstream">
        <h4>Downstream builds:</h4>
        <ul>{this.renderBuildLinks(downstreamRepoBuilds)}</ul>
      </div>
    );
  }

  renderCancelledModules() {
    const {cancelledDownstreamModules} = this.props.upAndDownstreamModules;

    if (cancelledDownstreamModules.length === 0) {
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
        <h4>Cancelled Module Builds</h4>
        <ul>{renderedCancelledModules}</ul>
      </div>
    );
  }

  renderRootBuildText() {
    const {rootRepoBuilds} = this.props.upAndDownstreamModules;

    if (!Object.keys(rootRepoBuilds).length) {
      return <p>This is a root build of the inter-project build.</p>;
    }
  }

  renderDetails() {
    const detailsClassNames = classNames(
      'inter-project-alert__details', {
        'expanded': this.state.expanded
      }
    );

    return (
      <div className={detailsClassNames}>
        {this.renderRootBuildText()}
        {this.renderFailedBuildDetails()}
        {this.renderUpstreamBuildDetails()}
        {this.renderDownstreamBuildDetails()}
        {this.renderCancelledModules()}
      </div>
    );
  }

  renderStatus() {
    const {state} = this.props.upAndDownstreamModules;

    return (
      <span className={this.getStatusClassNames()}>{state}</span>
    );
  }

  renderExpandText() {
    return (
      <a className='inter-project-alert__expand'>
        {this.state.expanded ? 'hide' : 'show'} details
      </a>
    );
  }

  render() {
    const {upAndDownstreamModules} = this.props;

    if (isEmpty(upAndDownstreamModules)) {
      return null;
    }

    const {upstreamRepoBuilds, downstreamRepoBuilds, rootRepoBuilds, interProjectBuildId} = upAndDownstreamModules;

    if (!(Object.keys(upstreamRepoBuilds).length + Object.keys(downstreamRepoBuilds).length  + Object.keys(rootRepoBuilds).length)) {
      return null;
    }

    return (
      <Alert onClick={this.onClickAlert} bsStyle='info' className={this.getClassNames()}>
        <h3>Inter-Project Build {interProjectBuildId}: {this.renderStatus()} {this.renderExpandText()}</h3>
        {this.renderDetails()}
      </Alert>
    );
  }
}

InterProjectAlert.propTypes = {
  upAndDownstreamModules: PropTypes.object.isRequired
};

export default InterProjectAlert;
