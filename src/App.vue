<template>
  <div id="app">
    <custom-btn-group :btns='btns'></custom-btn-group>
    <ckeditor :editor="editor" v-model="editorData" :config="editorConfig"></ckeditor>
  </div>
</template>

<script>
  import CustomBtnGroup from './components/CustomBtnGroup'
  export default {
    name: 'app',
    components: {CustomBtnGroup},
    comments: ['CustomBtnGroup'],
    data() {
      return {
        btns: [
          {
            text: '添加',
            icon: 'el-icon-plus',
            handler: () => {
              alert('添加')
            }
          },{
            text: '删除',
            icon: 'el-icon-delete',
            handler: () => {
              alert('删除')
            }
          },{
            text: '修改',
            icon: 'el-icon-edit',
            handler: () => {
              alert('修改')
            }
          },{
            text: '上移',
            icon: 'el-icon-arrow-down',
            handler: () => {
              alert('上移')
            }
          },{
            text: '下移',
            icon: 'el-icon-arrow-up',
            handler: () => {
              alert('下移')
            }
          }
        ],

        editor: window.ClassicEditor,
        editorData: '<p>Content of the editor.</p>',
        editorConfig: {
          ready: () => {

          },
          ckfinder: {
            uploadUrl: 'aliyun.com',
            uploadFunc: {
              handler: (resolve) => {
                  resolve({
                    default: 'https://www.baidu.com/img/bd_logo1.png'
                  })
              }
            }
          }
        }
      };
    }
  }
</script>