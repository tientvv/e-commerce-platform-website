import { ref, computed, watch } from 'vue'
import {
  getCart,
  saveCart,
  addToCart,
  updateCartItemQuantity,
  removeFromCart,
  clearCart,
  removeOrderedItems,
  getCartItemCount,
  getCartTotal,
} from '../utils/cart.js'

// Reactive cart state
const cartItems = ref([])
const cartItemCount = ref(0)
const cartTotal = ref(0)

// Load cart from cookie on initialization
const loadCart = () => {
  const cart = getCart()
  console.log('Loading cart from cookie:', cart)
  cartItems.value = cart
  updateCartStats()
}

// Update cart statistics
const updateCartStats = () => {
  cartItemCount.value = getCartItemCount()
  cartTotal.value = getCartTotal()
}

// Watch for cart changes and save to cookie
watch(
  cartItems,
  (newCart) => {
    saveCart(newCart)
    updateCartStats()
  },
  { deep: true },
)

// Cart actions
const addItem = (product, variant = null, quantity = 1) => {
  console.log('Adding item to cart:', { product, variant, quantity })
  const newCart = addToCart(product, variant, quantity)
  console.log('New cart after adding item:', newCart)
  cartItems.value = newCart
  return newCart
}

const updateQuantity = (itemId, quantity) => {
  const newCart = updateCartItemQuantity(itemId, quantity)
  cartItems.value = newCart
  return newCart
}

const removeItem = (itemId) => {
  const newCart = removeFromCart(itemId)
  cartItems.value = newCart
  return newCart
}

const clear = () => {
  const newCart = clearCart()
  cartItems.value = newCart
  return newCart
}

const removeOrdered = (orderedItems) => {
  const newCart = removeOrderedItems(orderedItems)
  cartItems.value = newCart
  return newCart
}

// Computed properties
const isEmpty = computed(() => cartItems.value.length === 0)
const itemCount = computed(() => cartItemCount.value)
const total = computed(() => cartTotal.value)

// Initialize cart
loadCart()

export function useCart() {
  return {
    // State
    cartItems: computed(() => cartItems.value),
    isEmpty,
    itemCount,
    total,

    // Actions
    addItem,
    updateQuantity,
    removeItem,
    clear,
    removeOrdered,
    loadCart,
    getCartTotal,
  }
}
