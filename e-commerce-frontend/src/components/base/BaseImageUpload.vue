<template>
  <div class="form-group">
    <label v-if="label">{{ label }}</label>
    <div class="image-upload-container">
      <input :id="id" type="file" accept="image/*" @change="handleImageChange" class="file-input" />
      <div v-if="previewImage" class="image-preview">
        <img :src="previewImage" alt="Preview" />
        <button type="button" @click="removeImage" class="btn-remove-image">
          <i class="fas fa-times"></i>
        </button>
      </div>
      <div v-else class="upload-placeholder">
        <i class="fas fa-cloud-upload-alt"></i>
        <p>{{ placeholder || 'Chọn hình ảnh' }}</p>
      </div>
    </div>
    <small v-if="error" class="error-text">{{ error }}</small>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  id: String,
  label: String,
  modelValue: String,
  file: File,
  placeholder: String,
  error: String,
})

const emit = defineEmits(['update:modelValue', 'update:file'])

const previewImage = ref('')

watch(
  () => props.modelValue,
  (newImage) => {
    if (newImage && !props.file) {
      previewImage.value = newImage
    }
  },
  { immediate: true },
)

const handleImageChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    emit('update:file', file)

    const reader = new FileReader()
    reader.onload = (e) => {
      previewImage.value = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

const removeImage = () => {
  previewImage.value = ''
  emit('update:modelValue', null)
  emit('update:file', null)

  const fileInput = document.getElementById(props.id)
  if (fileInput) fileInput.value = ''
}
</script>

<style scoped>
.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #495057;
}

.image-upload-container {
  border: 2px dashed #ced4da;
  border-radius: 4px;
  padding: 1rem;
  text-align: center;
  position: relative;
}

.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.upload-placeholder {
  color: #6c757d;
}

.upload-placeholder i {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  display: block;
}

.image-preview {
  position: relative;
  display: inline-block;
}

.image-preview img {
  max-width: 200px;
  max-height: 150px;
  border-radius: 4px;
  object-fit: cover;
}

.btn-remove-image {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 50%;
  width: 25px;
  height: 25px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-text {
  color: #dc3545;
  font-size: 0.875rem;
  margin-top: 0.25rem;
  display: block;
}
</style>
