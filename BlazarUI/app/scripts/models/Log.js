import Model from './Model';

class Log extends Model {
  constructor(logFileUrl){
    this.logFileUrl = logFileUrl + "&offset=0&length=1000000";
    this.fetchOptions = {
      dataType: 'json'
    }
  }

  url() {
    return this.logFileUrl;
  }
}

export default Log;
