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

      let {gitInfo, module, buildState} = item;
      let moduleInfo = {
        repository: gitInfo.repository,
        module: module.name,
        link: `${app.config.appRoot}/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${buildState.buildNumber}`
      };
      return moduleInfo;
    });

    return modules;
  }


  groupBuilds() {
    // builds grouped by repo
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
    }

    groupedInArray.forEach( (repo) => {
      // find most recent build
      repo.mostRecentBuild = repo.modules[0].buildState.startTime;
      repo.modules.forEach( (module) => {
        if (module.buildState.startTime < repo.mostRecentBuild) {
          repo.mostRecentBuild = module.buildState.startTime;
        }

      })
    })

    // sort by if building then by most recent build time
    function cmp(x, y) {
      return x > y ? 1 : x < y ? -1 : 0;
    }

    groupedInArray.sort( (a,b) => { ;
      return cmp(b.isBuilding, a.isBuilding) || cmp(a.mostRecentBuild, b.mostRecentBuild);
    })

    return groupedInArray;

  }


}

export default Builds;
