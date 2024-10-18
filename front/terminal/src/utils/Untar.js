const zipSuffixs = {
    "tar": "tar",
    "tbz2": "tbz2",
    "gz": "gz",
    "xz": "xz",
    "bz2": "bz2",
    "zip": "zip",
};

export const isZipFile = (item) => {
    const index = item.lastIndexOf('.');
    if(index == -1) return false;
    const fileSuffix = item.substring(index + 1);
    if(zipSuffixs[fileSuffix]) return true;
    return false;
};