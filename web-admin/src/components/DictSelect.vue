<template>
  <el-select
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :placeholder="placeholder"
    :disabled="disabled"
    :clearable="clearable"
    :style="style"
    @focus="loadDict"
  >
    <el-option
      v-for="item in dictOptions"
      :key="item.dictCode"
      :label="item.dictValue"
      :value="item.dictCode"
    />
  </el-select>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getByType } from '../api/dict'

const props = defineProps({
  modelValue: [String, Number],
  dictType: {
    type: String,
    required: true
  },
  placeholder: {
    type: String,
    default: '请选择'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  clearable: {
    type: Boolean,
    default: true
  },
  style: {
    type: Object,
    default: () => ({})
  }
})

defineEmits(['update:modelValue'])

const dictOptions = ref([])

const loadDict = async () => {
  if (dictOptions.value.length > 0) return
  try {
    const res = await getByType(props.dictType)
    dictOptions.value = res.data || []
  } catch (e) {
    console.error('加载字典数据失败', e)
  }
}

onMounted(() => {
  loadDict()
})
</script>