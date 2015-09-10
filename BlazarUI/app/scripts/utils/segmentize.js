const intersect = require('intersect');
const uniq = require('uniq');
const range = require('./range');

module.exports = function(o) {
  const page = o.page;
  const pages = o.pages;
  let beginPages = o.beginPages ? range(Math.min(o.beginPages, pages)) : [];
  let endPages = o.endPages ? range(Math.max(pages - o.endPages, 0), pages) : [];
  let center, ret;

  if (beginPages.length + endPages.length >= pages) {
    return [range(pages)];
  }

  if (page === 0) {
    ret = [[0]];

    if (pages > 1) {
      if (!beginPages.length) {
        beginPages = [0, 1];
      }

      ret = [beginPages, difference(endPages, beginPages)].filter((a) => a.length);
    }

    return ret;
  }

  if (page === pages - 1) {
    endPages = [pages - 2, pages - 1];

    return [beginPages, difference(endPages, beginPages)].filter((a) => a.length);
  }

  center = [page - 1, page, page + 1];

  if (intersect(beginPages, center).length) {
    beginPages = uniq(beginPages.concat(center)).sort(function(a, b) {
      return a > b;
    });
    center = [];
  }

  if (intersect(center, endPages).length) {
    endPages = uniq(center.concat(endPages)).sort(function(a, b) {
      return a > b;
    });
    center = [];
  }

  if (!center.length && beginPages.length === endPages.length && beginPages.every((page, i) => page === endPages[i])) {
      return [beginPages];
    }

    return [beginPages, center, endPages].filter((a) => a.length);
  };

  function difference(a, b) {
    return a.filter((v) => b.indexOf(v) < 0);
  }
