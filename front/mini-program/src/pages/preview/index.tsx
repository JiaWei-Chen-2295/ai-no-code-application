/**
 * 应用预览页面
 * 使用 webview 嵌入预览
 */

import React from 'react'
import { View, WebView } from '@tarojs/components'
import { useRouter } from '@tarojs/taro'
import { getApiBaseUrl } from '../../utils/http'

export default function PreviewPage() {
  const router = useRouter()
  const { appId, version } = router.params
  const previewUrl = `${getApiBaseUrl()}/api/static/preview/${appId}${version ? `/${version}` : ''}/index.html`

  //http://localhost:8081/api/static/preview/396761853419716608/1/

  
  return (
    <View className="preview-container">
      <WebView src={previewUrl} />
    </View>
  )
}