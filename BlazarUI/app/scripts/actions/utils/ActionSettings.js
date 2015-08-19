import Reflux from 'reflux';
import $ from 'jquery';

class ActionSettings {

  constructor() {
    this.polling = true;
  }

  setPolling(status) {
    this.polling = status;
  }

}

export default ActionSettings;
