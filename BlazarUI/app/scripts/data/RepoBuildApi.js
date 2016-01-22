/*global config*/
import RepoBuildPollingProvider from '../services/RepoBuildPollingProvider';
import Resource from '../services/ResourceProvider';

class RepoBuildApi {

  constructor(params) {
    this.buildsPoller = new RepoBuildPollingProvider(params);
  }

  startPolling(cb) {
    this.cb = cb;

    this.buildsPoller.poll((err, resp) => {
      if (err) {
        return cb(err);
      }

      this.branchId = resp.branchId;
      cb(err, resp);
    });
  }

  stopPolling() {
    if (!this.buildsPoller) {
      return;
    }

    this.buildsPoller.disconnect();
  }
  
  cancelBuild(cb) {
    const cancelPromise = new Resource({
      url: `${config.apiRoot}/branches/builds/${this.buildsPoller.repoBuildId}/cancel`,
      type: 'POST'
    }).send();

    cancelPromise.error((error) => {
      console.warn(error);
      cb(`Error cancelling build. See your console for more detail.`);
    });
       
  }

}

export default RepoBuildApi;
