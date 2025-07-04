<template>
  <div class="flex justify-center space-x-2">
    <button
      v-for="action in actions"
      :key="action.key"
      @click="$emit('action', action.key, item)"
      :class="[
        'inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md text-white transition-colors duration-200',
        getButtonClass(action.variant),
      ]"
    >
      <i :class="`${action.icon} mr-1`"></i>
      {{ action.label }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  actions: {
    type: Array,
    required: true,
    validator: (actions) =>
      actions.every((action) => action.key && action.label && action.icon && action.variant),
  },
  item: { type: Object, required: true },
})

defineEmits(['action'])

const getButtonClass = (variant) => {
  const classes = {
    primary: 'bg-blue-600 hover:bg-blue-700 focus:ring-blue-500',
    success: 'bg-green-600 hover:bg-green-700 focus:ring-green-500',
    danger: 'bg-red-600 hover:bg-red-700 focus:ring-red-500',
    warning: 'bg-yellow-600 hover:bg-yellow-700 focus:ring-yellow-500',
    secondary: 'bg-gray-600 hover:bg-gray-700 focus:ring-gray-500',
  }
  return `${classes[variant]} focus:outline-none focus:ring-2 focus:ring-offset-2`
}
</script>
