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
    return `/api/build/${build.buildNumber}/`
  }
}

export default Build;
