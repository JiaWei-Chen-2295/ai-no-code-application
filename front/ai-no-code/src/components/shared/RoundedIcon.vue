<template>
  <div class="rounded-icon-container" :style="containerStyle">
    <component 
      :is="icon" 
      class="rounded-icon" 
      :style="iconStyle" 
    />
  </div>
</template>

<script setup lang="ts">
import { VNode } from 'vue';

interface Props {
  icon: VNode;
  size?: number;
  borderRadius?: string;
  backgroundColor?: string;
}

const props = withDefaults(defineProps<Props>(), {
  size: 24,
  borderRadius: '4px',
  backgroundColor: 'transparent'
});

const containerStyle = {
  width: `${props.size}px`,
  height: `${props.size}px`,
  borderRadius: props.borderRadius,
  backgroundColor: props.backgroundColor,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  overflow: 'hidden',
  position: 'relative'
};

const iconStyle = {
  fontSize: `${props.size * 0.7}px`, // Make icon slightly smaller to avoid touching edges
  color: 'currentColor',
  flexShrink: 0,
  position: 'relative',
  zIndex: 1
};
</script>

<style scoped>
.rounded-icon-container {
  box-sizing: border-box;
}

.rounded-icon {
  pointer-events: none;
}
</style>