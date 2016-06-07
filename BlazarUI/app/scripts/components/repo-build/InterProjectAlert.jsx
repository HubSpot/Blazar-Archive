import React, {Component, PropTypes} from 'react';
import {isEmpty, bindAll} from 'underscore';
import {Link} from 'react-router';
import Alert from 'react-bootstrap/lib/Alert';

const initialState = {
  expanded: false
};

class InterProjectAlert extends Component {

  constructor(props) {
    super(props);

    this.state = initialState;
    bindAll(this, 'onClickAlert');
  }

  onClickAlert() {
    this.setState({
      expanded: !this.state.expanded
    });
  }

  renderBuildLinks(repoBuilds) {
    let renderedLinks = [];

    Object.keys(repoBuilds).map((value, key) => {
      renderedLinks.push(
        <li key={key}>
          <Link to={`/builds/repo-build/${value}`}>
            {repoBuilds[value]}
          </Link>
        </li>
      );
    });

    return renderedLinks;
  }

  renderUpstreamBuildDetails() {
    const {upstreamRepoBuilds}  = this.props.upAndDownstreamModules;

    if (Object.keys(upstreamRepoBuilds).length === 0) {
      return null;
    }

    return (
      <div className="upstream-builds">
        <h4>Upstream builds:</h4>
        <ul>{this.renderBuildLinks(upstreamRepoBuilds)}</ul>
      </div>
    );
  }

  renderDownstreamBuildDetails() {
    const {downstreamRepoBuilds}  = this.props.upAndDownstreamModules;

    if (Object.keys(downstreamRepoBuilds).length === 0) {
      return null;
    }

    return (
      <div className="downstream-builds">
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
      <div className="cancelled-modules">
        <h4>Cancelled Module Builds</h4>
        {renderCancelledModules}
      </div>
    );
  }

  renderDetails() {
    return (
      <div className="inter-project-details">
        {this.renderUpstreamBuildDetails()}
        {this.renderDownstreamBuildDetails()}
        {this.renderCancelledModules()}
      </div>
    );
  }

  renderTriggerCount() {
    const {upstreamRepoBuilds, downstreamRepoBuilds} = this.props.upAndDownstreamModules;

    const numUpstreamRepoBuilds = Object.keys(upstreamRepoBuilds).length;
    const pluralUpstream = numUpstreamRepoBuilds !== 1 ? 's' : '';
    const numDownstreamRepoBuilds = Object.keys(downstreamRepoBuilds).length;
    const pluralDownstream = numDownstreamRepoBuilds !== 1 ? 's' : '';

    let upstreamText = '';
    let downstreamText = '';

    if (numUpstreamRepoBuilds) {
      upstreamText = `was triggered by ${numUpstreamRepoBuilds} upstream build${pluralUpstream}`;
    }

    if (numDownstreamRepoBuilds) {
      downstreamText = `triggered ${numDownstreamRepoBuilds} downstream build${pluralDownstream}`;
    }

    return `This inter-project build ${upstreamText}${numUpstreamRepoBuilds && numDownstreamRepoBuilds ? ' and ' : ''}${downstreamText}.`;
  }

  render() {
    const {upAndDownstreamModules} = this.props;

    if (isEmpty(upAndDownstreamModules)) {
      return null;
    }

    const {upstreamRepoBuilds, downstreamRepoBuilds} = upAndDownstreamModules;

    if (Object.keys(upstreamRepoBuilds).length === 0 && Object.keys(downstreamRepoBuilds).length === 0) {
      return null;
    }

    if (!this.state.expanded) {
      return (
        <Alert onClick={this.onClickAlert} bsStyle="info" className="inter-project-alert">
          <h3>Inter-Project Build</h3>
          {this.renderTriggerCount()}<br />
          Click this alert for details.
        </Alert>
      );
    }

    return (
      <Alert onClick={this.onClickAlert} bsStyle="info" className="inter-project-alert">
        <h3>Inter-Project Build</h3>
        {this.renderTriggerCount()}<br /><br />
        {this.renderDetails()}
      </Alert>
    );
  }
}

InterProjectAlert.propTypes = {
  upAndDownstreamModules: PropTypes.object.isRequired
};

export default InterProjectAlert;
