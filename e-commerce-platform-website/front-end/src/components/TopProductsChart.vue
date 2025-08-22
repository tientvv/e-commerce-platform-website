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
    type: Array,
    default: () => []
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

  // Giới hạn hiển thị top 10 sản phẩm
  const topProducts = props.data.slice(0, 10)

  const chartData = {
    labels: topProducts.map(item => {
      const productName = item.productName || 'N/A'
      const variantInfo = item.variantValue ? ` (${item.variantValue})` : ''
      return (productName + variantInfo).substring(0, 20) + (productName.length > 20 ? '...' : '')
    }),
    datasets: [
      {
        label: 'Số lượng bán (cái)',
        data: topProducts.map(item => item.totalQuantity || 0),
        backgroundColor: 'rgba(59, 130, 246, 0.8)',
        borderColor: 'rgba(59, 130, 246, 1)',
        borderWidth: 1,
        borderRadius: 4,
        borderSkipped: false,
      },
      {
        label: 'Doanh thu (VNĐ)',
        data: topProducts.map(item => item.totalRevenue || 0),
        backgroundColor: 'rgba(16, 185, 129, 0.8)',
        borderColor: 'rgba(16, 185, 129, 1)',
        borderWidth: 1,
        borderRadius: 4,
        borderSkipped: false,
        yAxisID: 'y1'
      }
    ]
  }

  chartInstance = new Chart(ctx, {
    type: 'bar',
    data: chartData,
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: {
        mode: 'index',
        intersect: false,
      },
      plugins: {
        legend: {
          position: 'top',
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
              if (context.datasetIndex === 0) {
                return `Số lượng: ${context.parsed.y} cái`
              } else {
                return `Doanh thu: ${formatCurrency(context.parsed.y)}`
              }
            }
          }
        }
      },
      scales: {
        x: {
          display: true,
          title: {
            display: true,
            text: 'Sản phẩm',
            font: {
              size: 14,
              weight: '600'
            }
          },
          grid: {
            display: false
          },
          ticks: {
            font: {
              size: 10
            },
            maxRotation: 45,
            minRotation: 0
          }
        },
        y: {
          type: 'linear',
          display: true,
          position: 'left',
          title: {
            display: true,
            text: 'Số lượng (cái)',
            font: {
              size: 14,
              weight: '600'
            }
          },
          grid: {
            display: true,
            color: 'rgba(0, 0, 0, 0.1)'
          },
          ticks: {
            font: {
              size: 12
            }
          }
        },
        y1: {
          type: 'linear',
          display: true,
          position: 'right',
          title: {
            display: true,
            text: 'Doanh thu (VNĐ)',
            font: {
              size: 14,
              weight: '600'
            }
          },
          grid: {
            drawOnChartArea: false,
          },
          ticks: {
            font: {
              size: 12
            },
            callback: function(value) {
              return formatCurrency(value)
            }
          }
        }
      }
    }
  })
}

const formatCurrency = (amount) => {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount)
}

const updateChart = () => {
  if (props.data && props.data.length > 0) {
    nextTick(() => {
      createChart()
    })
  }
}

watch(() => props.data, updateChart, { deep: true })

onMounted(() => {
  if (props.data && props.data.length > 0) {
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
