import _ from 'underscore';
import BaseCollection from './BaseCollection';

class Builds extends BaseCollection {

  url() {
    return "/api/builds";
  }

  parse() {
    this.addTimeHelpers();
  }

  getModuleList() {
    // list of module names, used for sidebar search
    let modules = _.map(this.data, function(item){
      let module = { value: item.module.name, label: `${item.gitInfo.repository} » ${item.module.name}` };
      return module;
    });

    return modules;
  }


  groupBuilds() {
    // To do: sort them by descending order of time being built
    // To do: if the job is dead, sort by order of last built

    // jobs grouped by repo
    let grouped = _(this.data).groupBy(function(o) {
      return o.gitInfo.repository;
    });

    _.each(grouped, (repo) => {
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

    return grouped;

  }


}

export default Builds;
