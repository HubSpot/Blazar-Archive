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
    return this;
  }

  getInitialBuilds() {
    this.builds = BuildsStore.builds.all;

    if (!this.builds.size) {
      return;
    }
    
    this._afterInitialFetch();
    
  }
  
  _afterInitialFetch() {
    this._parse();  
  }

  // used by subclasses
  _parse(resp) {

  }

  _onStoreChange(resp) {
    this.builds = resp.builds.all;
    this._afterInitialFetch();
  }

  //
  // Public
  //
  fetchBuilds(cb) {
    this._unsubscribeFromBuilds = BuildsStore.listen(this._onStoreChange.bind(this));
    this.cb = cb;
    this.getInitialBuilds();  
  }
  
  stopPollingBuilds() {
    this.shouldPoll = false;
    this._unsubscribeFromBuilds();
  }
  
}

export default StoredBuilds;
