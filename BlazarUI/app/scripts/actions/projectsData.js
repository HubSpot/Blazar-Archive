import _ from 'underscore';


var projectsData = {

  // Ready data for the sidebar
  manageResponse: function(data, cb) {
    // list of module names
    let modules = [];
    _.filter(data, function(item){
      let module = { value: item.module.name, label: `${item.gitInfo.repository} » ${item.module.name}` };
      modules.push(module);
    });
    // jobs grouped by repo
    let grouped = _(data).groupBy(function(o) {
      return o.gitInfo.repository;
    });
    // determine if any of the repos have a module that is building
    var buildingRepos = [];
    let modulesGrouped = _.each(grouped, function(repo){
      repo.moduleIsBuilding = false;
      for (var value of repo) {
        if(value.buildState.result === 'IN_PROGRESS'){
          repo.moduleIsBuilding = true;
          repo.repository = value.repository;
          buildingRepos.push(repo);
          break;
        }
      }
      return repo;
    })

    data = {
      all: data,
      buildingRepos: buildingRepos,
      modules: modules
    };

    cb(data);
  }

}

export default projectsData;
