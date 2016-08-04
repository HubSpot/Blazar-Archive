import Model from './Model';

class BranchDefinition extends Model {

  url() {
    return `${window.config.apiRoot}/branch/lookup?host=${this.options.host}&organization=${this.options.org}&repository=${this.options.repo}&branch=${this.options.branch}`;
  }
}

export default BranchDefinition;
