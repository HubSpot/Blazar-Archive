export const getByteLength = (normalVal) => {
  // Force string type
  normalVal = String(normalVal);

  let byteLen = 0;
  for (let i = 0; i < normalVal.length; i++) {
    const c = normalVal.charCodeAt(i);

    if (c < (1 << 7)) {
      byteLen += 1;
    } else if (c < (1 << 11)) {
      byteLen += 2;
    } else if (c < (1 << 16)) {
      byteLen += 3;
    } else if (c < (1 << 21)) {
      byteLen += 4;
    } else if (c < (1 << 26)) {
      byteLen += 5;
    } else if (c < (1 << 31)) {
      byteLen += 6;
    } else {
      byteLen += Number.NaN;
    }
  }

  return byteLen;
};
