<template>
  <form @submit.prevent="$emit('submit')">
    <slot />
  </form>
</template>

<script setup>
defineEmits(['submit'])
</script>
