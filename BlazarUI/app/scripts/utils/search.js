import fuzzy from 'fuzzy';

export default class Search {

  constructor(options) {
    this.records = options.records;
  }

  getOptions() {
    return {
      extract: function(el) {
        return `${el.gitInfo.repository} ${el.gitInfo.branch}`;
      }
    };
  }

  match(term) {
    const results = fuzzy.filter(term, this.records, this.getOptions());
    return results.map(function(el) {
      return el.original;
    });
  }

}
