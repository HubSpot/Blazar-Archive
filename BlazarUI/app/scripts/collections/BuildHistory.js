import _ from 'underscore';
import BaseCollection from './BaseCollection';

class BuildHistory extends BaseCollection {

  constructor(build){
    this.build = build;
  }

  url() {
    let build = this.build;
    let moduleId = _.last(build.module.split('_'));
    return `${config.apiRoot}build/history/module/${moduleId}`
  }


  parse(){
    this.addTimeHelpers();
  }

}

export default BuildHistory;
