# API_kit的二开版本

## 介绍
-  APIKit_plus是对API-Security师傅的APIKit1.0进行的二开，增加了扫描开关，避免直接打开burp乱扫被抓起来。
- 修复了输出页面卡死的问题。
- Do Target API Scan页面将cookie的输入框变大了一些，更美观的输入。
- 感谢原作者的贡献，原项目地址：https://github.com/API-Security/APIKit
- APIKit是基于BurpSuite提供的JavaAPI开发的插件。
- APIKit可以主动/被动扫描发现应用泄露的API文档，并将API文档解析成BurpSuite中的数据包用于API安全测试。

  ## 使用界面如下
  
<img width="1824" alt="image" src="https://github.com/user-attachments/assets/87cc6dcc-8f9d-4496-9cf4-419db66c6947" />

### Scanner Enabled

开启扫描就打开Scanner Enabled按钮，其他功能与1.0相似

<img width="666" alt="image" src="https://github.com/user-attachments/assets/88d8abbd-cd3b-4614-b1e2-1763f45baf4f" />




### Send with Cookie

开启Cookie，可以把包的Cookie存下来，生成请求的时候保留Cookie。


### Auto Request Sending

扫描所有API泄露中的所有接口。


### Do Auto API scan

Do Auto API scan可以指定任意一个请求进行API指纹探测。
在任何一个Burpsuite可以右键打开更多选项的页面中，都可以点击右键，选择Do Auto API scan来发起一次主动扫描，进行API指纹探测。


### Do Target API Scan

Do Target API scan可以指定任意API技术、任意BasePath、任意API文档Path、和任意Header进行API请求的生成和探测。
在任何一个Burpsuite可以右键打开更多选项的页面中，都可以点击右键，选择Do Target API scan来打开选项框。
