import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';
import moment from 'moment';


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
      const indexA = topologicalSort.indexOf(a.moduleId);
      const indexB = topologicalSort.indexOf(b.moduleId);

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

    const previousCommitUrl = previousCommit.get('url');
    const currentCommitId = commitInfo.getIn(['current', 'id']);
    const commitUrl = `${previousCommitUrl.replace('/commit/', '/compare/')}...${currentCommitId}`;

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
    const build = this.getBuildToDisplay();

    return (
      <Link className="repo-branch-card__build-number" to={build.get('blazarPath')}>
        #{build.get('buildNumber')}
      </Link>
    );
  }


  renderModuleRows(modules) {
    return modules.map((module, i) => {
      return (
        <ModuleRow
          module={module}
          key={i}
        />
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
    } else {
      buildTriggerMessage = 'automatically by a code push';
      detailedTriggerMessage = this.buildCompareLink();
    }

    return (
      <div className="repo-branch-card__details">
        <div className="repo-branch-card__details-header">
          <span>Build {this.renderBuildNumberLink()} was started {buildTriggerMessage}</span>
          <span className="repo-branch-card__expanded-author">{detailedTriggerMessage}</span>
        </div>
        <div className="repo-branch-card__module-rows">
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
