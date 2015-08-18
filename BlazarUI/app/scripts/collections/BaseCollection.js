import {has} from 'underscore';
import moment from 'moment';
import Collection from './Collection';

class BaseCollection extends Collection{

  addTimeHelpers() {
    this.data.forEach( (item) => {
      if (has(item, 'lastBuild')) {
        item.lastBuild.duration = moment.duration(item.lastBuild.endTimestamp - item.lastBuild.startTimestamp).humanize();
      }
    });
  }


}

export default BaseCollection;
