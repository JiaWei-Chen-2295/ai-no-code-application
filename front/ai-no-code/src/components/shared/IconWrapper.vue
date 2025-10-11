<template>
  <div 
    class="icon-wrapper" 
    :class="{ 'rounded-corners': rounded }"
    :style="wrapperStyle"
  >
    <img 
      v-if="src" 
      :src="src" 
      :alt="alt"
      class="icon-image"
      :style="imageStyle"
    />
    <slot v-else class="icon-slot" :style="iconStyle" />
  </div>
</template>

<script setup lang="ts">
interface Props {
  src?: string;
  alt?: string;
  size?: number;
  borderRadius?: string;
  rounded?: boolean;
  backgroundColor?: string;
}

const props = withDefaults(defineProps<Props>(), {
  size: 24,
  borderRadius: '4px',
  rounded: true,
  backgroundColor: 'transparent'
});

const wrapperStyle = {
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

const imageStyle = {
  width: '100%',
  height: '100%',
  objectFit: 'cover'
};
</script>

<style scoped>
.icon-wrapper {
  box-sizing: border-box;
}

.icon-wrapper.rounded-corners {
  border-radius: v-bind('props.borderRadius');
}

.icon-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.icon-slot {
  pointer-events: none;
}
</style>