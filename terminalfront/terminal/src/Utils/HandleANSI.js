import ansiparser from 'ansi-parser';
import { AnsiUp } from 'ansi-up';

let ansi_up = new AnsiUp();

const parseShellToHtml = (stream) => {
  // \x1B[0m\x1B[01;34muuu\x1B[0m\r
  // \u001b[0m\u001b[01;34muuu\u001b[0m\r
  return ansi_up.ansi_to_html(stream)
}

import {
  regex,
} from 'cross-platform-terminal-characters'


// 处理Unicode字符
export const handleUnicode = (stream) => {
  return stream.replace(regex, '').replace(/\r?\\u001b\[[0-9;]*[A-Za-z]/g, '');
}

// 处理ANSI字符
export const handleANSI = (stream) => {
  stream = handleUnicode(stream);
  let parseText = ansiparser.removeAnsi(stream);
  if(parseText == stream) {
    return {
      isHtml:false,
      content:stream,
      origin:stream,
    }
  }
  else {
    return {
      isHtml:true,
      content:parseShellToHtml(stream),
      origin:stream,
    }
  }
}