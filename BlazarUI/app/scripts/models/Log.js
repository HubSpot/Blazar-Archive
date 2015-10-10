/*global config*/
import Model from './Model';

class Log extends Model {

  constructor(options) {
    this.offset = options.offset;
    this.buildNumber = options.buildNumber;

    this.fetchOptions = {
      dataType: 'json'
    };

  }

  url() {
    return `${config.apiRoot}/build/${this.buildNumber}/log?offset=${this.offset}`;
  }

  formatLog(jqxhr) {

    if (jqxhr.status !== 200) {
      console.warn(jqxhr);
      return "<p class='roomy-xy'>Error loading build log. Please check your console for more detail.</p>";
    }

    const NEW_LINE = '\n';
    return jqxhr.responseJSON.data.split(NEW_LINE).map((line) => {
      if (line.length === 0){
        return;
      }
      return `<p class='log-line'>${line}</p>`;
    }).join('');

  }


}

export default Log;
