<template>
  <BaseModal :show="show" :title="title" size="small" @close="$emit('cancel')">
    <div class="confirm-content">
      <div class="icon-container" :class="type">
        <i :class="iconClass"></i>
      </div>
      <p class="message">{{ message }}</p>
    </div>

    <template #footer>
      <BaseButton variant="secondary" @click="$emit('cancel')">
        {{ cancelText }}
      </BaseButton>
      <BaseButton :variant="confirmVariant" @click="$emit('confirm')">
        {{ confirmText }}
      </BaseButton>
    </template>
  </BaseModal>
</template>

<script setup>
import { computed } from 'vue'
import BaseModal from '@/components/base/BaseModal.vue'
import BaseButton from '@/components/base/BaseButton.vue'

const props = defineProps({
  show: Boolean,
  title: { type: String, default: 'Xác nhận' },
  message: { type: String, required: true },
  confirmText: { type: String, default: 'Xác nhận' },
  cancelText: { type: String, default: 'Hủy' },
  type: {
    type: String,
    default: 'warning',
    validator: (value) => ['warning', 'danger', 'info'].includes(value),
  },
})

defineEmits(['confirm', 'cancel'])

const iconClass = computed(() => {
  switch (props.type) {
    case 'danger':
      return 'fas fa-exclamation-triangle'
    case 'info':
      return 'fas fa-info-circle'
    default:
      return 'fas fa-question-circle'
  }
})

const confirmVariant = computed(() => {
  switch (props.type) {
    case 'danger':
      return 'danger'
    case 'info':
      return 'primary'
    default:
      return 'primary'
  }
})
</script>

<style scoped>
.confirm-content {
  text-align: center;
  padding: 1rem 0;
}

.icon-container {
  margin-bottom: 1rem;
}

.icon-container i {
  font-size: 3rem;
}

.icon-container.warning i {
  color: #ffc107;
}
.icon-container.danger i {
  color: #dc3545;
}
.icon-container.info i {
  color: #17a2b8;
}

.message {
  font-size: 1rem;
  color: #495057;
  margin: 0;
  line-height: 1.5;
}
</style>
