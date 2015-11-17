/*global config*/
import Model from './Model';

class LogSize extends Model {

  // to do: rename this buildId - buildNumber is something else
  url() {
    return `${config.apiRoot}/build/${this.options.buildNumber}/log/size`;
  }

}

export default LogSize;
