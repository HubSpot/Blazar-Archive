/* global config*/
import Model from './Model';

class BuildTrigger extends Model {

  constructor(options) {
    options.type = 'POST';
    super(options);
  }

  url() {
    const moduleId = this.moduleId;
    return `${config.apiRoot}/build/module/${this.options.moduleId}`;
  }
}

export default BuildTrigger;
