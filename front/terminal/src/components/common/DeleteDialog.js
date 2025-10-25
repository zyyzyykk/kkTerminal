import { ElMessageBox } from 'element-plus';
import { QuestionFilled } from '@element-plus/icons-vue';
import i18n from "@/locales/i18n";

export const deleteDialog = (title,content,callback) => {
    return ElMessageBox.alert(content || i18n.global.t('确定删除吗？'), title || i18n.global.t('提示'), {
      autofocus: false,
      customStyle: 'font-size: 13px; color: black; width: 265px; user-select: none;',
      cancelButtonText: i18n.global.t('取消'),
      confirmButtonText: i18n.global.t('确定'),
      showCancelButton: true,
      showConfirmButton: true,
      cancelButtonClass: 'el-button is-text',
      confirmButtonClass: 'el-button--danger',
      buttonSize: 'small',
      icon: QuestionFilled,
      type: 'warning',
      callback: (action) => {
        if(action === 'confirm') callback();
      },
    });
};
