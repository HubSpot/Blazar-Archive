/*global config*/
import { fromJS } from 'immutable';
import {has, contains} from 'underscore';
import humanizeDuration from 'humanize-duration';
import $ from 'jquery';
import PollingProvider from '../services/PollingProvider';
import StarProvider from '../services/starProvider';

function _filterBuilds(builds, filter) {
  
  if (filter === 'all') {
    return builds;
  }

  if (filter === 'building') {
    return builds.filter((build) => {
      return has(build, 'inProgressBuild');
    });
  }
  
  if (filter === 'starred') {
    const ids = StarProvider.getStars();
    return builds.filter((build) => {
      return contains(ids, build.gitInfo.id);
    }) || [];
  }
}

function _parse(data) {
  const parsed = data.map((item) => {

    const {
      gitInfo,
      lastBuild,
      inProgressBuild
    } = item;

    if (has(item, 'inProgressBuild')) {
      item.inProgressBuild.blazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${inProgressBuild.id}`;
      item.inProgressBuild.duration = humanizeDuration(Date.now() - item.inProgressBuild.startTimestamp, {round: true});
    }

    if (has(item, 'lastBuild')) {
      item.lastBuild.duration = humanizeDuration(item.lastBuild.endTimestamp - item.lastBuild.startTimestamp, {round: true});
      item.lastBuild.blazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${lastBuild.id}`;
    }

    item.gitInfo.blazarRepositoryPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}`;
    item.gitInfo.blazarBranchPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}`;

    return item;
  });

  return fromJS(parsed);
}

function fetchBuilds(options, cb) {    
  
  const {filter} = options;
  
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

    const filteredBuilds = _filterBuilds(resp, filter);
    cb(err, _parse(filteredBuilds));
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
