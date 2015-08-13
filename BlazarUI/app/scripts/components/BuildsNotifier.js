import Notify from 'notifyjs';
import WatchingProvider from './WatchingProvider';

let lastModuleStates = {};

let BuildsNotifier = {

  updateModules: function(modules) {
    modules.forEach((module) => {
      let id = { repo: module.repository, branch: module.branch };
      if (WatchingProvider.isWatching(id) === -1) {
        return;
      }

      if (lastModuleStates[id] === 'IN_PROGRESS' && module.inProgressBuildLink === undefined) {
        this.showNotification(module.repository, module.branch, module.module, module.lastBuildState, module.link);
      }

      lastModuleStates[id] = (module.inProgressBuildLink !== undefined ? 'IN_PROGRESS' : module.lastBuildState);
    });
  },

  showNotification: function(repo, branch, module, state, link) {
    let body = `${repo}[${branch}] ${module}: ${state}`;
    var notification = new Notify('Build Complete', {
        body: body,
        icon: '/images/icon.jpg',
        notifyClick: () => {
          window.open(link);
        }
    });
    notification.show();
  }
};

export default BuildsNotifier;
