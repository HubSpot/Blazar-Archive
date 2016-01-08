/*global config*/
import $ from 'jquery';
import RepoBuildPollingProvider from '../services/RepoBuildPollingProvider';

class RepoBuildApi {

  constructor(params) {
    this.buildsPoller = new RepoBuildPollingProvider(params);
  }

  startPolling(cb) {
    this.cb = cb;

    this.buildsPoller.poll((err, resp) => {
      if (err) {
        cb(err);
        return;
      }

      cb(err, resp);
    });
  }

  stopPolling() {
    if (!this.buildsPoller) {
      return;
    }

    this.buildsPoller.disconnect();
  }

}

export default RepoBuildApi;
