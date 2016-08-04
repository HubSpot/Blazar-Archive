module.exports = (a, b) => {
  const ret = [];
  let i = b ? a : 0;
  const len = b || a;

  for (; i < len; i++) {
    ret.push(i);
  }

  return ret;
};
