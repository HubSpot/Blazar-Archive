import Model from './Model';

class LogSize extends Model {

  url() {
    return `${window.config.apiRoot}/modules/builds/${this.options.buildId}/log/size`;
  }

}

export default LogSize;
