/*global config*/
import { fromJS } from 'immutable';
import {has, contains} from 'underscore';
import humanizeDuration from 'humanize-duration';
import $ from 'jquery';
import PollingProvider from '../services/PollingProvider';

function _filterBuilds(builds, filter, options) {
  if (filter === 'all') {
    return builds;
  }
  
  // Temoprarily filter all builds
  // until we can query API by `inProgressBuild`
  if (filter === 'building') {
    return builds.filter((build) => {
      return has(build, 'inProgressBuild');
    });
  }
  
  if (filter === 'starred') {
    return builds.filter((build) => {
      return contains(options.ids, build.gitInfo.repositoryId);
    });
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
      item.inProgressBuild.blazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/build/${inProgressBuild.buildNumber}/modules`;
      item.inProgressBuild.duration = humanizeDuration(Date.now() - item.inProgressBuild.startTimestamp, {round: true});
    }

    if (has(item, 'lastBuild')) {
      item.lastBuild.duration = humanizeDuration(item.lastBuild.endTimestamp - item.lastBuild.startTimestamp, {round: true});
      item.lastBuild.blazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/build/${lastBuild.buildNumber}/modules`;
    }

    item.gitInfo.blazarRepositoryPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}`;
    item.gitInfo.blazarBranchPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}`;

    return item;
  });

  return fromJS(parsed);
}

function fetchBuilds(options, cb) {    
  if (this.buildsPoller) {
    this.buildsPoller.disconnect();
    this.buildsPoller = undefined;
  }

  this.buildsPoller = new PollingProvider({
    // url: `${config.apiRoot}/branches/state`,
    url: 'http://local.hubteam.com:5000/js/fixtures/builds.js',
    type: 'GET',
    dataType: 'json'
  });

  this.buildsPoller.poll((err, resp) => {
    const filteredBuilds = _filterBuilds(resp, options.filter);
    cb(err, _parse(filteredBuilds));
  });
}

// Temoprarily filter all builds
// until we can query API by repositoryId
function fetchStarredBuilds(ids, cb) {  
  const promise = $.ajax({
    url: 'http://local.hubteam.com:5000/js/fixtures/builds.js',
    type: 'GET',
    dataType: 'json'
  });
  
  promise.done((resp) => {
    const filteredBuilds = _filterBuilds(resp, 'starred', {ids: ids});
    cb(false, _parse(filteredBuilds));
  });
  
  promise.fail((err) => {
    cb(err, []);
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
  fetchBuild: fetchBuild,
  fetchStarredBuilds: fetchStarredBuilds
};
