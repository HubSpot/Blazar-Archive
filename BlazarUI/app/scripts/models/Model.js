import moment from 'moment';
import $ from 'jQuery';

class Model {

  constructor() {
    this.fetchOptions = {};
    this.fetch = this.fetch.bind(this);
  }

  parse() {}

  fetch() {
    this.data = {};
    let promise = $.ajax({
      url: this.fetchOptions.url || this.url(),
      type: this.fetchOptions.type || 'GET',
      dataType: this.fetchOptions.dataType || 'json'
    });

    promise.done( (resp) => {
      this.data = resp;
      this.parse();
    })

    return promise;

  }

  addTimeHelpers() {
    let buildState = this.data.buildState;

    if (buildState.startTime && buildState.endTime) {
      buildState.duration = moment.duration(buildState.endTime - buildState.startTime).humanize();
    }


  }

}

export default Model;
