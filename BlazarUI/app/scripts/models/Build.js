/*global config*/
import Model from './Model';
import {findWhere} from 'underscore'; 

class Build extends Model {
  
  parse() {
    this.data = findWhere(this.raw, {id: parseInt(this.options.buildId)});
  }
  
  url() {
    const {repoBuildId} = this.options;
    return `${config.apiRoot}/branches/builds/${repoBuildId}/modules`;
  }
}

export default Build;
