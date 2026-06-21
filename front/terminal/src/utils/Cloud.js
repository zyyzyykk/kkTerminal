import browser from "@/utils/Browser";
import { aesDecrypt, aesEncrypt } from "@/utils/Encrypt";
import { generateRandomString } from "@/utils/String";

import { request } from "@/utils/Request";
import { http_base_url } from "@/env/Base";
import { ElMessage } from "element-plus";
import i18n from "@/locales/i18n";
import { localStore, cloudStore } from "@/env/Store";

const getUserInfo = () => {
  if(!browser.localStorage.getItem(localStore['user'])) {
    browser.localStorage.setItem(localStore['user'], aesEncrypt(browser.crypto.randomUUID() + '@' + generateRandomString(16) + '@' + new Date().getTime()), false);
  }
  const userInfo = aesDecrypt(browser.localStorage.getItem(localStore['user'])).split('@');
  return {
    name: userInfo[0],
    key: userInfo[1],
    time: Number(userInfo[2]),
  }
};

// content: string
export const cloudUpload = async (type, name, content) => {
  if(!content) return;
  const userInfo = getUserInfo();
  // 创建Blob对象
  const blob = new Blob([aesEncrypt(content, userInfo.key)], {type: 'application/octet-stream'});
  // 创建File对象
  const file = new File([blob], name);
  const formData = new FormData();
  formData.append('user', userInfo.name + '-' + userInfo.time);
  formData.append('type', type);
  formData.append('name', name);
  formData.append('file', file);
  return new Promise((resolve, reject) => {
    request({
      url: http_base_url + '/cloud/upload',
      type: 'post',
      data: formData,
      contentType: false,
      processData: false,
      success(resp) {
        if(resp.status !== 'success') {
          if(resp.code === 506) {
            ElMessage({
              message: i18n.global.t('云端文件过多'),
              type: resp.status,
              grouping: true,
            });
          }
          else {
            ElMessage({
              message: i18n.global.t('云端同步失败'),
              type: resp.status,
              grouping: true,
            });
          }
          reject();
        }
        resolve();
      },
      error() {
        reject();
      },
    });
  });
};

// return: object
export const cloudDownload = async (fileName) => {
  const userInfo = getUserInfo();
  return new Promise((resolve, reject) => {
    let content = null;
    request({
      url: http_base_url + '/cloud/download',
      method: 'GET',
      data: {
        user: userInfo.name + '-' + userInfo.time,
        fileName: fileName,
      },
      success(resp) {
        if(resp.status === 'success') {
          content = JSON.parse(aesDecrypt(resp.data, userInfo.key));
        }
        resolve(content);
      },
      error() {
        reject();
      },
    });
  });
};

// 同步项
const syncItems = Object.values(cloudStore);

export const syncUpload = async (items) => {
  const uploadItems = syncItems.filter((syncItem) => {
    return !items || items.includes(syncItem);
  });
  if(uploadItems.length === 0) return;
  const promises = [];
  for(const uploadItem of uploadItems) {
    const content = browser.localStorage.getItem(uploadItem);
    if(content) {
      const promise = cloudUpload(uploadItem, '', aesDecrypt(content));
      promises.push(promise);
    }
  }

  return Promise.allSettled(promises);
};

export const syncDownload = async (userInfo) => {
  if(userInfo) browser.localStorage.setItem(localStore['user'], userInfo, false);
  const promises = [];
  const uploadItems = [];
  const downloadItems = [...syncItems];
  for(const downloadItem of downloadItems) {
    const promise = cloudDownload(downloadItem);
    promise.then((content) => {
      if(content) browser.localStorage.setItem(downloadItem, aesEncrypt(JSON.stringify(content)), false);
      else {
        if(userInfo) browser.localStorage.removeItem(downloadItem);
        else if(browser.localStorage.getItem(downloadItem)) uploadItems.push(downloadItem);
      }
    });
    promises.push(promise);
  }

  return Promise.allSettled(promises).then(() => {
    if(userInfo) browser.location.reload();
    else if(uploadItems.length > 0) syncUpload(uploadItems);
  });
};

