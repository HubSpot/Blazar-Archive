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
        item.build.duration = humanizeDuration(item.build.endTimestamp - item.build.startTimestamp, {round: true});
        item.build.blazarPath = `/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${encodeURIComponent(gitInfo.branch)}/${module.name}/${item.build.buildNumber}`;
      }

      if (has(item, 'module')) {
        item.module.blazarPath = {
          module: `/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${encodeURIComponent(gitInfo.branch)}/${module.name}`,
          branch: `/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${encodeURIComponent(gitInfo.branch)}`,
          repo: `/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}`
        };
      }

      if (has(item, 'inProgressBuild')) {
        item.inProgressBuild.blazarPath = `/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${encodeURIComponent(gitInfo.branch)}/${module.name}/${inProgressBuild.buildNumber}`;
        item.inProgressBuild.duration = humanizeDuration(Date.now() - item.inProgressBuild.startTimestamp, {round: true});
      }

      if (has(item, 'lastBuild')) {
        item.lastBuild.duration = humanizeDuration(item.lastBuild.endTimestamp - item.lastBuild.startTimestamp, {round: true});
        item.lastBuild.blazarPath = `/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${encodeURIComponent(gitInfo.branch)}/${module.name}/${lastBuild.buildNumber}`;
      }

      return item;
    });
  }


}

export default BaseCollection;
