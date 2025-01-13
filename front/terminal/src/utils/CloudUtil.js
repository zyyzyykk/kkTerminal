import { decrypt, encrypt} from '@/utils/Encrypt';
import { generateRandomString } from '@/utils/StringUtil';

import $ from 'jquery';
import { http_base_url } from '@/env/BaseUrl';
import { ElMessage } from 'element-plus';
import i18n from "@/locales/i18n";

const getUserInfo = () => {
  if(!localStorage.getItem('user')) {
    localStorage.setItem('user', encrypt(crypto.randomUUID() + '@' + generateRandomString(16) + '@' + new Date().getTime()));
  }
  const userInfo = decrypt(localStorage.getItem('user')).split('@');
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
  const blob = new Blob([encrypt(content, userInfo.key)], { type: 'application/octet-stream' });
  // 创建File对象
  const file = new File([blob], name);
  let formData = new FormData();
  formData.append('user',userInfo.name);
  formData.append('type',type);
  formData.append('name',name);
  formData.append('file',file);
  await $.ajax({
    url: http_base_url + '/cloud',
    type:'post',
    data: formData,
    contentType : false,
    processData : false,
    success(resp){
      if(resp.status != 'success') {
        if(resp.code == 506) {
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
      }
    }
  });
};

// return: object
export const load = async (fileName) => {
  const userInfo = getUserInfo();
  let content = null;
  await $.ajax({
    url: http_base_url + '/load',
    method: 'GET',
    data: {
      time:new Date().getTime(),
      user: userInfo.name,
      fileName: fileName,
    },
    success(resp) {
      if(resp.status == 'success') content = JSON.parse(decrypt(resp.data, userInfo.key));
      else {
        ElMessage({
          message: i18n.global.t('云端文件加载失败'),
          type: resp.status,
          grouping: true,
        });
      }
    }
  });
  return content;
};

// 需要同步的内容
const syncArr = ['options','tcodes'];
const oneDayInMs = 24 * 60 * 60 * 1000;

export const syncUpload = async () => {
  const userInfo = getUserInfo();
  if(new Date().getTime() - userInfo.time < oneDayInMs) return;
  for(let i = 0; i < syncArr.length; i++) {
    const content = decrypt(localStorage.getItem(syncArr[i]));
    await cloud(syncArr[i],'',content);
  }
};

export const syncDownload = async () => {
  for(let i = 0; i < syncArr.length; i++) {
    const content = await load(syncArr[i]);
    if(content) localStorage.setItem(syncArr[i], encrypt(JSON.stringify(content)));
  }
  window.location.reload();
};