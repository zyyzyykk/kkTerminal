import ansiparser from 'ansi-parser';
import { AnsiUp } from 'ansi-up';

let ansi_up = new AnsiUp();

const parseShellToHtml = (stream) => {
  // \x1B[0m\x1B[01;34muuu\x1B[0m\r
  // \u001b[0m\u001b[01;34muuu\u001b[0m\r
  return ansi_up.ansi_to_html(stream)
}

const handleANSI = (stream) => {
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

export default handleANSI