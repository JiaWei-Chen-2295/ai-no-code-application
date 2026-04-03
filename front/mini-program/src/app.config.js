module.exports = {
  pages: [
    'pages/home/index',
    'pages/login/index',
    'pages/register/index',
    'pages/app-detail/index',
    'pages/chat/index',
    'pages/preview/index',
    'pages/create-app/index',
    'pages/my/index',
    'pages/settings/index',
    'pages/about/index',
    'pages/profile-edit/index',
    'pages/app-manage/index',
    'pages/version-manage/index',
    'pages/deploy-manage/index'
  ],
  window: {
    backgroundTextStyle: 'light',
    navigationBarBackgroundColor: '#1a1c1c',
    navigationBarTitleText: 'AI No-Code',
    navigationBarTextStyle: 'white'
  },
  tabBar: {
    color: '#7a7a7a',
    selectedColor: '#785900',
    backgroundColor: '#ffffff',
    borderStyle: 'white',
    list: [
      {
        pagePath: 'pages/home/index',
        text: '首页'
      },
      {
        pagePath: 'pages/my/index',
        text: '我的'
      }
    ]
  },
  usingComponents: {}
}
