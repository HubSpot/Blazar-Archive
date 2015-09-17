/*global config*/
import Model from './Model';

class BuildTrigger extends Model {
  constructor(moduleId) {
    super();
    this.moduleId = moduleId;
    this.fetchOptions = {
      type: 'POST'
    };
  }

  url() {
    const moduleId = this.moduleId;
    return `${config.apiRoot}/build/module/${moduleId}`;
  }
}

export default BuildTrigger;
