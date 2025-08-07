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
              <h2 class="text-lg font-medium text-gray-900">Sản phẩm ({{ cartItems.length }})</h2>
            </div>
            <div class="divide-y divide-gray-200">
              <div v-for="item in cartItems" :key="item.id" class="p-6 flex">
                <!-- Product Image -->
                <div class="flex-shrink-0 w-24 h-24">
                  <img
                    :src="getProductImage(item)"
                    :alt="getProductName(item)"
                    class="w-full h-full object-cover rounded-lg"
                  />
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
                      <p v-if="getVariantName(item)" class="mt-1 text-sm text-gray-500">
                        <span class="font-medium">Biến thể:</span> {{ getVariantName(item) }}
                      </p>
                      <p v-if="getVariantDetails(item)" class="mt-1 text-sm text-gray-500">
                        <span class="font-medium">Chi tiết:</span> {{ getVariantDetails(item) }}
                      </p>
                      <p class="mt-1 text-sm text-gray-500">Giá: {{ formatPrice(item.price) }}</p>
                    </div>
                    <div class="text-right">
                      <p class="text-lg font-medium text-gray-900">
                        {{ formatPrice(item.price * item.quantity) }}
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
                <p>Tạm tính</p>
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
                  <n-select
                    v-model:value="selectedDiscountCode"
                    :options="discountOptions"
                    placeholder="Chọn mã giảm giá"
                    :disabled="!!appliedDiscount"
                    @update:value="applyDiscount"
                    clearable
                  />
                  <button
                    v-if="appliedDiscount"
                    @click="removeDiscount"
                    class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
                  >
                    Hủy
                  </button>
                </div>
                <div v-if="appliedDiscount" class="mt-3 p-3 bg-green-50 border border-green-200 rounded-lg">
                  <div class="flex items-center justify-between">
                    <div>
                      <p class="text-sm font-medium text-green-800">Mã giảm giá: {{ appliedDiscount.name }}</p>
                      <p class="text-xs text-green-600">{{ appliedDiscount.description }}</p>
                    </div>
                    <span class="text-sm font-bold text-green-800">-{{ formatPrice(discountAmount) }}</span>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { ChevronRight, ShoppingCart, Minus, Plus } from 'lucide-vue-next'
import { NSelect, useMessage } from 'naive-ui'
import { useCart } from '../composables/useCart'

import axios from '../utils/axios'
import NavbarView from './components/NavbarView.vue'
import FooterView from './components/FooterView.vue'

const message = useMessage()

const router = useRouter()
const { cartItems, updateQuantity, removeItem, removeOrdered, total } = useCart()

// State
const shippings = ref([])
const selectedShipping = ref(null)
const payments = ref([])
const selectedPayment = ref(null)
const availableDiscounts = ref([])
const selectedDiscountCode = ref('')
const appliedDiscount = ref(null)
const discountError = ref('')

// Computed
const subtotal = computed(() => {
  return total.value
})

const shippingCost = computed(() => {
  return selectedShippingObject.value ? selectedShippingObject.value.price : 0
})

const discountAmount = computed(() => {
  if (!appliedDiscount.value) return 0

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

const discountOptions = computed(() => {
  return availableDiscounts.value.map((discount) => ({
    label: discount.description ? `${discount.name} - ${discount.description}` : discount.name,
    value: discount.name,
  }))
})

// Methods
const fetchShippings = async () => {
  try {
    const response = await axios.get('/api/shippings')
    shippings.value = response.data.shippings || []

    // Mặc định chọn phương thức vận chuyển đầu tiên
    if (shippings.value.length > 0) {
      selectedShipping.value = shippings.value[0].id
    }
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

    // Mặc định chọn mã giảm giá đầu tiên nếu có
    if (availableDiscounts.value.length > 0) {
      const firstDiscount = availableDiscounts.value[0]
      console.log('First discount object:', firstDiscount)
      selectedDiscountCode.value = firstDiscount.name || firstDiscount.code || ''
      console.log('Selected discount code:', selectedDiscountCode.value)

      // Tự động áp dụng mã giảm giá đầu tiên
      if (selectedDiscountCode.value) {
        console.log('Auto-applying first discount...')
        await applyDiscount()
      }
    } else {
      console.log('No discounts available from API')
    }
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
      },
      {
        id: '2',
        name: 'SAVE50K',
        description: 'Giảm 50k cho đơn hàng từ 300k',
        discountType: 'FIXED',
        discountValue: 50000,
        minOrderValue: 300000,
      },
    ]

    // Mặc định chọn mã đầu tiên
    if (availableDiscounts.value.length > 0) {
      selectedDiscountCode.value = availableDiscounts.value[0].name
      console.log('Selected sample discount:', selectedDiscountCode.value)
      await applyDiscount()
    }
  }
}

