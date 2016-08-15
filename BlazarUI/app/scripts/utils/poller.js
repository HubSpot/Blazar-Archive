class Poller {

  constructor(options) {
    this.collection = options.collection;
    this.interval = options.interval;
    this.dontPoll = false;
  }

  startPolling(cb) {
    const that = this;
    (function _doPoll() {
      if (that.dontPoll) {
        return;
      }
      that._fetchBuilds((resp) => {
        setTimeout(_doPoll, window.config.buildsRefresh);
        if (cb) {
          cb(resp);
        }
      });
    })();
  }

  stopPolling() {
    this.dontPoll = true;
  }

  _fetchBuilds(cb) {
    const promise = this.collection.fetch();
    let result;

    promise.done((data, textStatus, jqXHR) => {
      result = {
        success: true,
        data,
        textStatus,
        jqXHR
      };
    });

    promise.always(() => {
      if (typeof cb === 'function') {
        cb(result);
      }
    });
  }


}


export default Poller;
