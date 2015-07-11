import _ from 'underscore';


var projectsData = {

  // Ready data for sidebar
  manageResponse: function(data, cb){
    // jobs grouped by repo
    let grouped = _.groupBy(data, 'repository');
    // determine if any of the repos have a module that is building
    var buildingRepos = []
    let modulesGrouped = _.each(grouped, function(repo){
      repo.moduleIsBuilding = false
      for (var value of repo) {
        if(value.result === 'IN_PROGRESS'){
          repo.moduleIsBuilding = true;
          repo.repository = value.repository
          buildingRepos.push(repo)
          break
        }
      }
      return repo;
    })

    data = {
      all: data,
      buildingRepos: buildingRepos,
    }

    cb(data);
  }


}


export default projectsData;