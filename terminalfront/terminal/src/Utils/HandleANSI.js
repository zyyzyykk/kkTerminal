import ansiparser from 'ansi-parser';
import { AnsiUp } from 'ansi-up';

let ansi_up = new AnsiUp();

const parseShellToHtml = (stream) => {
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