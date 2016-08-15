import BaseCollection from './BaseCollection';

class BuildHistory extends BaseCollection {

  url() {
    return `${window.config.apiRoot}/build/history/module/${this.options.moduleId}`;
  }
}

export default BuildHistory;
