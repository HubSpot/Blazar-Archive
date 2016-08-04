import fuzzy from 'fuzzy';
import { uniq } from 'underscore';

export default class Search {

  constructor(options) {
    this.records = options.records;
  }

  getRepoOptions() {
    return {
      extract(el) {
        return `${el.gitInfo.repository}`;
      }
    };
  }

  getBranchOptions() {
    return {
      extract(el) {
        return `${el.gitInfo.branch}`;
      }
    };
  }

  match(term) {
    const repoResults = fuzzy.filter(term, this.records, this.getRepoOptions())
      .map((el) => {
        return el.original;
      });
    const branchResults = fuzzy.filter(term, this.records, this.getBranchOptions())
      .map((el) => {
        return el.original;
      });

    return uniq([...repoResults, ...branchResults]);
  }

}
