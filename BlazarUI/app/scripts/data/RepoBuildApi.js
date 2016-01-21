import RepoBuildPollingProvider from '../services/RepoBuildPollingProvider';

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
  
  cancelBuild() {
    // console.log('cancel: ', this.branchId);
  }

}

export default RepoBuildApi;
