const langs = {
    "c": "c_cpp",
    "h": "c_cpp",
    "cpp": "c_cpp",
    "hpp": "c_cpp",
    "cs": "csharp",
    "css": "css",
    "gitignore": "gitignore",
    "go": "golang",
    "html": "html",
    "htm": "html",
    "java": "java",
    "js": "javascript",
    "jsx": "javascript",
    "json": "json",
    "jsp": "jsp",
    "md": "markdown",
    "markdown": "markdown",
    "properties": "properties",
    "py": "python",
    "sh": "sh",
    "bash": "sh",
    "sql": "sql",
    "ts": "typescript",
    "tsx": "typescript",
    "vue": "javascript",
    "xml": "xml",
    "xsd": "xml",
    "xsl": "xml",
    "xslt": "xml",
    "yaml": "yaml",
    "yml": "yaml"
}

// 获取文件名后缀
const getSuffix = (name) => {
    let suffix = 'txt';
    if(name && name.length > 0) {
      let index = name.lastIndexOf('.');
      if(index != -1) suffix = name.substring(index + 1);
    }
    return suffix;
}

export default function langToMode(name) {
    return langs[getSuffix(name)] || "text";
}