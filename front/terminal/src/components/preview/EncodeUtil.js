import jschardet from 'jschardet';
import iconv from 'iconv-lite';

export const detectEncoding = (content) => {
  return jschardet.detect(content).encoding;
};

export const encodeStrToArray = (text, encoding) => {
  return iconv.encode(text, encoding);
};

export const decodeArrayToStr = (buffer, decoding) => {
  return iconv.decode(buffer, decoding);
};