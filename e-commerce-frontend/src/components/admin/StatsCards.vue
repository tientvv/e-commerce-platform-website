<template>
  <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
    <div
      v-for="stat in stats"
      :key="stat.label"
      :class="`bg-gradient-to-r ${stat.gradient} rounded-lg p-4`"
    >
      <div class="flex items-center">
        <div :class="`p-2 ${stat.iconBg} rounded-lg`">
          <i :class="`${stat.icon} text-white`"></i>
        </div>
        <div class="ml-3">
          <p :class="`text-sm font-medium ${stat.labelColor}`">{{ stat.label }}</p>
          <p :class="`text-2xl font-bold ${stat.valueColor}`">{{ stat.value }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  stats: {
    type: Array,
    required: true,
    validator: (stats) =>
      stats.every(
        (stat) =>
          stat.label &&
          typeof stat.value !== 'undefined' &&
          stat.icon &&
          stat.gradient &&
          stat.iconBg &&
          stat.labelColor &&
          stat.valueColor,
      ),
  },
})
</script>
