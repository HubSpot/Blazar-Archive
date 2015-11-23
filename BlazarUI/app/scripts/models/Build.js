/*global config*/
import Model from './Model';

class Build extends Model {
  
  parse() {
    this.addTimeHelpers();
  }

  cancel() {
    this.options = {
      type: 'POST',
      url: `${config.apiRoot}/build/${this.options.buildId}/cancel`,
      parse: false
    };
    return this.fetch({
      parse: false
    });
  }

  url() {
    return `${config.apiRoot}/build/history/module/${this.moduleId}/build/${this.options.buildNumber}/`;
  }
}

export default Build;
