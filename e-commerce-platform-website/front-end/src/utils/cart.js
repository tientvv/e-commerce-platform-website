// Utility để quản lý giỏ hàng bằng cookie

const CART_COOKIE_NAME = 'ecpw_cart'
const CART_EXPIRY_DAYS = 30

// Lấy giỏ hàng từ cookie
export const getCart = () => {
  try {
    const cartData = getCookie(CART_COOKIE_NAME)
    if (!cartData) return []

    const cart = JSON.parse(decodeURIComponent(cartData))
    return Array.isArray(cart) ? cart : []
  } catch (error) {
    console.error('Error parsing cart from cookie:', error)
    return []
  }
}

// Lưu giỏ hàng vào cookie
export const saveCart = (cart) => {
  try {
    const cartData = JSON.stringify(cart)
    setCookie(CART_COOKIE_NAME, cartData, CART_EXPIRY_DAYS)
  } catch (error) {
    console.error('Error saving cart to cookie:', error)
  }
}

// Thêm sản phẩm vào giỏ hàng
export const addToCart = (product, variant = null, quantity = 1) => {
  const cart = getCart()

  // Tạo cart item
  const cartItem = {
    id: generateCartItemId(product.id, variant?.id),
    product: {
      id: product.id,
      name: product.name,
      mainImage: product.mainImage || product.productImage,
      brand: product.brand,
      shopName: product.shopName,
      shopId: product.shopId,
    },
    variant: variant
      ? {
          id: variant.id,
          name: variant.name,
          price: variant.price,
          variantName: variant.variantName,
          variantValue: variant.variantValue,
          color: variant.color,
          size: variant.size,
        }
      : null,
    price: variant?.price || product.minPrice || 0,
    quantity: quantity,
    addedAt: new Date().toISOString(),
  }

  // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
  const existingItemIndex = cart.findIndex((item) => item.id === cartItem.id)

  if (existingItemIndex >= 0) {
    // Cập nhật số lượng nếu đã tồn tại
    cart[existingItemIndex].quantity += quantity
  } else {
    // Thêm mới nếu chưa tồn tại
    cart.push(cartItem)
  }

  saveCart(cart)
  return cart
}

// Cập nhật số lượng sản phẩm trong giỏ hàng
export const updateCartItemQuantity = (itemId, quantity) => {
  const cart = getCart()
  const itemIndex = cart.findIndex((item) => item.id === itemId)

  if (itemIndex >= 0) {
    if (quantity <= 0) {
      // Xóa item nếu số lượng <= 0
      cart.splice(itemIndex, 1)
    } else {
      // Cập nhật số lượng
      cart[itemIndex].quantity = quantity
    }
    saveCart(cart)
  }

  return cart
}

// Xóa sản phẩm khỏi giỏ hàng
export const removeFromCart = (itemId) => {
  const cart = getCart()
  const filteredCart = cart.filter((item) => item.id !== itemId)
  saveCart(filteredCart)
  return filteredCart
}

// Xóa toàn bộ giỏ hàng
export const clearCart = () => {
  saveCart([])
  return []
}

// Xóa những sản phẩm đã đặt hàng khỏi giỏ hàng
export const removeOrderedItems = (orderedItems) => {
  const cart = getCart()

  // Tạo danh sách ID của những sản phẩm đã đặt hàng
  const orderedItemIds = orderedItems.map((item) => item.id)

  // Lọc ra những sản phẩm chưa đặt hàng
  const remainingItems = cart.filter((item) => !orderedItemIds.includes(item.id))

  saveCart(remainingItems)
  return remainingItems
}

// Lấy tổng số lượng sản phẩm trong giỏ hàng
export const getCartItemCount = () => {
  const cart = getCart()
  return cart.length // Chỉ đếm số lượng sản phẩm, không tính số lượng của từng sản phẩm
}

// Lấy tổng giá trị giỏ hàng
export const getCartTotal = () => {
  const cart = getCart()
  return cart.reduce((total, item) => total + item.price * item.quantity, 0)
}

// Tạo ID duy nhất cho cart item
const generateCartItemId = (productId, variantId) => {
  return variantId ? `${productId}-${variantId}` : productId
}

// Utility functions để làm việc với cookie
const setCookie = (name, value, days) => {
  const expires = new Date()
  expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000)
  document.cookie = `${name}=${encodeURIComponent(value)};expires=${expires.toUTCString()};path=/`
}

const getCookie = (name) => {
  const nameEQ = name + '='
  const ca = document.cookie.split(';')
  for (let i = 0; i < ca.length; i++) {
    let c = ca[i]
    while (c.charAt(0) === ' ') c = c.substring(1, c.length)
    if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length)
  }
  return null
}
