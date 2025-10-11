<template>
  <div class="global-footer">
    <div class="footer-content">
      <p>Powered by JavierChen (https://www.javierchen.fun)</p>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.global-footer {
  text-align: center;
  padding: var(--spacing-6) var(--spacing-12);
  background: var(--gray-700);
  border-top: 3px solid var(--accent-500);
  color: white;
  position: relative;
  overflow: hidden;
}

.global-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, transparent 0%, rgba(255, 220, 0, 0.1) 50%, transparent 100%);
  pointer-events: none;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  z-index: 2;
  position: relative;
}

.global-footer p {
  margin: 0;
  color: rgba(255, 255, 255, 0.8);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  transition: var(--transition-colors);
}

.global-footer p:hover {
  color: var(--accent-400);
}

@media (max-width: 768px) {
  .global-footer {
    padding: var(--spacing-4) var(--spacing-6);
  }

  .global-footer p {
    font-size: var(--text-xs);
  }
}
</style>