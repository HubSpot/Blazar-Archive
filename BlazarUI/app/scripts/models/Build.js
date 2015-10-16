/*global config*/
import Model from './Model';

class Build extends Model {
  constructor(build) {
    super();
    this.build = build;
  }

  parse() {
    this.addTimeHelpers();
  }
  
  cancel(id) {
    this.fetchOptions = {
      type: 'POST',
      url: `${config.apiRoot}/build/${id}/cancel`,
      parse: false
    }
    return this.fetch({
      parse: false
    });
  }

  url() {
    const build = this.build;
    return `${config.apiRoot}/build/history/module/${build.moduleId}/build/${build.buildNumber}/`;
  }
}

export default Build;
