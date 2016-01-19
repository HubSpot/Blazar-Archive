/*global config*/
import { fromJS } from 'immutable';
import {has, contains} from 'underscore';
import humanizeDuration from 'humanize-duration';
import PollingProvider from '../services/PollingProvider';
import StarProvider from '../services/starProvider';

function _groupBuilds(builds) {
  const stars = StarProvider.getStars();
  
  let groupedBuilds = { all: fromJS(builds) };
  
  groupedBuilds.building = fromJS(builds.filter((build) => {
    return has(build, 'inProgressBuild');
  }));
  
  groupedBuilds.starred = fromJS(builds.filter((build) => {
    return contains(stars, build.gitInfo.id);
  })) || Immutable.List.of();

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
      item.inProgressBuild.blazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${inProgressBuild.buildNumber}`;
      item.inProgressBuild.duration = humanizeDuration(Date.now() - item.inProgressBuild.startTimestamp, {round: true});
    }

    if (has(item, 'lastBuild')) {
      item.lastBuild.duration = humanizeDuration(item.lastBuild.endTimestamp - item.lastBuild.startTimestamp, {round: true});
      item.lastBuild.blazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${lastBuild.buildNumber}`;
    }

    item.gitInfo.blazarRepositoryPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}`;
    item.gitInfo.blazarBranchPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}`;
    item.gitInfo.blazarHostPath = `${config.appRoot}/builds/${gitInfo.host}`;

    return item;
  });

  return parsed;
}

function fetchBuilds(cb) {
  
  if (this.buildsPoller) {
    this.buildsPoller.disconnect();
    this.buildsPoller = undefined;
  }

  this.buildsPoller = new PollingProvider({
    url: `${config.apiRoot}/branches/state`,
    type: 'GET',
    dataType: 'json'
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

function fetchBuild(id) {

}

export default {
  fetchBuilds: fetchBuilds,
  fetchBuild: fetchBuild
};
