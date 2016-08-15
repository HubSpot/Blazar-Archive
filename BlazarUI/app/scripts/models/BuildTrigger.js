import Model from './Model';

class BuildTrigger extends Model {

  constructor(options) {
    options.type = 'POST';
    super(options);
  }

  url() {
    return `${window.config.apiRoot}/build/module/${this.options.moduleId}`;
  }
}

export default BuildTrigger;
