/*global config*/
import {map, has} from 'underscore';
import Collection from './Collection';
import humanizeDuration from 'humanize-duration';

class BaseCollection extends Collection {

  parse() {
    this.addHelpers();
  }

  addHelpers() {

    return map(this.data, (item) => {

      const {
        gitInfo,
        module,
        lastBuild,
        inProgressBuild
      } = item;

      if (has(item, 'build')) {
        item.build.duration = humanizeDuration(item.build.endTimestamp - item.build.startTimestamp, {round: true, units: ['h', 'm', 's']});
        item.build.blazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${item.build.buildNumber}`.replace('#', '%23');
      }

      if (has(item, 'module')) {
        item.module.blazarPath = {
          module: `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}`.replace('#', '%23'),
          branch: `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}`.replace('#', '%23'),
          repo: `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}`
        };
      }

      if (has(item, 'inProgressBuild')) {
        item.inProgressBuild.blazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${inProgressBuild.buildNumber}`.replace('#', '%23');
        item.inProgressBuild.duration = humanizeDuration(Date.now() - item.inProgressBuild.startTimestamp, {round: true, units: ['h', 'm', 's']});
      }

      if (has(item, 'lastBuild')) {
        item.lastBuild.duration = humanizeDuration(item.lastBuild.endTimestamp - item.lastBuild.startTimestamp, {round: true, units: ['h', 'm', 's']});
        item.lastBuild.blazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${lastBuild.buildNumber}`.replace('#', '%23');
      }

      return item;

    });

  }


}

export default BaseCollection;
