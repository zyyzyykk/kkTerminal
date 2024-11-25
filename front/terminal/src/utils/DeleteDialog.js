import { ElMessageBox } from 'element-plus';
import { QuestionFilled } from '@element-plus/icons-vue';

export const deleteDialog = (title,content,callback) => {
    return ElMessageBox.alert(content || '确定删除吗?', title || '提示', {
      autofocus: false,
      customStyle:'font-size: 13px; color: black; width: 250px; user-select: none;',
      cancelButtonText: '取消',
      confirmButtonText: '确定',
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