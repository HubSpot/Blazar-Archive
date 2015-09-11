/*global config*/
import Model from './Model';

class Log extends Model {

  constructor(buildNumber) {

    this.buildNumber = buildNumber;

    this.fetchOptions = {
      dataType: 'json'
    };
  }

  url() {
    return `${config.apiRoot}/build/${this.buildNumber}/log/`;
  }

  formatLog(jqxhr) {
    let cache = [];

    if (jqxhr.status !== 200) {
      console.warn(jqxhr);
      return "<p class='x-y-roomy'>Error loading build log. Please check your console for more detail.</p>";
    }

    const lines = jqxhr.responseJSON.data.split('\n');

    lines.forEach( (line) => {
      cache.push(`<p class='log-line'>${line}</p>`);
    });

    return cache.join('\n');
  }


}

export default Log;
