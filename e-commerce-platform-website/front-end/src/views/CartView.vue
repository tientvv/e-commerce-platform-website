<template>
  <div class="flex flex-col h-screen">
    <NavbarView />
    <main class="flex-1 max-w-[1440px] w-full mx-auto px-4 py-6">
      <!-- Breadcrumb -->
      <nav class="mb-6">
        <ol class="flex items-center space-x-2 text-sm text-gray-500">
          <li>
            <RouterLink to="/" class="hover:text-blue-600">Trang chủ</RouterLink>
          </li>
          <li class="flex items-center">
            <ChevronRight class="w-4 h-4 mx-2" />
            <span class="text-gray-800 font-medium">Giỏ hàng</span>
          </li>
        </ol>
      </nav>

      <!-- Page Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Giỏ hàng</h1>
      </div>

      <!-- Empty Cart -->
      <div v-if="cartItems.length === 0" class="text-center py-12">
        <div class="mx-auto h-24 w-24 text-gray-400 flex items-center justify-center">
          <ShoppingCart class="w-16 h-16" />
        </div>
        <h3 class="mt-4 text-lg font-medium text-gray-900">Giỏ hàng trống</h3>
        <p class="mt-2 text-gray-500">Bạn chưa có sản phẩm nào trong giỏ hàng.</p>
        <div class="mt-6">
          <a
            href="/"
            class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
          >
            Tiếp tục mua sắm
          </a>
        </div>
      </div>

      <!-- Cart Content -->
      <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Cart Items -->
        <div class="lg:col-span-2">
          <div class="bg-white rounded-lg">
            <div class="px-6 py-4 border-b border-gray-200">
              <div class="flex items-center justify-between">
                <h2 class="text-lg font-medium text-gray-900">Sản phẩm ({{ cartItems.length }})</h2>
                <div class="flex items-center">
                  <input
                    type="checkbox"
                    :checked="isAllSelected"
                    @change="toggleSelectAll"
                    class="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500 focus:ring-2"
                  />
                  <label class="ml-2 text-sm font-medium text-gray-700">Chọn tất cả</label>
                </div>
              </div>
            </div>
            <div class="divide-y divide-gray-200">
              <div v-for="item in cartItems" :key="item.id" class="p-6 flex">
                <!-- Checkbox -->
                <div class="flex items-start mr-4">
                  <input
                    type="checkbox"
                    :checked="selectedItems.includes(item.id)"
                    @change="toggleSelectItem(item.id)"
                    class="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500 focus:ring-2 mt-1"
                  />
                </div>

                <!-- Product Image -->
                <div class="flex-shrink-0 w-24 h-24">
                  <img
                    v-if="getProductImage(item) && getProductImage(item) !== '/placeholder-image.jpg'"
                    :src="getProductImage(item)"
                    :alt="getProductName(item)"
                    class="w-full h-full object-cover rounded-lg"
                    @error="handleImageError"
                  />
                  <div v-else class="w-full h-full bg-gray-100 rounded-lg flex items-center justify-center">
                    <svg class="w-8 h-8 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clip-rule="evenodd" />
                    </svg>
                  </div>
                </div>

                <!-- Product Details -->
                <div class="ml-6 flex-1">
                  <div class="flex justify-between">
                    <div>
                      <h3 class="text-lg font-medium text-gray-900">
                        <a :href="`/product/${getProductId(item)}`" class="hover:text-blue-600">
                          {{ getProductName(item) }}
                        </a>
                      </h3>
                      <p v-if="getVariantDetails(item)" class="mt-1 text-sm text-gray-500">
                        <span class="font-medium">Biến thể:</span> {{ getVariantDetails(item) }}
                      </p>
                      <p class="mt-1 text-sm text-gray-500">
                        Giá: {{ getItemPrice(item) > 0 ? formatPrice(getItemPrice(item)) : 'Liên hệ' }}
                      </p>
                    </div>
                    <div class="text-right">
                      <p class="text-lg font-medium text-gray-900">
                        {{ getItemPrice(item) > 0 ? formatPrice(getItemPrice(item) * item.quantity) : 'Liên hệ' }}
                      </p>
                    </div>
                  </div>

                  <!-- Quantity Controls -->
                  <div class="mt-4 flex items-center justify-between">
                    <div class="flex items-center space-x-3">
                      <button
                        @click="updateQuantity(item.id, item.quantity - 1)"
                        :disabled="item.quantity <= 1"
                        class="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        <Minus class="w-4 h-4" />
                      </button>
                      <span class="text-lg font-medium w-12 text-center">{{ item.quantity }}</span>
                      <button
                        @click="updateQuantity(item.id, item.quantity + 1)"
                        class="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center hover:bg-gray-50"
                      >
                        <Plus class="w-4 h-4" />
                      </button>
                    </div>
                    <button @click="removeItem(item.id)" class="text-red-600 hover:text-red-800 text-sm font-medium">
                      Xóa
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Delete Selected Button -->
            <div v-if="selectedItems.length > 0" class="px-6 py-4 border-t border-gray-200">
              <button
                @click="deleteSelectedItems"
                class="w-full bg-red-600 text-white py-2 px-4 rounded-lg hover:bg-red-700 transition-colors font-medium"
              >
                Xóa đã chọn ({{ selectedItems.length }})
              </button>
            </div>
          </div>
        </div>

        <!-- Cart Summary -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-lg sticky top-8">
            <div class="px-6 py-4 border-b border-gray-200">
              <h2 class="text-lg font-medium text-gray-900">Tổng đơn hàng</h2>
            </div>
            <div class="p-6 space-y-4">
              <!-- Subtotal -->
              <div class="flex justify-between text-base font-medium text-gray-900">
                <p>Tạm tính {{ selectedItems.length > 0 ? `(${selectedItems.length} sản phẩm)` : '' }}</p>
                <p>{{ formatPrice(subtotal) }}</p>
              </div>

              <!-- Shipping -->
              <div class="border-t border-gray-200 pt-4">
                <label class="block text-sm font-medium text-gray-700 mb-2">Phương thức vận chuyển</label>
                <n-select
                  v-model:value="selectedShipping"
                  :options="shippingOptions"
                  placeholder="Chọn phương thức vận chuyển"
                  clearable
                />
              </div>

              <!-- Shipping Cost -->
              <div v-if="selectedShippingObject" class="flex justify-between text-sm text-gray-600">
                <p>Phí vận chuyển</p>
                <p>{{ formatPrice(selectedShippingObject.price) }}</p>
              </div>

              <!-- Payment Method -->
              <div class="border-t border-gray-200 pt-4">
                <label class="block text-sm font-medium text-gray-700 mb-2">Phương thức thanh toán</label>
                <n-select
                  v-model:value="selectedPayment"
                  :options="paymentOptions"
                  placeholder="Chọn phương thức thanh toán"
                  clearable
                />
              </div>

              <!-- Discount Code -->
              <div class="border-t border-gray-200 pt-4">
                <label class="block text-sm font-medium text-gray-700 mb-2">Mã giảm giá</label>
                <div class="flex space-x-2">
                  <button
                    @click="showDiscountDialog = true"
                    class="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-left text-sm text-gray-700 hover:border-blue-500 hover:bg-blue-50 transition-colors"
                  >
                    <span v-if="appliedDiscount" class="text-green-600 font-medium">
                      Mã giảm giá đã được áp dụng
                    </span>
                    <span v-else class="text-gray-500">
                      Chọn mã giảm giá
                    </span>
                  </button>
                </div>

                <!-- Hiển thị voucher đã chọn -->
                <div v-if="appliedDiscount" class="mt-3">
                  <div class="voucher-card-shopee cursor-default">
                    <div class="flex w-full">
                      <!-- Left section - Blue stub -->
                      <div class="voucher-left">
                        <div class="logo-section">
                          <div class="shop-logo">E</div>
                          <div class="brand-name">E-COMMERCE</div>
                        </div>
                      </div>

                      <!-- Right section - White details -->
                      <div class="voucher-right">
                        <div class="discount-info">
                          <div class="discount-title">
                            <span v-if="appliedDiscount.discountType === 'PERCENTAGE'">
                              {{ appliedDiscount.name }} - Giảm {{ appliedDiscount.discountValue }}%
                            </span>
                            <span v-else-if="appliedDiscount.discountType === 'FIXED'">
                              {{ appliedDiscount.name }} - Giảm {{ formatPrice(appliedDiscount.discountValue) }}
                            </span>
                          </div>
                          <div class="discount-condition">
                            <span v-if="appliedDiscount.minOrderValue">
                              Đơn hàng tối thiểu {{ formatPrice(appliedDiscount.minOrderValue) }}
                            </span>
                            <span v-else>
                              Áp dụng cho mọi đơn hàng
                            </span>
                          </div>
                        </div>

                        <div class="flex justify-between items-end">
                          <div class="validity-info">
                            <span v-if="appliedDiscount.endDate">
                              Có hiệu lực đến: {{ formatDate(appliedDiscount.endDate) }}
                            </span>
                            <span v-else>
                              Không giới hạn thời gian
                            </span>
                          </div>
                          <button
                            @click="removeDiscount"
                            class="text-red-600 hover:text-red-800 text-sm font-medium"
                          >
                            Hủy
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="discountError" class="mt-2 text-sm text-red-600">
                  {{ discountError }}
                </div>
              </div>

              <!-- Total -->
              <div class="border-t border-gray-200 pt-4">
                <div class="flex justify-between text-lg font-bold text-gray-900">
                  <p>Tổng cộng</p>
                  <p>{{ formatPrice(finalTotal) }}</p>
                </div>
              </div>

              <!-- Checkout Button -->
              <button
                @click="handleCheckout"
                class="w-full bg-blue-600 text-white py-3 px-4 rounded-lg hover:bg-blue-700 transition-colors font-medium"
              >
                Đặt hàng
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
    <FooterView />

    <!-- Discount Dialog -->
    <n-modal v-model:show="showDiscountDialog" preset="card" style="width: 500px; max-width: 90vw;">
      <template #header>
        <div class="text-lg font-semibold text-gray-900">Chọn mã giảm giá</div>
      </template>

      <div class="max-h-96 overflow-y-auto">
        <div class="grid grid-cols-1 gap-3">
          <div
            v-for="discount in availableDiscounts.filter(d => {
              // Kiểm tra hết hạn
              if (d.endDate && new Date() > new Date(d.endDate)) {
                return false
              }
              // Kiểm tra điều kiện tối thiểu
              if (d.minOrderValue && subtotal.value < d.minOrderValue) {
                return false
              }
              return true
            })"
            :key="discount.id"
            class="voucher-card-shopee cursor-pointer"
            @click="selectDiscount(discount)"
          >
            <div class="flex w-full">
              <!-- Left section - Orange stub -->
              <div class="voucher-left">
                <div class="logo-section">
                  <div class="shop-logo">E</div>
                  <div class="brand-name">E-COMMERCE</div>
                </div>
              </div>

              <!-- Right section - White details -->
              <div class="voucher-right">
                <div class="discount-info">
                  <div class="discount-title">
                    <span v-if="discount.discountType === 'PERCENTAGE'">
                      {{ discount.name }} - Giảm {{ discount.discountValue }}%
                    </span>
                    <span v-else-if="discount.discountType === 'FIXED'">
                      {{ discount.name }} - Giảm {{ formatPrice(discount.discountValue) }}
                    </span>
                  </div>
                  <div class="discount-condition">
                    <span v-if="discount.minOrderValue">
                      Đơn hàng tối thiểu {{ formatPrice(discount.minOrderValue) }}
                    </span>
                    <span v-else>
                      Áp dụng cho mọi đơn hàng
                    </span>
                  </div>
                </div>

                <div class="flex justify-between items-end">
                  <div class="validity-info">
                    <span v-if="discount.endDate">
                      Có hiệu lực đến: {{ formatDate(discount.endDate) }}
                    </span>
                    <span v-else>
                      Không giới hạn thời gian
                    </span>
                  </div>
                  <div class="flex gap-2 items-center">
                    <button
                      class="apply-btn"
                      @click.stop="selectDiscount(discount)"
                    >
                      Áp dụng
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { ChevronRight, ShoppingCart, Minus, Plus } from 'lucide-vue-next'
import { NSelect, useMessage, NModal } from 'naive-ui'
import { useCart } from '../composables/useCart'

