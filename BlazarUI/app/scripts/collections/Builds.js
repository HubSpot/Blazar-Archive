import _ from 'underscore';
import BaseCollection from './BaseCollection';

class Builds extends BaseCollection {

  url() {
    return "/api/builds";
  }

  parse(){
    this.addTimeHelpers();
    this.groupBuilds();
  }

  groupBuilds() {
    // To do: sort them by descending order of time being built
    // To do: if the job is dead, sort by order of last built

    // list of module names, used for sidebar search
    this.data.modules = _.map(this.data.data, function(item){
      let module = { value: item.module.name, label: `${item.gitInfo.repository} » ${item.module.name}` };
      return module;
    });

    // jobs grouped by repo
    this.data.grouped = _(this.data.data).groupBy(function(o) {
      return o.gitInfo.repository;
    });

    _.each(this.data.grouped, (repo) => {
      repo.moduleIsBuilding = false;
      for (var value of repo) {
        repo.repository = value.gitInfo.repository;
        if(value.buildState.result === 'IN_PROGRESS'){
          repo.moduleIsBuilding = true;
          break;
        }
      }
      return repo;
    })


  }


}

export default Builds;
