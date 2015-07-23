import Model from './Model';

class Log extends Model {
  constructor(logFileUrl){
    this.logFileUrl = logFileUrl;
    this.fetchOptions = {
      dataType: 'text'
    }
  }

  url() {
    return this.logFileUrl;
  }
}

export default Log;
