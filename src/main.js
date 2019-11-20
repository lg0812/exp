import Vue from 'vue'
import App from './App.vue'
import CKEditor from '@ckeditor/ckeditor5-vue';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';

Vue.use(ElementUI);
Vue.use( CKEditor );
Vue.config.productionTip = false

new Vue({
  render: h => h(App),
}).$mount('#app')
