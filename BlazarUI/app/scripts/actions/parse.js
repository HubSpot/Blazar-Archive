import _ from 'underscore';
import moment from 'moment';

class Parse {

  constructor(data) {
    this.parsed = data;
  }

  addTimeHelpers() {
    this.parsed.forEach( (item) => {
      if (item.buildState.startTime && item.buildState.endTime) {
        item.buildState.duration = moment.duration(item.buildState.endTime - item.buildState.startTime).humanize();
      }
    });
    return this;
  }

  groupJobs() {
    // list of module names, used for sidebar search
    let modules = [];
    _.filter(this.parsed, function(item){
      let module = { value: item.module.name, label: `${item.gitInfo.repository} » ${item.module.name}` };
      modules.push(module);
    });
    // jobs grouped by repo
    let grouped = _(this.parsed).groupBy(function(o) {
      return o.gitInfo.repository;
    });

    // determine if any of the repos have a module that is building
    _.each(grouped, function(repo){
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

    // To do: sort them by descending order of time being built
    // To do: if the job is dead, sort by order of last built
    this.grouped = grouped;
    this.modules = modules;

    return this;

  }



}

export default Parse;
