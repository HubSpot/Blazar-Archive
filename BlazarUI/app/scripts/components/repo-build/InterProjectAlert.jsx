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

  renderBuildLinks(repoBuilds) {
    const listItemNodes = Object.keys(repoBuilds).map((value, key) => {
      return (
        <li key={key}>
          <Link to={`/builds/repo-build/${value}`}>
            {repoBuilds[value]}
          </Link>
        </li>
      );
    });

    return <ul>{listItemNodes}</ul>;
  }

  renderFailedBuildDetails() {
    const {failedRepoBuilds} = this.props.upAndDownstreamModules;

    if (Object.keys(failedRepoBuilds).length === 0) {
      return null;
    }

    return (
      <div className="inter-project-alert__failed">
        <h4>Failed Builds:</h4>
        {this.renderBuildLinks(failedRepoBuilds)}
      </div>
    );
  }

  renderUpstreamBuildDetails() {
    const {upstreamRepoBuilds}  = this.props.upAndDownstreamModules;

    if (Object.keys(upstreamRepoBuilds).length === 0) {
      return null;
    }

    return (
      <div className="inter-project-alert__upstream">
        <h4>Upstream builds:</h4>
        {this.renderBuildLinks(upstreamRepoBuilds)}
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
        {this.renderBuildLinks(downstreamRepoBuilds)}
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

  renderDetails() {
    return (
      <div className="inter-project-alert__details">
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

    const {upstreamRepoBuilds, downstreamRepoBuilds, interProjectBuildId} = upAndDownstreamModules;

    if (Object.keys(upstreamRepoBuilds).length === 0 && Object.keys(downstreamRepoBuilds).length === 0) {
      return null;
    }

    return (
      <Alert onClick={this.onClickAlert} bsStyle='info' className={this.getClassNames()}>
        <h3>Inter-Project Build {interProjectBuildId}: {this.renderStatus()} {this.renderExpandText()}</h3>
        {this.state.expanded ? this.renderDetails() : null}
      </Alert>
    );
  }
}

InterProjectAlert.propTypes = {
  upAndDownstreamModules: PropTypes.object.isRequired
};

export default InterProjectAlert;
