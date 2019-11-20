<template>
    <div class='custom-btn-wrap'>
        <template v-if="showMore">
            <el-button v-for="(btn, index) in first3" :key="index" size="small" plain @click="btn.handler">
                <i v-if="btn.icon" :class="btn.icon"></i>
                {{btn.text}}
            </el-button>
            <el-dropdown split-button type="primary" @click="handleClick">
                更多
                <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item v-for="(btn, index) in left" :key="index">{{btn.text}}</el-dropdown-item>
                </el-dropdown-menu>
            </el-dropdown>
        </template>
        <template v-else>
            <el-button v-for="(btn, index) in btns" :key="index" size="small" plain @click="btn.handler">
                <i v-if="btn.icon" :class="btn.icon"></i>
                {{btn.text}}
            </el-button>
        </template>
    </div>
</template>

<script>
  export default {
    name: "CustomBtnGroup",
    props: {
      btns: {
        type: Array,
        default() {
          return []
        }
      }
    },
    computed: {
      showMore() {
        return this.btns.length > 4;
      },
      first3() {
        return this.btns.slice(0,3);
      },
      left() {
        return this.btns.slice(3);
      }
    }
  }
</script>

<style scoped>
.custom-btn-wrap {
    display: flex;

}
</style>