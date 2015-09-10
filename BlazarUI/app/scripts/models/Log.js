/*global config*/
import Model from './Model';

class Log extends Model {

  constructor(buildNumber) {

    const params = '';
    this.logUrl = `${config.apiRoot}/build/${buildNumber}/log/${params}`;

    this.fetchOptions = {
      dataType: 'json'
    };
  }

  url() {
    return this.logUrl;
  }
}

export default Log;
