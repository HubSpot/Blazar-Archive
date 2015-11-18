export const getByteLength = function(normal_val) {
  // Force string type
  normal_val = String(normal_val);

  let byteLen = 0;
  for (let i = 0; i < normal_val.length; i++) {
    const c = normal_val.charCodeAt(i);
    byteLen += c < (1 <<  7) ? 1 :
               c < (1 << 11) ? 2 :
               c < (1 << 16) ? 3 :
               c < (1 << 21) ? 4 :
               c < (1 << 26) ? 5 :
               c < (1 << 31) ? 6 : Number.NaN;
  }
  return byteLen;
  
};
