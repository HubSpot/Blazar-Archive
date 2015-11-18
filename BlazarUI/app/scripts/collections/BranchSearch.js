/*global config*/
import Collection from './Collection';

class BranchSearch extends Collection {

  url() {
    let url = `${config.apiRoot}/branch/search?property=id`;

    this.options.params.forEach((param) => {
      url+= `&${param.property}=${param.value}`;
    });

    return url;
  
  }
}

export default BranchSearch;
