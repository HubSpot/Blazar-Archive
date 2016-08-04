import $ from 'jquery';
import {has, contains} from 'underscore';
import humanizeDuration from 'humanize-duration';
import PollingProvider from '../services/PollingProvider';
import StarProvider from '../services/starProvider';

function _groupBuilds(builds) {
  const stars = StarProvider.getStars();

  const groupedBuilds = { all: builds };

  groupedBuilds.building = builds.filter((build) => {
    return has(build, 'inProgressBuild');
  });

  groupedBuilds.starred = builds.filter((build) => {
    return contains(stars, build.gitInfo.id);
  }) || [];

  return groupedBuilds;
}

function _parse(data) {
  const parsed = data.map((item) => {
    const {
      gitInfo,
      lastBuild,
      inProgressBuild
    } = item;

    if (has(item, 'inProgressBuild')) {
      item.inProgressBuild.duration = humanizeDuration(Date.now() - item.inProgressBuild.startTimestamp, {round: true});
      item.inProgressBuild.blazarPath = `/builds/branch/${gitInfo.id}/build/${inProgressBuild.buildNumber}`;
    }

    if (has(item, 'lastBuild')) {
      item.lastBuild.duration = humanizeDuration(item.lastBuild.endTimestamp - item.lastBuild.startTimestamp, {round: true});
      item.lastBuild.blazarPath = `/builds/branch/${gitInfo.id}/build/${lastBuild.buildNumber}`;
    }

    item.gitInfo.blazarRepositoryPath = `/builds/repo/${gitInfo.repository}`;
    item.gitInfo.blazarBranchPath = `/builds/branch/${gitInfo.id}`;

    return item;
  });

  return parsed;
}

function fetchBuilds(extraData, cb) {
  if (this.buildsPoller) {
    this.buildsPoller.disconnect();
    this.buildsPoller = undefined;
  }

  let exclusionOpts = {
    property: [
      '!pendingBuild.buildOptions',
      '!inProgressBuild.buildOptions',
      '!lastBuild.buildOptions'
    ]
  };

  if (!extraData) {
    exclusionOpts = {
      property: [
        '!pendingBuild.commitInfo',
        '!inProgressBuild.commitInfo',
        '!lastBuild.commitInfo',
        '!pendingBuild.dependencyGraph',
        '!inProgressBuild.dependencyGraph',
        '!lastBuild.dependencyGraph',
        '!pendingBuild.buildTrigger',
        '!inProgressBuild.buildTrigger',
        '!lastBuild.buildTrigger',
        '!pendingBuild.buildOptions',
        '!inProgressBuild.buildOptions',
        '!lastBuild.buildOptions'
      ]
    };
  }

  this.buildsPoller = new PollingProvider({
    url: `${window.config.apiRoot}/branches/state`,
    type: 'GET',
    dataType: 'json',
    data: $.param(exclusionOpts).replace(/%5B%5D/g, '')
  });

  this.buildsPoller.poll((err, resp) => {
    if (err) {
      cb(err);
      return;
    }

    cb(err, _groupBuilds(_parse(resp)));
  });
}

function stopPolling() {
  if (!this.buildsPoller) {
    return;
  }

  this.buildsPoller.disconnect();
}

function fetchBuild() {

}

export default {
  fetchBuilds,
  fetchBuild,
  stopPolling
};
