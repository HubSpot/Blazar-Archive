import moment from 'moment';
import Collection from './Collection';

class BaseCollection extends Collection{

  addTimeHelpers() {
    this.data.data.forEach( (item) => {
      if (item.buildState.startTime && item.buildState.endTime) {
        item.buildState.duration = moment.duration(item.buildState.endTime - item.buildState.startTime).humanize();
      }
    });
  }


}

export default BaseCollection;
