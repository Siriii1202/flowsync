<template>
  <!--
    根组件——整个应用的容器
    router-view 是一个"占位符"，Vue Router 会根据当前 URL
    自动把对应的页面组件渲染到这里
  -->
  <router-view />
</template>

<script setup>
// App.vue 是项目的根组件，它不需要任何逻辑
// 所有页面的切换都由 router-view 自动完成
</script>

<style>
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
  background-color: #F8F9FB;
}
</style>
