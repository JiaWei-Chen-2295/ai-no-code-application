import React from 'react'
import { useLaunch } from '@tarojs/taro'
import { UserProvider } from './store/user'
import { initSystemInfo } from './utils/system'
import { ErrorBoundary } from './components/ErrorBoundary'

function App({ children }: { children: React.ReactNode }) {
  useLaunch(() => {
    console.log('App launched, initializing...')
    try {
      initSystemInfo()
      console.log('App initialization completed')
    } catch (error) {
      console.error('App initialization failed:', error)
    }
  })

  return (
    <UserProvider>
      <ErrorBoundary>
        {children}
      </ErrorBoundary>
    </UserProvider>
  )
}

export default App
