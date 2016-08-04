/* global config*/
import Reflux from 'reflux';
import BuildActions from '../actions/buildActions';
import BuildApi from '../data/BuildApi';

const BuildStore = Reflux.createStore({

  listenables: BuildActions,

  init() {
    this.data = null;
    this.error = false;
  },

  updateData(err, resp) {
    this.data = resp;
    this.error = err;

    if (err) {
      this.triggerError();
    }
    else {
      this.triggerSuccess();
    }
  },

  triggerSuccess() {
    this.trigger({
      data: this.data,
      loading: false
    });
  },

  triggerError() {
    let error;

    // custom error message
    if (typeof(this.error) === 'string') {
      error = this.error;
    }
    // send the xhr message
    else {
      error = {
        status: this.error.status,
        statusText: this.error.statusText
      };
    }

    this.trigger({
      loading: false,
      error
    });
  },

  onLoadBuild(params) {
    this.api = new BuildApi(params);

    this.api.loadBuild((err, resp) => {
      this.updateData(err, resp);
    });
  },

  onResetBuild() {
    this.api.setLogPollingState(false);
    this.api = undefined;
  },

  onFetchNext() {
    this.api.fetchNext((err, resp) => {
      this.updateData(err, resp);
    });
  },

  onFetchPrevious() {
    this.api.fetchPrevious((err, resp) => {
      this.updateData(err, resp);
    });
  },

  onNavigationChange(position) {
    this.api.navigationChange(position, (err, resp) => {
      this.updateData(err, resp);
    });
  },

  setLogPollingState(state) {
    this.api.navigationChange(state, (err, resp) => {
      this.updateData(err, resp);
    });
  }

});

export default BuildStore;
