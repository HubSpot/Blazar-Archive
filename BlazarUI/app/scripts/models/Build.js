import Model from './Model';

class Build extends Model {
  constructor(build){
    super()
    this.build = build;
  }

  parse() {
    this.addTimeHelpers();
  }

  url() {
    let build = this.build;
    return `${config.apiRoot}/build/history/module/${build.moduleId}/build/${build.buildNumber}/`
  }
}

export default Build;