import axios from '../utils/axios'
import NavbarView from './components/NavbarView.vue'
import FooterView from './components/FooterView.vue'

const message = useMessage()

const router = useRouter()
const { cartItems, updateQuantity, removeItem, removeOrdered, total } = useCart()

// Reactive data
const shippings = ref([])
const selectedShipping = ref(null)
const payments = ref([])
const selectedPayment = ref(null)
const availableDiscounts = ref([])
const selectedDiscountCode = ref('')
const appliedDiscount = ref(null)
const discountError = ref('')
const showDiscountDialog = ref(false)
const selectedItems = ref([])

// Computed
const subtotal = computed(() => {
  return cartItems.value
    .filter(item => selectedItems.value.includes(item.id))
    .reduce((total, item) => {
      const itemPrice = getItemPrice(item)
      return total + (itemPrice * item.quantity)
    }, 0)
})

const shippingCost = computed(() => {
  if (!selectedShippingObject.value || subtotal.value === 0) {
    return 0
  }
  return selectedShippingObject.value.price
})

const discountAmount = computed(() => {
  if (!appliedDiscount.value || subtotal.value <= 0) return 0

  if (appliedDiscount.value.discountType === 'PERCENTAGE') {
    return (subtotal.value * appliedDiscount.value.discountValue) / 100
  } else if (appliedDiscount.value.discountType === 'FIXED') {
    return Math.min(appliedDiscount.value.discountValue, subtotal.value)
  }
  return 0
})

