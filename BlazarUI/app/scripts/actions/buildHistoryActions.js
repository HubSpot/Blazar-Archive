/*global config*/
import Reflux from 'reflux';
import ActionSettings from './utils/ActionSettings';
import BuildHistory from '../collections/BuildHistory';
import BranchDefinition from '../models/BranchDefinition';
import BranchModules from '../collections/BranchModules';
import {find} from 'underscore';

const buildHistoryActionSettings = new ActionSettings;

const BuildHistoryActions = Reflux.createActions([
  'loadBuildHistory',
  'loadModulesBuildHistory',
  'loadBuildHistorySuccess',
  'loadBuildHistoryError',
  'loadModulesBuildHistorySuccess',
  'updatePollingStatus'
]);

let gitInfo;

// Build history for module page:
// e.g. HubSpot/Blazar/master/BlazarUI
BuildHistoryActions.loadBuildHistory.preEmit = (data) => {
  gitInfo = data;
  buildHistoryPoller();
};

// Build history for provided list of module IDs
// e.g. starred modules in dashboard
BuildHistoryActions.loadModulesBuildHistory = (options) => {
  if (options.modules.length === 0) {
    BuildHistoryActions.loadModulesBuildHistorySuccess([]);
    return;
  }

  const modulesPromises = options.modules.map((module) => {
    return new BuildHistory(module.moduleId)
      .fetch().then((data) => {
        data = data.splice(0, options.limit);
        return {
          module: module,
          builds: data
        };
      });
  });

  Promise.all(modulesPromises).then(res => {
    BuildHistoryActions.loadModulesBuildHistorySuccess(res);
  });

};

BuildHistoryActions.updatePollingStatus = (status) => {
  buildHistoryActionSettings.setPolling(status);
};


function buildHistoryPoller() {

  (function pollBuildHistory() {
    getBranchId()
      .then(getModule)
      .then(getBuildHistory);

    if (buildHistoryActionSettings.polling) {
      setTimeout(pollBuildHistory, config.buildsRefresh);
    }
  })();
}

function getBranchId() {
  const branchDefinition = new BranchDefinition(gitInfo);
  const branchPromise =  branchDefinition.fetch();

  branchPromise.done( () => {
    gitInfo.branchId = branchDefinition.data.id;
  });

  return branchPromise;
}

function getModule() {
  const branchModules = new BranchModules(gitInfo.branchId);
  const modulesPromise = branchModules.fetch();

  modulesPromise.done( () => {
    gitInfo.moduleId = find(branchModules.data, (m) => {
      return m.name === gitInfo.module;
    }).id;
  });

  return modulesPromise;
}

function getBuildHistory() {
  const buildHistory = new BuildHistory(gitInfo.moduleId);
  const buildHistoryPromise = buildHistory.fetch();

  buildHistoryPromise.done( () => {
    BuildHistoryActions.loadBuildHistorySuccess(buildHistory.data);
  });

  return buildHistoryPromise;
}



export default BuildHistoryActions;
