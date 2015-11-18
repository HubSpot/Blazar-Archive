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
  BuildHistoryActions.updatePollingStatus(true);
  gitInfo = data;
  buildHistoryPoller();
};

BuildHistoryActions.fetchLatestHistory = () => {
  fetchHistory();
};

// Build history for provided list of module IDs
// e.g. starred modules in dashboard
BuildHistoryActions.loadModulesBuildHistory = (options) => {
  if (options.modules.length === 0) {
    BuildHistoryActions.loadModulesBuildHistorySuccess([]);
    return;
  }

  const modulesPromises = options.modules.map((module) => {
    return new BuildHistory({
      moduleId: module.moduleId
    })
      .fetch()
      .then((data) => {
        return {
          module: module,
          builds: data
        };
      })
      .fail((jqXHR) => {
        BuildHistoryActions.loadBuildHistoryError(`Sorry but we can't find any module named ${gitInfo.module}`);
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
    
    fetchHistory(() => {
      if (buildHistoryActionSettings.polling) {
        setTimeout(pollBuildHistory, config.buildsRefresh);
      }
    });
  })();
}

function fetchHistory(cb) {
  getBranchId()
    .then(getModule)
    .then(getBuildHistory)
    .then(() => {
      if (cb) {
        cb();
      }
    });
}

function getBranchId() {
  const branchDefinition = new BranchDefinition(gitInfo);
  const branchPromise =  branchDefinition.fetch();

  branchPromise
    .done( () => {
      gitInfo.branchId = branchDefinition.data.id;
    })
    
    .fail(() => {
      BuildHistoryActions.loadBuildHistoryError(`Sorry but we can't find any module named ${gitInfo.module}.`);
      return;
    });
    
  

  return branchPromise;
}

function getModule() {
  const branchModules = new BranchModules({
    branchId: gitInfo.branchId
  });

  const modulesPromise = branchModules.fetch();

  modulesPromise.done( (data, textStatus, jqXHR) => {
    const module = find(branchModules.data, (m) => {
      return m.name === gitInfo.module;
    });

    if (!module) {
      BuildHistoryActions.loadBuildHistoryError(`Sorry but we can't find any module named ${gitInfo.module}.`);
      buildHistoryActionSettings.polling = false;
    }
    
    else {
      gitInfo.moduleId = module.id;
    }
  });
  
  modulesPromise.error(() => {
    BuildHistoryActions.loadBuildHistoryError("An error occured loading this module's id");
  });

  return modulesPromise;
}

function getBuildHistory() {
  
  if (!gitInfo.moduleId) {
    return;
  }
  
  const buildHistory = new BuildHistory({
    moduleId: gitInfo.moduleId
  });

  const buildHistoryPromise = buildHistory.fetch();

  buildHistoryPromise.done( () => {
    BuildHistoryActions.loadBuildHistorySuccess(buildHistory.data);
  });

  return buildHistoryPromise;
}



export default BuildHistoryActions;
