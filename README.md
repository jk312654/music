apk文件名为app-debug.apk


SplashActivity为启动项。
MusicPlayerActivity为音乐播放页。

一些要点：
1.《用户协议》和《隐私政策》使用SpannableString，是他们变成可点击的连接。
2.使用MMKV来进行隐私信息的本地存储。
3.HomeMultiAdapter继承BaseMultiItemQuickAdapter<HomePageInfo, BaseViewHolder>来封装所有的展示类型，
并根据类型选择相应的RecycleViewAdapter进行嵌套RecycleView层的处理。
4.使用SwipeRefreshLayout实现下拉刷新。
5.使用BaseMultiItemQuickAdapter实现上拉加载。

