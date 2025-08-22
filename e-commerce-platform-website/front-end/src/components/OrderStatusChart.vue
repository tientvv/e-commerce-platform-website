<template>
  <div class="chart-container">
    <canvas ref="chartCanvas"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const props = defineProps({
  data: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const chartCanvas = ref(null)
let chartInstance = null

const createChart = () => {
  if (!chartCanvas.value) return

  const ctx = chartCanvas.value.getContext('2d')

  if (chartInstance) {
    chartInstance.destroy()
  }

  const statusData = [
    {
      label: 'Chờ xử lý',
      value: props.data.pendingProcessingOrders || 0,
      color: '#fbbf24'
    },
    {
      label: 'Đã xử lý',
      value: props.data.processedOrders || 0,
      color: '#3b82f6'
    },
    {
      label: 'Chờ lấy hàng',
      value: props.data.readyForPickupOrders || 0,
      color: '#8b5cf6'
    },
    {
      label: 'Đang giao hàng',
      value: props.data.inTransitOrders || 0,
      color: '#06b6d4'
    },
    {
      label: 'Đã giao hàng',
      value: props.data.deliveredOrders || 0,
      color: '#10b981'
    },
    {
      label: 'Đã hủy',
      value: props.data.cancelledOrders || 0,
      color: '#ef4444'
    }
  ].filter(item => item.value > 0)

  const chartData = {
    labels: statusData.map(item => item.label),
    datasets: [
      {
        data: statusData.map(item => item.value),
        backgroundColor: statusData.map(item => item.color),
        borderColor: statusData.map(item => item.color),
        borderWidth: 2,
        hoverOffset: 4
      }
    ]
  }

  chartInstance = new Chart(ctx, {
    type: 'doughnut',
    data: chartData,
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            usePointStyle: true,
            padding: 20,
            font: {
              size: 12,
              weight: '600'
            }
          }
        },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          titleColor: '#ffffff',
          bodyColor: '#ffffff',
          borderColor: 'rgba(59, 130, 246, 0.5)',
          borderWidth: 1,
          cornerRadius: 8,
          displayColors: true,
          callbacks: {
            label: function(context) {
              const total = context.dataset.data.reduce((a, b) => a + b, 0)
              const percentage = ((context.parsed / total) * 100).toFixed(1)
              return `${context.label}: ${context.parsed} đơn (${percentage}%)`
            }
          }
        }
      },
      cutout: '60%'
    }
  })
}

const updateChart = () => {
  if (props.data && Object.keys(props.data).length > 0) {
    nextTick(() => {
      createChart()
    })
  }
}

watch(() => props.data, updateChart, { deep: true })

onMounted(() => {
  if (props.data && Object.keys(props.data).length > 0) {
    createChart()
  }
})
</script>

<style scoped>
.chart-container {
  position: relative;
  height: 100%;
  width: 100%;
}
</style>
