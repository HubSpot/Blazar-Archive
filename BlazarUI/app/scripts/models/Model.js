import humanizeDuration from 'humanize-duration';
import $ from 'jquery';



class Model {

  constructor() {
    this.fetchOptions = {};
    this.fetch = this.fetch.bind(this);
  }

  parse() {}

  fetch() {
    this.data = {};
    const promise = $.ajax({
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
    const build = this.data.build;

    if (build.startTimestamp && build.endTimestamp) {
      build.duration = humanizeDuration(build.endTimestamp - build.startTimestamp);
    }
  }

}

export default Model;
