export const getStarredModules = (stars, modules) => {
  const starredModules = [];

  modules.forEach( (module) => {
    stars.forEach( (star) => {
      if (star.moduleId === module.module.id) {
        starredModules.push(module);
        return;
      }
    });
  });

  return starredModules;
};

export const markStarredModules = (modules, stars) => {
  modules.map( (module) => {
    module.module.isStarred = false;
    stars.forEach( (star) => {
      if (star.moduleId === module.module.id) {
        module.module.isStarred = true;
        return;
      }
    });
  });

  return modules;
};
