/*global config*/
import Model from './Model';

class BranchDefinition extends Model {

  constructor(gitInfo) {
    super();
    this.gitInfo = gitInfo;
  }

  url() {
    const gitInfo = this.gitInfo;
    return `${config.apiRoot}/branch/lookup?host=${gitInfo.host}&organization=${gitInfo.org}&repository=${gitInfo.repo}&branch=${gitInfo.branch}`;
  }
}

export default BranchDefinition;
