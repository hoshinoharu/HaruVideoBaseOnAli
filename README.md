# HaruVideoBaseOnAli
基于阿里SDK封装的安卓播放器
## 结构
项目总共分为两个部分 VideoController 和 ControlPanel
## VideoController
控制器

主要用于视频的控制，然后再通知Panel的改变状态，项目中是由一个FrameLayout来实现控制器的功能，初始化的时候会添加视频的View到内部。

###权限
视频播放时的缓存，需要文件读写权限
```java
    /**
     * 检查权限
     * 缓存需要 文件读写权限
     */
    void checkPermission(Activity activity) ;
```

### 直播和普通视频的区分
```java
    /**
     * 视频是否可以控制进度
     *
     * @return 比如直播就无法控制进度
     */
    boolean canControlProgress();
```
通过继承项目中的Controller 重载此方法，在面板中显示的时候可以根据这个方法来判断，项目中的ControlPanel的实现是区分了是否是直播，来隐藏和显示进度条。

## ControlPanel
控制面板
主要用于视频控制器于用户交互的部分 通过Controller的bindControlPanel方法绑定 顶层是面板接口 里面定义了视频播放时间的回调，由控制器来调用
### 面板组成
由五块组成，首先分为 前后两个面板，前面的面板用于做一些提示信息的展示，后面的面板分为4块，上下左右，面板初始化的时候通过设置PanelAdapter会有各部分面板的回调。
```java
public interface PanelAdapter {

    void onInitTopPanel(ViewGroup topPanel);

    void onInitBottomPanel(ViewGroup bottomPanel);

    void onInitLeftPanel(ViewGroup leftPanel);

    void onInitRightPanel(ViewGroup rightPanel);

    void onInitFrontPanel(ViewGroup frontPanel);
}
```
可以在面板的对应位置 自定义控件
### 视频控制手势
面板可以添加手势监听
```java
public interface HaruVideoControlListener {

    void onShowControlPanel();

    void onHideControlPanel();

    /**
     * 开关面板
     */
    void onToggleControlPanel();

    void onAddBrightness(float lPercent);

    void onAddProgress(float pPercent);

    void onAddVolume(float vPercent);

    /**
     * 提交音量修改
     */
    void onSubmitVolume(float vPercent);

    /**
     * 提交进度修改
     */
    void onSubmitProgress(float pDuration);

    /**
     * 提交亮度修改
     */
    void onSubmitBrightness(float lPercent);

    /**
     * 开始设置亮度
     */
    void onStartSetBrightness();

    /**
     * 开始设置声音
     */
    void onStartSetVolume();
}

```
项目中的实现为，屏幕左边上下滑动控制亮度，屏幕右边上下滑动控制声音，水平滑动控制进度，用户按下时 显示面板，用户抬起时隐藏面板

滑动控制分为三个阶段，开始，滑动中，提交操作（滑动结束）;
### 配置
视频的一些设置保存在SharedPreferences中 通过Config类获取 

亮度默认是50% 声音默认是0%

