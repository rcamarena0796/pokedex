<script setup>
import AppHeader from './components/AppHeader.vue'
</script>

<template>
  <a class="skip-link" href="#main">Skip to content</a>
  <AppHeader />
  <main id="main">
    <RouterView />
  </main>
</template>

<style scoped>
.skip-link {
  position: absolute;
  left: 8px;
  top: -48px;
  padding: 8px 12px;
  background: var(--ink);
  color: #fff;
  z-index: 10;
}
.skip-link:focus { top: 8px; }
</style>
