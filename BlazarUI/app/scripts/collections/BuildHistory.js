import _ from 'underscore';
import BaseCollection from './BaseCollection';

class BuildHistory extends BaseCollection {

  constructor(build){
    this.build = build;
  }

  url() {
    let build = this.build;
    return `/api/builds/${build.url}/${build.org}/${build.repo}/${build.branch}/${build.module}`
  }


  parse(){
    this.addTimeHelpers();
  }

}

export default BuildHistory;