const finalTotal = computed(() => {
  return Math.max(0, subtotal.value + shippingCost.value - discountAmount.value)
})

// Computed để kiểm tra tất cả sản phẩm đã được chọn chưa
const isAllSelected = computed(() => {
  return cartItems.value.length > 0 && selectedItems.value.length === cartItems.value.length
})

// Computed để tìm object từ ID
const selectedShippingObject = computed(() => {
  if (!selectedShipping.value) return null
  return shippings.value.find(s => s.id === selectedShipping.value)
})

const selectedPaymentObject = computed(() => {
  if (!selectedPayment.value) return null
  return payments.value.find(p => p.id === selectedPayment.value)
})

// Computed options for selects
const shippingOptions = computed(() => {
  return shippings.value.map((shipping) => ({
    label: `${shipping.shippingMethod} - ${formatPrice(shipping.price)}`,
    value: shipping.id, // Sử dụng ID thay vì object
  }))
})

const paymentOptions = computed(() => {
  return payments.value.map((payment) => ({
    label: payment.paymentName,
    value: payment.id, // Sử dụng ID thay vì object
  }))
})

// Methods
const fetchShippings = async () => {
  try {
    const response = await axios.get('/api/shippings')
    shippings.value = response.data.shippings || []

    // Bỏ logic chọn mặc định phương thức vận chuyển đầu tiên
  } catch (error) {
    console.error('Error fetching shippings:', error)
  }
}

