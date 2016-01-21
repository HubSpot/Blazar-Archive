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

  put() {
    return $.ajax(Object.assign(this.defaultAjaxProps(), {type: 'PUT', data: this.data}));
  }
  
  post() {
    return $.ajax(Object.assign(this.defaultAjaxProps(), {type: 'POST', data: this.data}));
  }
  
  delete() {
    return $.ajax(Object.assign(this.defaultAjaxProps(), {type: 'DELETE', data: this.data}));
  }
  
}

export default ResourceProvider;
