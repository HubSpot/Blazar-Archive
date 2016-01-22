//
// Generic Ajax Resource Provider
//
import { fromJS } from 'immutable';
import $ from 'jquery';

class ResourceProvider {
  
  constructor(settings) {
    this.settings = settings;
    return this;
  }

  defaultSettings() {
    return {
      dataType: 'json'
    };
  }

  send() {
    return $.ajax(Object.assign(this.defaultSettings(), this.settings));
  }

}

export default ResourceProvider;
