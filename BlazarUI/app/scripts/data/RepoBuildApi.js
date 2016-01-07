/*global config*/
import { fromJS } from 'immutable';
import $ from 'jquery';
import PollingProvider from '../services/PollingProvider';

function _parse(data) {
  return fromJS(data);
}

function fetchModuleBuilds(options, cb) {

  const {repoBuildId} = options;
  
  if (this.buildsPoller) {
    this.buildsPoller.disconnect();
    this.buildsPoller = undefined;
  }

  this.buildsPoller = new PollingProvider({
    url: `${config.apiRoot}/branches/builds/${repoBuildId}/modules`,
    type: 'GET',
    dataType: 'json'
  });

  this.buildsPoller.poll((err, resp) => {
    if (err) {
      cb(err);
      return;
    }

    cb(err, _parse(resp));
  });
}


function stopPolling() {
  if (!this.buildsPoller) {
    return;
  }

  this.buildsPoller.disconnect();
}

function fetchModule(id) {

}

export default {
  fetchModuleBuilds: fetchModuleBuilds,
  fetchModule: fetchModule
};
