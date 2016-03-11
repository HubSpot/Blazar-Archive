import humanizeDuration from 'humanize-duration';
import {extend} from 'underscore';
import $ from 'jquery';

class Model {

  constructor(options) {    
    const defaultOptions = {
      parse: true
    };

    this.options = extend(defaultOptions, options);
    this.fetch = this.fetch.bind(this);
  }

  parse() {}

  fetch() {    
    const promise = $.ajax({
      url: this.options.url || this.url(),
      type: this.options.type || 'GET',
      dataType: this.options.dataType || 'json'
    });

    promise.done((raw, textStatus, jqXHR) => {
      this.raw = raw;
      this.textStatus = textStatus;
      this.jqXHR = jqXHR;

      if (!this.options.parse) {
        return;
      }
      this.parse();
    });

    return promise;

  }

  addTimeHelpers() {
    const build = this.data.build;

    if (build.startTimestamp && build.endTimestamp) {
      build.duration = humanizeDuration(build.endTimestamp - build.startTimestamp, {round: true, units: ['h', 'm', 's']});
    }
  }

}

export default Model;
