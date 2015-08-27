module.exports = function(a, b) {
  let ret = [];
  let i = b ? a : 0;
  let len = b ? b : a;

  for (; i < len; i++) {
    ret.push(i);
  }

  return ret;
};
