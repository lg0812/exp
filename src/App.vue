<template>
  <div id="app">
    <ckeditor :editor="editor" v-model="editorData" :config="editorConfig"></ckeditor>
  </div>
</template>

<script>
  import ClassicEditor from '@ckeditor/ckeditor5-build-classic';
  export default {
    name: 'app',
    comments: ['CustomBtnGroup'],
    data() {
      return {
        editor: ClassicEditor,
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