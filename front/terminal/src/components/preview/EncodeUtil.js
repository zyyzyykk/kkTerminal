import iconv from 'iconv-lite';

export const encodeStrToArray = (text, encoding) => {
  return iconv.encode(text, encoding);
};

export const decodeArrayToStr = (buffer, decoding) => {
  return iconv.decode(buffer, decoding);
};