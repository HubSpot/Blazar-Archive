import Reflux from 'reflux';

const LocationsStore = Reflux.createStore({

  init() {
    this.pathname = window.location.pathname;
  }


});

export default LocationsStore;
