import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';

import ModuleRow from './ModuleRow.jsx';

class RepoBranchCardDetails extends Component {
  getBuildToDisplay() {
    const {item} = this.props;

    if (item.get('inProgressBuild') !== undefined) {
      return item.get('inProgressBuild');
    }

    return item.get('lastBuild');
  }

  sortModules() {
    const {moduleBuildsList} = this.props;
    const build = this.getBuildToDisplay().toJS();

    if (!build || !build.dependencyGraph || !build.dependencyGraph.topologicalSort) {
      return moduleBuildsList;
    }

    const topologicalSort = build.dependencyGraph.topologicalSort;

    return moduleBuildsList.sort((a, b) => {
      let indexA = topologicalSort.indexOf(a.moduleId);
      let indexB = topologicalSort.indexOf(b.moduleId);

      if (indexA < indexB) {
        return -1;
      }

      return 1;
    });
  }

  buildCompareLink() {
    const commitInfo = this.getBuildToDisplay().get('commitInfo');
    const previousCommit = commitInfo.get('previous');

    if (!previousCommit) {
      return null;
    }

    const currentCommit = commitInfo.get('current');
    const commitUrl = previousCommit.get('url').replace('/commit/', '/compare/') + '...' + currentCommit.get('id');

    return (
      <a href={commitUrl}>
        compare
      </a>
    );
  }

  getBuildTime(startTimestamp) {
    const timestampText = moment(startTimestamp).fromNow();

    if (timestampText === 'a day ago') {
      return 'yesterday';
    }

    return timestampText;
  }

  renderBuildNumberLink() {
    const {item} = this.props;
    const build = this.getBuildToDisplay();
    const gitInfo = item.get('gitInfo');

    return (
      <Link className='repo-branch-card__build-number' to={build.get('blazarPath')}>
        #{build.get('buildNumber')}
      </Link>
    );
  }


  renderModuleRows(modules) {
    return modules.map((module, i) => {
      return (
        <ModuleRow
          module={module}
          key={i} />
      );
    });
  }

  render() {
    const build = this.getBuildToDisplay();

    let buildTriggerMessage;
    let detailedTriggerMessage;

    if (build.get('buildTrigger') && build.get('buildTrigger').get('type') === 'MANUAL') {
      buildTriggerMessage = 'manually';
      const buildTime = this.getBuildTime(build.get('startTimestamp'));
      const author = build.get('buildTrigger').get('id');
      detailedTriggerMessage = `Triggered ${buildTime}${author === 'unknown' ? '' : ` by ${author}`}`;
    }

    else {
      buildTriggerMessage = 'automatically by a code push';
      detailedTriggerMessage = this.buildCompareLink();
    }

    return (
      <div className='repo-branch-card__details'>
        <div className='repo-branch-card__details-header'>
          <span>Build {this.renderBuildNumberLink()} was started {buildTriggerMessage}</span>
          <span className='repo-branch-card__author'>{detailedTriggerMessage}</span>
        </div>
        <div className='repo-branch-card__module-rows'>
          {this.renderModuleRows(this.sortModules(this.props.moduleBuildsList))}
        </div>
      </div>
    );
  }
}

RepoBranchCardDetails.propTypes = {
  item: PropTypes.object.isRequired,
  moduleBuildsList: PropTypes.array
};

export default RepoBranchCardDetails;
