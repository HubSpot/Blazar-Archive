import Reflux from 'reflux';

const ConfigStore = Reflux.createStore({

  init() {
    this.appRoot = window.config.appRoot;
  }


});

export default ConfigStore;
