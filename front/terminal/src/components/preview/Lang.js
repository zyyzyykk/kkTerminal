const langs = {
    // common file types
    "c": "c_cpp",
    "cc": "c_cpp",
    "cxx": "c_cpp",
    "h": "c_cpp",
    "hh": "c_cpp",
    "cpp": "c_cpp",
    "hpp": "c_cpp",
    "ino": "c_cpp",
    "cs": "csharp",
    "css": "css",
    "gitignore": "gitignore",
    "go": "golang",
    "html": "html",
    "htm": "html",
    "ini": "ini",
    "conf": "ini",
    "cfg": "ini",
    "java": "java",
    "js": "javascript",
    "jsm": "javascript",
    "cjs": "javascript",
    "mjs": "javascript",
    "jsx": "javascript",
    "vue": "javascript",
    "we": "javascript",
    "wpy": "javascript",
    "json": "json",
    "jsp": "jsp",
    "md": "markdown",
    "markdown": "markdown",
    "properties": "properties",
    "py": "python",
    "sh": "sh",
    "bash": "sh",
    "sql": "sql",
    "cts": "typescript",
    "mts": "typescript",
    "ts": "typescript",
    "tsx": "typescript",
    "xml": "xml",
    "rdf": "xml",
    "rss": "xml",
    "wsdl": "xml",
    "xsl": "xml",
    "xslt": "xml",
    "atom": "xml",
    "mathml": "xml",
    "mml": "xml",
    "xul": "xml",
    "xbl": "xml",
    "xaml": "xml",
    "xsd": "xml",
    "xhtml": "xml",
    "yaml": "yaml",
    "yml": "yaml",
    // uncommon file types
    "ipynb": "json",
    "bash_history": "sh",
    "bash_logout": "sh",
    "bash_profile": "sh",
    "tcshrc": "sh",
    "bashrc": "sh",
    "cshrc": "sh",
    "vcxproj": "xml",
};

// 获取文件名后缀
const getSuffix = (name) => {
    let suffix = 'txt';
    if(name && name.length > 0) {
      const index = name.lastIndexOf('.');
      if(index !== -1) suffix = name.substring(index + 1).toLowerCase();
    }
    return suffix;
};

// 文件名 => 语言
const langToMode = (name) => {
    return langs[getSuffix(name)] || "text";
};

export default langToMode;
