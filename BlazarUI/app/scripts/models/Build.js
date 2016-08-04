import Model from './Model';
import {findWhere} from 'underscore';

class Build extends Model {

  parse() {
    this.data = findWhere(this.raw, {id: parseInt(this.options.id, 10)});
  }

  url() {
    return `${window.config.apiRoot}/branches/builds/${this.options.repoBuildId}/modules`;
  }
}

export default Build;
