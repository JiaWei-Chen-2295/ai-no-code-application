/**
 * 版本列表组件
 */

import React from 'react'
import { View, Text } from '@tarojs/components'
import { AppVersionVO } from '../../api/app'
import './VersionList.css'

interface VersionListProps {
  versions: AppVersionVO[]
  currentVersion: string
  onVersionSelect: (version: string) => void
}

export default function VersionList({ versions, currentVersion, onVersionSelect }: VersionListProps) {
  return (
    <View className="version-list">
      {versions.map((version) => (
        <View
          key={version.version}
          className={`version-item ${currentVersion === version.version ? 'active' : ''}`}
          onClick={() => onVersionSelect(version.version)}
        >
          <View className="version-header">
            <Text className="version-number">v{version.version}</Text>
            {version.isDeployed && (
              <View className="deployed-tag">
                <Text className="tag-text">已部署</Text>
              </View>
            )}
          </View>
          <Text className="version-time">{version.createTime?.split('T')[0]}</Text>
          {version.message && (
            <Text className="version-message">{version.message}</Text>
          )}
        </View>
      ))}
    </View>
  )
}