const fetchPayments = async () => {
  try {
    const response = await axios.get('/api/payments')
    payments.value = response.data.payments || []

    // Mặc định chọn COD nếu có
    const codPayment = payments.value.find((p) => p.paymentCode === 'COD')

    if (codPayment) {
      selectedPayment.value = codPayment.id
    } else if (payments.value.length > 0) {
      // Nếu không có COD, chọn payment đầu tiên
      selectedPayment.value = payments.value[0].id
    }
  } catch (error) {
    console.error('Error fetching payments:', error)
  }
}

const fetchDiscounts = async () => {
  try {
    console.log('Fetching discounts from API...')
    const response = await axios.get('/api/discounts')
    console.log('Discounts API response:', response)
    console.log('Discounts response data:', response.data)

    // Kiểm tra cấu trúc response
    if (response.data && response.data.discounts) {
      availableDiscounts.value = response.data.discounts
      console.log('Using discounts from response.data.discounts')
    } else if (Array.isArray(response.data)) {
      availableDiscounts.value = response.data
      console.log('Using discounts from response.data (array)')
    } else {
      availableDiscounts.value = []
      console.log('No discounts found in response')
    }

    console.log('Final available discounts:', availableDiscounts.value)

    // Bỏ logic chọn mặc định mã đầu tiên
  } catch (error) {
    console.error('Error fetching discounts from API:', error)
    console.error('Error details:', error.response?.data)
    availableDiscounts.value = []

    // Thêm mã giảm giá mẫu để test
    console.log('Adding sample discounts for testing...')
    availableDiscounts.value = [
      {
        id: '1',
        name: 'SAVE10',
        description: 'Giảm 10% cho đơn hàng từ 500k',
        discountType: 'PERCENTAGE',
        discountValue: 10,
        minOrderValue: 500000,
        endDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), // 7 ngày nữa
      },
      {
        id: '2',
        name: 'SAVE50K',
        description: 'Giảm 50k cho đơn hàng từ 300k',
        discountType: 'FIXED',
        discountValue: 50000,
        minOrderValue: 300000,
        endDate: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000), // 3 ngày nữa
      },
      {
        id: '3',
        name: 'FLASH20',
        description: 'Giảm 20% cho đơn hàng từ 200k',
        discountType: 'PERCENTAGE',
        discountValue: 20,
        minOrderValue: 200000,
        endDate: new Date(Date.now() + 1 * 24 * 60 * 60 * 1000), // 1 ngày nữa
      },
      {
        id: '4',
        name: 'FREESHIP',
        description: 'Miễn phí vận chuyển cho đơn hàng từ 100k',
        discountType: 'FIXED',
        discountValue: 30000,
        minOrderValue: 100000,
        endDate: new Date(Date.now() + 5 * 24 * 60 * 60 * 1000), // 5 ngày nữa
      },
      {
        id: '5',
        name: 'NEWUSER',
        description: 'Giảm 15% cho khách hàng mới',
        discountType: 'PERCENTAGE',
        discountValue: 15,
        minOrderValue: 150000,
        endDate: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000), // Hết hạn 1 ngày trước
      },
      {
        id: '6',
        name: 'VIP30',
        description: 'Giảm 30% cho VIP',
        discountType: 'PERCENTAGE',
        discountValue: 30,
        minOrderValue: 1000000,
        endDate: new Date(Date.now() + 10 * 24 * 60 * 60 * 1000), // 10 ngày nữa
      },
    ]

    // Bỏ logic chọn mặc định mã đầu tiên
  }
}