const applyDiscount = async () => {
  if (!selectedDiscountCode.value) {
    appliedDiscount.value = null
    discountError.value = ''
    return
  }

  try {
    // Kiểm tra xem có phải mã giảm giá mẫu không
    const sampleDiscount = availableDiscounts.value.find((d) => d.name === selectedDiscountCode.value)
    if (sampleDiscount) {
      // Kiểm tra điều kiện áp dụng
      if (subtotal.value < sampleDiscount.minOrderValue) {
        discountError.value = `Đơn hàng tối thiểu ${formatPrice(sampleDiscount.minOrderValue)} để áp dụng mã này`
        appliedDiscount.value = null
        return
      }

      appliedDiscount.value = sampleDiscount
      discountError.value = ''
      return
    }

    // Gọi API validate
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

    // Thử lấy từ product
    if (firstItem.product && firstItem.product.shopId) {
      shopId = firstItem.product.shopId
    } else if (firstItem.shopId) {
      shopId = firstItem.shopId
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

// Helper methods để xử lý cấu trúc dữ liệu cart cũ và mới
const getProductImage = (item) => {
  // Cấu trúc mới: item.product.mainImage
  if (item.product && item.product.mainImage) {
    return item.product.mainImage
  }
  // Cấu trúc cũ: item.productImage
  if (item.productImage) {
    return item.productImage
  }
  return '/placeholder-image.jpg'
}

const getProductName = (item) => {
  // Cấu trúc mới: item.product.name
  if (item.product && item.product.name) {
    return item.product.name
  }
  // Cấu trúc cũ: item.productName
  if (item.productName) {
    return item.productName
  }
  return 'Sản phẩm không xác định'
}

const getProductId = (item) => {
  // Cấu trúc mới: item.product.id
  if (item.product && item.product.id) {
    return item.product.id
  }
  // Cấu trúc cũ: item.productId
  if (item.productId) {
    return item.productId
  }
  return ''
}

const getVariantName = (item) => {
  console.log('Getting variant name for item:', item)

  // Cấu trúc mới: item.variant.name
  if (item.variant && item.variant.name) {
    console.log('Found variant name (new structure):', item.variant.name)
    return item.variant.name
  }
  // Cấu trúc cũ: item.variantName
  if (item.variantName) {
    console.log('Found variant name (old structure):', item.variantName)
    return item.variantName
  }
  console.log('No variant name found')
  return null
}

const getVariantDetails = (item) => {
  console.log('Getting variant details for item:', item)

  // Cấu trúc mới: item.variant có thông tin chi tiết
  if (item.variant) {
    const details = []
    if (item.variant.variantName) details.push(item.variant.variantName)
    if (item.variant.variantValue) details.push(item.variant.variantValue)
    if (item.variant.color) details.push(`Màu: ${item.variant.color}`)
    if (item.variant.size) details.push(`Kích thước: ${item.variant.size}`)
    const result = details.join(' - ')
    console.log('Variant details result:', result)
    return result
  }

  // Cấu trúc cũ: item có thông tin variant
  if (item.variantName && item.variantValue) {
    const result = `${item.variantName}: ${item.variantValue}`
    console.log('Old variant details result:', result)
    return result
  }

  console.log('No variant details found')
  return null
}

onMounted(() => {
  fetchShippings()
  fetchPayments()
  fetchDiscounts()
})
</script>
