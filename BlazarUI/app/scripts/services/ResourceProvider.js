//
// Generic Ajax Resource Provider
//
import { fromJS } from 'immutable';
import $ from 'jquery';

class ResourceProvider {
  
  constructor({url, dataType = 'json', data={}}) {
    this.url = url;
    this.data = data;
    this.dataType = dataType;
  }

  defaultAjaxProps() {
    return {
      url: this.url,
      dataType: this.dataType
    };
  }

  get() {
    return $.ajax(Object.assign(this.defaultAjaxProps(), {type: 'GET'}));
  }

  post() {
    return $.ajax(Object.assign(this.defaultAjaxProps(), {type: 'POST'}));
  }
  
  delete() {
    
  }
  
}

export default ResourceProvider;