const applyDiscount = async () => {
  if (!selectedDiscountCode.value) {
    appliedDiscount.value = null
    discountError.value = ''
    return
  }

  try {
    // Tìm mã giảm giá trong danh sách có sẵn
    const discount = availableDiscounts.value.find((d) => d.name === selectedDiscountCode.value)
    if (discount) {
      // Kiểm tra hết hạn
      if (discount.endDate) {
        const now = new Date()
        const endDate = new Date(discount.endDate)
        if (now > endDate) {
          discountError.value = 'Mã giảm giá đã hết hạn'
          appliedDiscount.value = null
          return
        }
      }

      // Kiểm tra điều kiện tối thiểu
      if (discount.minOrderValue && subtotal.value < discount.minOrderValue) {
        discountError.value = 'Đơn hàng của bạn không đủ điều kiện'
        appliedDiscount.value = null
        return
      }

      appliedDiscount.value = discount
      discountError.value = ''
      return
    }

    // Gọi API validate nếu không tìm thấy trong danh sách có sẵn
    const response = await axios.post('/api/discounts/validate', {
      code: selectedDiscountCode.value,
      orderValue: subtotal.value,
    })

    appliedDiscount.value = response.data.discount
    discountError.value = ''
  } catch (error) {
    appliedDiscount.value = null
    discountError.value = error.response?.data?.message || 'Mã giảm giá không hợp lệ'
  }
}

const removeDiscount = () => {
  appliedDiscount.value = null
  selectedDiscountCode.value = ''
  discountError.value = ''
}

