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
    // jobs grouped by repo
    let grouped = _(this.data).groupBy(function(o) {
      return o.gitInfo.repository;
    });

    _.each(grouped, (repo) => {
      repo.moduleIsBuilding = false;
      for (var value of repo) {
        if(value.buildState.result === 'IN_PROGRESS'){
          repo.moduleIsBuilding = true;
          break;
        }
      }
      return repo;
    })

    let groupedInArray = [];
    for (var repo in grouped) {
      groupedInArray.push({
        name: repo,
        isBuilding: grouped[repo].moduleIsBuilding,
        modules: grouped[repo]
      })
      // sorted.push([repo, grouped[repo]])
      // sorted.sort(function(a, b) {return a[1] - b[1]})
    }

    // To do: sort repos in order of most recent module in progress
    // To do: if the job is dead, sort by order of last built
    return groupedInArray;

  }


}

export default Builds;
