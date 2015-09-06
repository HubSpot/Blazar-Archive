import {map,has} from 'underscore';
import moment from 'moment';
import Collection from './Collection';

class BaseCollection extends Collection{

  parse() {
    this.addHelpers();
  }

  limit(limit) {
    return this.data.splice(0, limit)
  }


  get() {
    return this.data;
  }

  addHelpers() {

    return map(this.data, (item) => {

      let {
        gitInfo,
        module,
        lastBuild,
        inProgressBuild
      } = item;

      item.module.blazarPath = {
        module: `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}`,
        branch: `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}`,
        repo: `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}`
      };

      if (has(item, 'inProgressBuild')) {
        item.inProgressBuild.blazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${inProgressBuild.buildNumber}`;
      }

      if (has(item, 'lastBuild')) {
        item.lastBuild.duration = moment.duration(item.lastBuild.endTimestamp - item.lastBuild.startTimestamp).humanize();
        item.lastBuild.blazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${lastBuild.buildNumber}`;
      }

      return item;

    });

  }


}

export default BaseCollection;