// Helper function để xử lý thanh toán COD
const processCODPayment = async (orderData) => {
  try {
    // Kiểm tra số lượng tồn kho trước khi đặt hàng
    try {
      await axios.post('/api/orders/check-inventory', orderData)
    } catch (inventoryError) {
      if (inventoryError.response?.data?.message) {
        message.error('Lỗi kiểm tra tồn kho: ' + inventoryError.response.data.message)
      } else {
        message.error('Không đủ số lượng sản phẩm để đặt hàng')
      }
      return
    }

    const orderResponse = await axios.post('/api/orders', orderData)
    const order = orderResponse.data

    // Hiển thị thông báo thành công và redirect
    message.success('Đặt hàng thành công! Chúng tôi sẽ liên hệ để xác nhận và giao hàng.')

    // Xóa những sản phẩm đã đặt hàng khỏi giỏ hàng
    removeOrdered(cartItems.value)
    // Redirect to order detail page
    router.push(`/order/${order.id}`)
  } catch (error) {
    console.error('Error creating COD order:', error)
    console.error('Error response:', error.response?.data)
    console.error('Error status:', error.response?.status)

    if (error.response?.data?.message) {
      message.error('Lỗi đặt hàng: ' + error.response.data.message)
    } else {
      message.error('Có lỗi xảy ra khi đặt hàng. Vui lòng thử lại.')
    }
    throw error
  }
}

