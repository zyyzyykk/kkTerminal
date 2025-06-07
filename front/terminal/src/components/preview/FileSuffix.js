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
    if(index === -1) return false;
    const fileSuffix = item.substring(index + 1).toLowerCase();
    if(zipSuffixs[fileSuffix]) return true;
    return false;
};

const iframeSuffixs = {
    "pdf": "application/pdf",
    "html": "text/html",
    "jpg": "image/jpeg",
    "jpeg": "image/jpeg",
    "png": "image/png",
    "gif": "image/gif",
    "bmp": "image/bmp",
    "webp": "image/webp",
    "svg": "image/svg+xml",
    "ico": "image/vnd.microsoft.icon",
};

const audioSuffixs = {
    "mp3": "audio/mpeg",
    "wav": "audio/wav",
    "ogg": "audio/ogg",
    "aac": "audio/aac",
    "m4a": "audio/mp4"
};

const videoSuffixs = {
    "mp4": "video/mp4",
    "webm": "video/webm",
    "ogv": "video/ogg",
    "avi": "video/x-msvideo",
    "mov": "video/quicktime"
};

export const previewFileInfo = (item) => {
    const previewInfo = {
        preview:'editor',
        type:'text',
    };
    const index = item.lastIndexOf('.');
    if(index === -1) return previewInfo;
    const fileSuffix = item.substring(index + 1).toLowerCase();
    if(iframeSuffixs[fileSuffix]) {
        previewInfo.preview = "iframe";
        previewInfo.type = iframeSuffixs[fileSuffix];
    }
    else if(audioSuffixs[fileSuffix]) {
        previewInfo.preview = "audio";
        previewInfo.type = audioSuffixs[fileSuffix];
    }
    else if(videoSuffixs[fileSuffix]) {
        previewInfo.preview = "video";
        previewInfo.type = videoSuffixs[fileSuffix];
    }
    return previewInfo;
};
