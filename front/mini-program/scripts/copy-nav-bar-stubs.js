const fs = require('fs')
const path = require('path')

const distDir = path.join(__dirname, '..', 'dist', 'weapp')
const navBarDir = path.join(distDir, 'components', 'navigation-bar')

if (!fs.existsSync(navBarDir)) {
  fs.mkdirSync(navBarDir, { recursive: true })
}

const files = {
  'navigation-bar.wxml': `<template name="tmpl_0_0">
  <view style="{{i.st}}" class="{{i.cl}}" id="{{i.uid||i.sid}}" data-sid="{{i.sid}}">
    <template is="{{xs.a(c, item.nn, l)}}" data="{{i:item,c:c+1,l:xs.f(l,item.nn)}}" wx:for="{{i.cn}}" wx:key="sid" />
  </view>
</template>
`,
  'navigation-bar.wxss': '/* navigation-bar stub */\n',
  'navigation-bar.json': JSON.stringify({ component: true, usingComponents: {} }, null, 2) + '\n',
  'navigation-bar.js': 'Component({})\n',
}

Object.entries(files).forEach(([name, content]) => {
  fs.writeFileSync(path.join(navBarDir, name), content)
})

console.log('Navigation bar stub files copied.')
