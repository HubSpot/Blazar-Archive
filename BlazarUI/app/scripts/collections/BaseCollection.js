import moment from 'moment';
import Collection from './Collection';

class BaseCollection extends Collection{

  addTimeHelpers() {
    this.data.forEach( (item) => {
      if (typeof item.buildState === "undefined") {

      }
      else if (item.buildState.startTime && item.buildState.endTime) {
        item.buildState.duration = moment.duration(item.buildState.endTime - item.buildState.startTime).humanize();
      }
    });
  }


}

export default BaseCollection;
