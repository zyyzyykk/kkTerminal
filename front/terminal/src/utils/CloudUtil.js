import { aesDecrypt, aesEncrypt } from '@/utils/Encrypt';
import { generateRandomString } from '@/utils/StringUtil';

import $ from 'jquery';
import { http_base_url } from '@/env/BaseUrl';
import { ElMessage } from 'element-plus';
import i18n from "@/locales/i18n";
import { localStore, cloudStore } from "@/env/Store";

const getUserInfo = () => {
  if(!localStorage.getItem(localStore['user'])) {
    localStorage.setItem(localStore['user'], aesEncrypt(crypto.randomUUID() + '@' + generateRandomString(16) + '@' + new Date().getTime()));
  }
  const userInfo = aesDecrypt(localStorage.getItem(localStore['user'])).split('@');
  return {
    name: userInfo[0],
    key: userInfo[1],
    time: Number(userInfo[2]),
  }
};

// content: string
export const cloud = async (type, name, content) => {
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
    $.ajax({
      url: http_base_url + '/cloud',
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
export const load = async (fileName) => {
  const userInfo = getUserInfo();
  return new Promise((resolve, reject) => {
    let content = null;
    $.ajax({
      url: http_base_url + '/load',
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

// 需要同步的内容
const syncArr = Object.values(cloudStore);

export const syncUpload = async (items) => {
  const syncItems = items || syncArr;
  const promises = [];
  for(let i = 0; i < syncItems.length; i++) {
    const content = localStorage.getItem(syncItems[i]);
    if(content) {
      const promise = cloud(syncItems[i], '', aesDecrypt(content));
      promises.push(promise);
    }
  }

  return Promise.allSettled(promises);
};

export const syncDownload = async (userInfo) => {
  if(userInfo) localStorage.setItem(localStore['user'], userInfo);
  const promises = [];
  for(let i = 0; i < syncArr.length; i++) {
    const promise = load(syncArr[i]);
    promise.then((content) => {
      if(content) localStorage.setItem(syncArr[i], aesEncrypt(JSON.stringify(content)));
      // TODO
      // else localStorage.removeItem(syncArr[i]);
    });
    promises.push(promise);
  }

  return Promise.allSettled(promises).then(() => {
    if(userInfo) window.location.reload();
  });
};

export const localStoreUtil = {
  setItem(key, value) {
    localStorage.setItem(key, value);
    // 多端同步-上传
    syncUpload([key]);
  },
  getItem(key) {
    return localStorage.getItem(key);
  },
  removeItem(key) {
    localStorage.removeItem(key);
  },
};
