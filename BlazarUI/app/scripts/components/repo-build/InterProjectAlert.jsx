import React, {Component, PropTypes} from 'react';
import ReactDOM from 'react-dom';
import {isEmpty, bindAll, map} from 'underscore';
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

    if (this.state.expanded) {
      const node = ReactDOM.findDOMNode(this);
      node.scrollTop = 0;
    }
  }

  renderBuildLinks(repoBuilds) {
    const repoBuildNodes = map(repoBuilds, (name, id) => {
      return (
        <li key={id}>
          <Link to={`/builds/repo-build/${id}`}>
            {name}
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
      <div className={`inter-project-alert__${buildType}`}>
        {buildType !== 'failed' ? <h4>{buildType} builds:</h4> : null}
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
        <h4>Cancelled Module Builds</h4>
        <ul>{renderedCancelledModules}</ul>
      </div>
    );
  }

  renderRootBuildText() {
    const {rootRepoBuilds} = this.props.upAndDownstreamModules;

    if (isEmpty(rootRepoBuilds)) {
      return <p>This is a root build of the inter-project build.</p>;
    }
  }

  renderDetails() {
    const {
      upstreamRepoBuilds,
      downstreamRepoBuilds,
      failedRepoBuilds,
      rootRepoBuilds
    } = this.props.upAndDownstreamModules;

    const detailsClassNames = classNames(
      'inter-project-alert__details', {
        'expanded': this.state.expanded
      }
    );

    return (
      <div className={detailsClassNames}>
        {this.renderRootBuildText()}
        {this.renderBuildDetails(failedRepoBuilds, 'failed')}
        {this.renderBuildDetails(rootRepoBuilds, 'root')}
        {this.renderBuildDetails(upstreamRepoBuilds, 'upstream')}
        {this.renderBuildDetails(downstreamRepoBuilds, 'downstream')}
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
        <h3>
          Inter-project build details
          <span className="inter-project-build-number">#{interProjectBuildId}</span>
          {this.renderStatus()}
          {this.renderExpandText()}
         </h3>
        {this.renderDetails()}
      </Alert>
    );
  }
}

InterProjectAlert.propTypes = {
  upAndDownstreamModules: PropTypes.object.isRequired
};

export default InterProjectAlert;