const handleCheckout = async () => {
  if (!selectedShippingObject.value) {
    message.error('Vui lòng chọn phương thức vận chuyển')
    return
  }

  if (!selectedPaymentObject.value) {
    message.error('Vui lòng chọn phương thức thanh toán')
    return
  }

  try {
    // Kiểm tra xem có sản phẩm nào trong giỏ hàng không
    if (cartItems.value.length === 0) {
      message.error('Giỏ hàng trống. Vui lòng thêm sản phẩm vào giỏ hàng.')
      return
    }

    // Lấy thông tin user hiện tại
    let userResponse
    try {
      userResponse = await axios.get('/api/info-account')
      console.log('User response:', userResponse.data)

      if (!userResponse.data.account) {
        message.error(userResponse.data.message || 'Vui lòng đăng nhập để đặt hàng.')
        // Redirect to login page
        router.push('/login')
        return
      }
    } catch (error) {
      console.error('Error fetching user info:', error)
      message.error('Lỗi khi kiểm tra thông tin đăng nhập. Vui lòng thử lại.')
      return
    }

    // Kiểm tra địa chỉ giao hàng (không bắt buộc)
    if (!userResponse.data.account.address) {
      console.log('User chưa cập nhật địa chỉ giao hàng')
    }

    // Lấy shop ID từ sản phẩm đầu tiên trong giỏ hàng
    const firstItem = cartItems.value[0]
    let shopId = null

    // Lấy shop ID từ product
    if (firstItem.product && firstItem.product.shopId) {
      shopId = firstItem.product.shopId
    } else {
      // Nếu không có shop ID, sử dụng shop ID cố định (backend sẽ tự tạo shop demo)
      console.log('Không tìm thấy shop ID trong cart items, sử dụng shop ID cố định...')
      shopId = '550e8400-e29b-41d4-a716-446655440001'
      console.log('Sử dụng shop ID cố định:', shopId)
    }

    // Validate dữ liệu trước khi gửi
    if (!userResponse.data.account.id) {
      message.error('Không thể lấy thông tin user. Vui lòng đăng nhập lại.')
      return
    }

    if (!selectedShippingObject.value?.id) {
      message.error('Vui lòng chọn phương thức vận chuyển.')
      return
    }

    if (!selectedPaymentObject.value?.id) {
      message.error('Vui lòng chọn phương thức thanh toán.')
      return
    }

    // Tạo order data với thông tin thực
    const orderData = {
      accountId: userResponse.data.account.id,
      shopId: shopId,
      shippingId: selectedShippingObject.value.id,
      paymentId: selectedPaymentObject.value.id,
      totalAmount: finalTotal.value,
      discountAmount: discountAmount.value,
      orderStatus: 'PENDING',
      shippingAddress: userResponse.data.account.address || 'Chưa cập nhật địa chỉ',
      orderItems: cartItems.value.map((item) => ({
        productVariantId: item.variant ? item.variant.id : null,
        quantity: item.quantity,
        productPrice: item.price,
        discountApplied: 0,
      })),
      discountCode: appliedDiscount.value ? appliedDiscount.value.name : null,
    }

    // Log để debug
    console.log('User ID:', userResponse.data.account.id)
    console.log('Shop ID:', shopId)
    console.log('Shipping ID:', selectedShippingObject.value.id)
    console.log('Payment ID:', selectedPaymentObject.value.id)
    console.log('Cart items:', cartItems.value)

    console.log('Order data to send:', JSON.stringify(orderData, null, 2))
    console.log('=== DEBUG CART VALUES ===')
    console.log('total.value:', total.value)
    console.log('subtotal.value:', subtotal.value)
    console.log('shippingCost.value:', shippingCost.value)
    console.log('discountAmount.value:', discountAmount.value)
    console.log('finalTotal.value:', finalTotal.value)
    console.log('cartItems.value:', cartItems.value)
    console.log('=== END DEBUG ===')

    // Xử lý thanh toán dựa trên phương thức thanh toán
    if (selectedPaymentObject.value.paymentCode === 'PAYOS') {
      // PayOS - Kiểm tra tồn kho trước khi tạo payment URL
      try {
        // Kiểm tra số lượng tồn kho trước khi tạo payment URL
        await axios.post('/api/orders/check-inventory', orderData)
      } catch (inventoryError) {
        if (inventoryError.response?.data?.message) {
          message.error('Lỗi kiểm tra tồn kho: ' + inventoryError.response.data.message)
        } else {
          message.error('Không đủ số lượng sản phẩm để đặt hàng')
        }
        return
      }

      // PayOS - Tạo payment URL trước, không tạo đơn hàng
      try {
        const payosResponse = await axios.post('/api/payos/create-payment', orderData)

        if (payosResponse.data.success) {
          // Lưu orderCode và amount để verify sau này
          localStorage.setItem('pendingOrderCode', payosResponse.data.orderCode)
          localStorage.setItem('pendingOrderAmount', finalTotal.value.toString())

          // Hiển thị thông báo chuyển hướng thanh toán
          message.info('Đang chuyển hướng đến trang thanh toán PayOS...')

          // Redirect to PayOS payment page
          window.location.href = payosResponse.data.paymentUrl
        } else {
          // Kiểm tra xem có phải lỗi cấu hình PayOS không
          if (payosResponse.data.errorCode === 'PAYOS_ERROR') {
            message.warning('PayOS chưa được cấu hình. Vui lòng chọn phương thức thanh toán khác.')
            // Tự động chuyển về COD
            const codPayment = payments.value.find((p) => p.paymentCode === 'COD')
            if (codPayment) {
              selectedPayment.value = codPayment.id
              message.info('Đã chuyển về thanh toán khi nhận hàng (COD)')
              // Tiếp tục xử lý với COD
              await processCODPayment(orderData)
            }
          } else {
            message.error('Lỗi tạo URL thanh toán: ' + payosResponse.data.message)
          }
        }
      } catch (error) {
        console.error('Error creating PayOS payment:', error)
        message.error('Lỗi khi tạo thanh toán PayOS: ' + (error.response?.data?.message || error.message))
        // Fallback về COD nếu có lỗi
        const codPayment = payments.value.find((p) => p.paymentCode === 'COD')
        if (codPayment) {
          selectedPayment.value = codPayment.id
          message.info('Đã chuyển về thanh toán khi nhận hàng (COD)')
          await processCODPayment(orderData)
        }
      }
    } else {
      // COD hoặc các phương thức khác - Tạo đơn hàng trực tiếp
      await processCODPayment(orderData)
    }
  } catch (error) {
    console.error('Error during checkout:', error)
    const errorMessage = error.response?.data?.message || error.message || 'Có lỗi xảy ra khi đặt hàng'
    message.error('Có lỗi xảy ra khi đặt hàng: ' + errorMessage)
  }
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

// Helper methods để xử lý cấu trúc dữ liệu cart mới
const getProductImage = (item) => {
  // Cấu trúc mới: item.product.mainImage hoặc item.product.productImage
  if (item.product && item.product.mainImage && item.product.mainImage.trim() !== '') {
    return item.product.mainImage
  }
  if (item.product && item.product.productImage && item.product.productImage.trim() !== '') {
    return item.product.productImage
  }
  // Fallback
  return null
}

const getProductName = (item) => {
  // Cấu trúc mới: item.product.name
  if (item.product && item.product.name) {
    return item.product.name
  }
  return 'Sản phẩm không xác định'
}

const getProductId = (item) => {
  // Cấu trúc mới: item.product.id
  if (item.product && item.product.id) {
    return item.product.id
  }
  return ''
}

const getItemPrice = (item) => {
  console.log('Cart item:', item)
  console.log('Item variant:', item.variant)
  console.log('Item price:', item.price)

  // Ưu tiên giá từ variant nếu có, nếu không thì lấy giá từ item.price
  if (item.variant && item.variant.price && item.variant.price > 0) {
    console.log('Using variant price:', item.variant.price)
    return item.variant.price
  }
  const finalPrice = item.price && item.price > 0 ? item.price : 0
  console.log('Using item price:', finalPrice)
  return finalPrice
}

const getVariantDetails = (item) => {
  // Cấu trúc mới: item.variant có thông tin chi tiết
  if (item.variant) {
    const details = []
    if (item.variant.variantName) details.push(item.variant.variantName)
    if (item.variant.variantValue) details.push(item.variant.variantValue)
    if (item.variant.color) details.push(`Màu: ${item.variant.color}`)
    if (item.variant.size) details.push(`Kích thước: ${item.variant.size}`)
    return details.join(' - ')
  }
  return null
}

const handleImageError = (event) => {
  event.target.src = '/placeholder-image.jpg'
}

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedItems.value = []
  } else {
    selectedItems.value = cartItems.value.map(item => item.id)
  }
}

