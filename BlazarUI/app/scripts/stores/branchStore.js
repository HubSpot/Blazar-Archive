import Reflux from 'reflux';
import BranchActions from '../actions/branchActions';


const BranchStore = Reflux.createStore({

  listenables: BranchActions,
  
  init() {
    this.modules = [];
  },

  loadModulesSuccess(incomingData) {
    this.modules = incomingData;

    this.trigger({
      modules: this.modules,
      loading: false
    });
  }

});

export default BranchStore;
