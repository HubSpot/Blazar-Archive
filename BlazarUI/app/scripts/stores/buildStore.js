import Reflux from 'reflux';
import BuildActions from '../actions/buildActions';

const BuildStore = Reflux.createStore({

  init() {
    this.build = {};

    this.buildTriggeringDone = true;
    this.buildTriggeringError = '';

    this.listenTo(BuildActions.loadBuild, this.loadBuild);
    this.listenTo(BuildActions.loadBuildSuccess, this.loadBuildSuccess);
    this.listenTo(BuildActions.loadBuildError, this.loadBuildError);

    this.listenTo(BuildActions.triggerBuildSuccess, this.triggerBuildSuccess);
    this.listenTo(BuildActions.triggerBuildError, this.triggerBuildError);
    this.listenTo(BuildActions.triggerBuildStart, this.triggerBuildStart);
  },

  loadBuild() {
    this.trigger({
      loading: true
    });
  },

  loadBuildSuccess(build) {
    this.build = build;

    this.trigger({
      build: this.build,
      loading: false
    });
  },

  loadBuildError(error) {
    this.trigger({
      error: error,
      loading: false
    });
  },

  triggerBuildStart() {
    this.buildTriggeringDone = false;

    this.trigger({
      buildTriggeringDone: false
    });
  },

  triggerBuildSuccess() {
    this.buildTriggeringDone = true;

    this.trigger({
      buildTriggeringDone: true
    });
  },

  triggerBuildError(error) {
    this.buildTriggeringDone = true;
    this.buildTriggeringError = error;

    this.trigger({
      buildTriggeringDone: true,
      buildTriggeringError: error
    });
  }

});

export default BuildStore;