const toggleSelectItem = (itemId) => {
  if (selectedItems.value.includes(itemId)) {
    selectedItems.value = selectedItems.value.filter(id => id !== itemId)
  } else {
    selectedItems.value.push(itemId)
  }
}

const deleteSelectedItems = () => {
  if (selectedItems.value.length === 0) {
    message.warning('Bạn chưa chọn sản phẩm nào để xóa.')
    return
  }

  // Xóa từng sản phẩm đã chọn
  selectedItems.value.forEach(itemId => {
    removeItem(itemId)
  })

  message.success(`Đã xóa ${selectedItems.value.length} sản phẩm thành công.`)
  selectedItems.value = [] // Xóa danh sách ID đã chọn
}

const selectDiscount = (discount) => {
  selectedDiscountCode.value = discount.name
  showDiscountDialog.value = false
  applyDiscount()
}

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('vi-VN', { month: 'numeric', day: 'numeric', hour: 'numeric', minute: 'numeric' })
}

onMounted(() => {
  fetchShippings()
  fetchPayments()
  fetchDiscounts()
})
</script>

<style scoped>
/* Custom scrollbar cho mã giảm giá */
.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 2px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 2px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Hiệu ứng vé */
.voucher-card {
  position: relative;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 1px solid #bae6fd;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.voucher-card:hover {
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

.voucher-card::before {
  content: '';
  position: absolute;
  left: -1px;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  background: white;
  border: 1px solid #bae6fd;
  border-radius: 50%;
}

.voucher-card::after {
  content: '';
  position: absolute;
  right: -1px;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  background: white;
  border: 1px solid #bae6fd;
  border-radius: 50%;
}

/* New styles for Shopee voucher */
.voucher-card-shopee {
  position: relative;
  display: flex;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 12px;
  border: 1px solid #e5e7eb;
}

.voucher-left {
  position: relative;
  width: 120px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  border-radius: 8px 0 0 8px;
}

.voucher-left::after {
  content: '';
  position: absolute;
  right: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 0 0 4px #f0f0f0;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.shop-logo {
  width: 32px;
  height: 32px;
  background: white;
  color: #3b82f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.brand-name {
  font-size: 12px;
  font-weight: bold;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.voucher-right {
  flex: 1;
  padding: 16px;
  background: white;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border-top: 1px solid #e5e7eb;
  border-right: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
  border-radius: 0 8px 8px 0;
  min-width: 0;
}

.discount-info {
  margin-bottom: 8px;
  min-width: 0;
  flex: 1;
}

.discount-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
  line-height: 1.2;
}

.discount-condition {
  font-size: 12px;
  color: #666;
  line-height: 1.3;
}

.apply-btn {
  background: #3b82f6;
  color: white;
  padding: 6px 12px;
  border: none;
  font-size: 12px;
  font-weight: bold;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s ease;
  white-space: nowrap;
}

.apply-btn:hover {
  background: #2563eb;
}

.validity-info {
  font-size: 11px;
  color: #999;
  line-height: 1.2;
}
</style>
