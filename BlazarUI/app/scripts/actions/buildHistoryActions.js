/*global config*/
import Reflux from 'reflux';
import ActionSettings from './utils/ActionSettings';
import BuildHistory from '../collections/BuildHistory';
import BranchDefinition from '../models/BranchDefinition';
import BranchModules from '../collections/BranchModules';

let gitInfo;

const buildHistoryActionSettings = new ActionSettings;

const BuildHistoryActions = Reflux.createActions([
  'loadBuildHistory',
  'loadModulesBuildHistory',
  'loadBuildHistorySuccess',
  'loadBuildHistoryError',
  'loadModulesBuildHistorySuccess',
  'updatePollingStatus'
]);

BuildHistoryActions.loadBuildHistory.preEmit = (data) => {
  startPolling(data);
};

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

function getBranchId() {
    const branchDefinition = new BranchDefinition(gitInfo);
    const branchPromise =  branchDefinition.fetch();

    branchPromise.done( () => {
      gitInfo.branchId = branchDefinition.data.id;
      getModule();
    });
    branchPromise.error( () => {
      BuildHistoryActions.loadBuildHistoryError('an error occured');
    });
}

function getModule() {
  const branchModules = new BranchModules(gitInfo.branchId);
  const modulesPromise = branchModules.fetch();

  modulesPromise.done( () => {
    gitInfo.moduleId = branchModules.data.find((m) => {
      return m.name === gitInfo.module;
    }).id;
    getBuildHistory();
  });
  modulesPromise.error( () => {
    BuildHistoryActions.loadBuildHistoryError('an error occured');
  });
}

function getBuildHistory() {
  const buildHistory = new BuildHistory(gitInfo.moduleId);
  const promise = buildHistory.fetch();

  promise.done( () => {
    BuildHistoryActions.loadBuildHistorySuccess(buildHistory.data);
  });
  promise.error( () => {
    BuildHistoryActions.loadBuildHistoryError('an error occured');
  });
}

function startPolling(data) {
  gitInfo = data;

  (function doPoll() {
    getBranchId();
    if (buildHistoryActionSettings.polling) {
      setTimeout(doPoll, config.buildsRefresh);
    }
  })();
}


export default BuildHistoryActions;
