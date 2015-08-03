import _ from 'underscore';
import BaseCollection from './BaseCollection';

class BuildHistory extends BaseCollection {

  constructor(build){
    this.build = build;
  }

  url() {
    let build = this.build;

    return `/api/build/history/module/${build.module}`
  }


  parse(){
    this.addTimeHelpers();
  }

}

export default BuildHistory;
