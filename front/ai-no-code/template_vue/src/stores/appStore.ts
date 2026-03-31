import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const title = ref('Vue Template')

  function setTitle(newTitle: string) {
    title.value = newTitle
  }

  return {
    title,
    setTitle,
  }
})

