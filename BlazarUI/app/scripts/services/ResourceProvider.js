//
// Generic Ajax Resource Provider
//
import { fromJS } from 'immutable';
import $ from 'jquery';
import {extend} from 'underscore';

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
    return $.ajax(extend(this.defaultSettings(), this.settings));
  }

}

export default ResourceProvider;
