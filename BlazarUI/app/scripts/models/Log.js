/*global config*/
import Model from './Model';

class Log extends Model {

  url() {
    return `${config.apiRoot}/build/${this.options.buildNumber}/log?offset=${this.options.offset}`;
  }

  formatLog(jqxhr) {

    if (jqxhr.status !== 200) {
      console.warn(jqxhr);
      return "<p class='roomy-xy'>Error loading build log. Please check your console for more detail.</p>";
    }

    const NEW_LINE = '\n';
    return jqxhr.responseJSON.data.split(NEW_LINE).map((line) => {
      if (line.length === 0) {
        return null;
      }
      return `<p class='log-line'>${line}</p>`;
    }).join('');

  }

}

export default Log;
