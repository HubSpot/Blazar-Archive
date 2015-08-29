import moment from 'moment';
import $ from 'jquery';

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

    promise.done((resp) => {
      this.data = resp;
      this.parse();
    });

    return promise;

  }

  addTimeHelpers() {
    let build = this.data.build;

    if (build.startTimestamp && build.endTimestamp) {
      build.duration = moment.duration(build.endTimestamp - build.startTimestamp).humanize();
    }
  }

}

export default Model;
