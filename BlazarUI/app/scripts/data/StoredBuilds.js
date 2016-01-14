//
// Super class for polling the builds store
//  - extended by BranchesApi abnd BranchBuildsApi
//
import {isEmpty} from 'underscore';
import BuildsStore from '../stores/buildsStore';

class StoredBuilds {
  
  constructor(options) {
    this.options = options;
    this.shouldPoll = true;
  }

  getInitialBuilds() {
    this.builds = BuildsStore.builds.all;

    if (!this.builds.size) {
      return;
    }
    this._parse();
  }

  // used by subclasses
  _parse(resp) {

  }

  _onStoreChange(resp) {
    this.builds = resp.builds.all;
    this._parse();
  }

  //
  // Public
  //
  fetchBuilds(cb) {
    this.cb = cb;
    this.getInitialBuilds();
    this._unsubscribeFromBuilds = BuildsStore.listen(this._onStoreChange.bind(this));
  }
  
  stopPollingBuilds() {
    this._unsubscribeFromBuilds();
  }
  
}

export default StoredBuilds;
