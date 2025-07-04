<template>
  <BaseModal
    :show="show"
    :title="isEdit ? 'Chỉnh sửa danh mục' : 'Thêm danh mục mới'"
    @close="$emit('close')"
  >
    <BaseForm @submit="handleSubmit">
      <BaseInput
        id="categoryName"
        label="Tên danh mục"
        :model-value="formData.name"
        placeholder="Nhập tên danh mục"
        required
        @update:model-value="updateField('name', $event)"
      />

      <BaseSelect
        id="parentCategory"
        label="Danh mục cha"
        :model-value="formData.parentId"
        :options="parentOptions"
        placeholder="-- Chọn danh mục cha (tuỳ chọn) --"
        @update:model-value="updateField('parentId', $event)"
      />

      <BaseImageUpload
        id="categoryImage"
        label="Hình ảnh danh mục"
        :model-value="formData.categoryImage"
        :file="formData.categoryImageFile"
        placeholder="Chọn hình ảnh danh mục"
        @update:model-value="updateField('categoryImage', $event)"
        @update:file="updateField('categoryImageFile', $event)"
      />
    </BaseForm>

    <template #footer>
      <BaseButton variant="secondary" @click="$emit('close')"> Huỷ </BaseButton>
      <BaseButton type="submit" :loading="loading" @click="handleSubmit">
        {{ isEdit ? 'Cập nhật' : 'Thêm mới' }}
      </BaseButton>
    </template>
  </BaseModal>
</template>

<script setup>
import { computed } from 'vue'
import BaseModal from '@/components/base/BaseModal.vue'
import BaseForm from '@/components/base/BaseForm.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import BaseImageUpload from '@/components/base/BaseImageUpload.vue'
import BaseButton from '@/components/base/BaseButton.vue'

const props = defineProps({
  show: Boolean,
  formData: Object,
  availableParents: Array,
  isEdit: Boolean,
  loading: Boolean,
})

const emit = defineEmits(['close', 'submit', 'update:formData'])

const parentOptions = computed(() =>
  props.availableParents.map((cat) => ({
    value: cat.id,
    label: cat.name,
  })),
)

const updateField = (field, value) => {
  emit('update:formData', { ...props.formData, [field]: value })
}

const handleSubmit = () => {
  emit('submit')
}
</script>
