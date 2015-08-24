import Reflux from 'reflux';
import $ from 'jquery';
import _ from 'underscore';
import ActionSettings from './utils/ActionSettings';
import BuildHistory from '../collections/BuildHistory';
import BranchDefinition from '../models/BranchDefinition';
import BranchModules from '../collections/BranchModules';

var gitInfo;

let buildHistoryActionSettings = new ActionSettings;

let BuildHistoryActions = Reflux.createActions([
  'loadBuildHistory',
  'loadBuildHistorySuccess',
  'loadBuildHistoryError',
  'updatePollingStatus'
]);

BuildHistoryActions.loadBuildHistory.preEmit = function(data) {
  startPolling(data);
};

BuildHistoryActions.updatePollingStatus = function (status) {
  buildHistoryActionSettings.setPolling(status);
};

function getBranchId() {
    let branchDefinition = new BranchDefinition(gitInfo);
    let branchPromise =  branchDefinition.fetch();

    branchPromise.done( () => {
      gitInfo.branchId = branchDefinition.data.id;
      getModule();
    });
    branchPromise.error( () => {
      BuildHistoryActions.loadBuildHistoryError('an error occured');
    })
}

function getModule() {
  let branchModules = new BranchModules(gitInfo.branchId);
  let modulesPromise = branchModules.fetch();

  modulesPromise.done( () => {
    gitInfo.moduleId = _.find(branchModules.data, (m) => {
      return m.name === gitInfo.module;
    }).id;
    getBuildHistory();
  });
  modulesPromise.error( () => {
    BuildHistoryActions.loadBuildHistoryError('an error occured');
  })
}

function getBuildHistory() {
    let buildHistory = new BuildHistory(gitInfo);
    let promise = buildHistory.fetch();

    promise.done( () => {
      BuildHistoryActions.loadBuildHistorySuccess(buildHistory.data);
    });
    promise.error( () => {
      BuildHistoryActions.loadBuildHistoryError('an error occured');
    })
}

function startPolling(data){
  gitInfo = data;

  (function doPoll() {
    getBranchId();
    if (buildHistoryActionSettings.polling) {
      setTimeout(doPoll, config.buildsRefresh);
    }
  })();
}


export default BuildHistoryActions;
