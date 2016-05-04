/*global config*/
import Resource from '../services/ResourceProvider';
import { fromJS } from 'immutable';
import { getUsernameFromCookie } from '../components/Helpers.js';

function triggerInterProjectBuild(moduleIds, resetCache, cb) {
<<<<<<< HEAD
  console.log('asdf');
  return;
=======
>>>>>>> 84ab3f0c27623e1a23506bb36f65970e6c1ee4e8
  if (moduleIds === null) {
    moduleIds = [];
  }
  const username = getUsernameFromCookie() ? `username=${getUsernameFromCookie()}` : '';
  const buildPromise = new Resource({
<<<<<<< HEAD
    url: `${config.apiRoot}/inter-project-builds/?${username}`,
=======
    url: `${config.apiRoot}/inter-project-builds?${username}`,
>>>>>>> 84ab3f0c27623e1a23506bb36f65970e6c1ee4e8
    type: 'POST',
    contentType: 'application/json',
    data: _generateBuildModuleJsonBody(moduleIds, 'NONE', resetCache)
  }).send();

  buildPromise.then((resp) => {
    cb(false, resp);
  }, (error) => {
    console.warn(error);
    cb('Error triggering build. Check your console for more detail.');
  });
}


function _generateBuildModuleJsonBody(moduleIds, downstreamModules, resetCache) {
  return JSON.stringify({
    moduleIds: moduleIds, 
    buildDownstreams: downstreamModules,
    resetCaches: resetCache
  });
}

export default {
  triggerInterProjectBuild: triggerInterProjectBuild
};
