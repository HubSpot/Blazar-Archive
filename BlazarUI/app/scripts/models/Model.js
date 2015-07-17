import moment from 'moment';
import $ from 'jQuery';

class Model {

  constructor() {}

  parse() {}

  fetch() {
    this.data = {};

    let promise = $.ajax({
      url: this.url(),
      type: 'GET',
      dataType: 'json'
    });

    promise.done( (resp) => {
      this.data.data = resp;
      this.parse();
    })

    return promise;

  }

  addTimeHelpers() {
    let buildState = this.data.data.buildState;

    if (buildState.startTime && buildState.endTime) {
      buildState.duration = moment.duration(buildState.endTime - buildState.startTime).humanize();
    }


  }

}

export default Model;
