import React, { Component, ErrorInfo, ReactNode } from 'react'
import { View, Text } from '@tarojs/components'

interface Props {
  children: ReactNode
  fallback?: ReactNode
}

interface State {
  hasError: boolean
  error: Error | null
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error }
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo)
  }

  render() {
    if (this.state.hasError) {
      return this.props.fallback || (
        <View style={{ padding: '20px', textAlign: 'center' }}>
          <Text style={{ color: 'red', fontSize: '16px' }}>
            页面加载出错: {this.state.error?.message}
          </Text>
        </View>
      )
    }

    return this.props.children
  }
}