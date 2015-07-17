import Model from './Model';

class Build extends Model {
  constructor(build){
    this.build = build;
  }

  parse() {
    this.addTimeHelpers();
  }

  url() {
    let build = this.build;
    return `/api/builds/${build.url}/${build.org}/${build.repo}/${build.branch}/${build.module}/${build.buildNumber}`
  }
}

export default Build;
