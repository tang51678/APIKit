# APIKit Pro

<div align="center">
  <img src="https://raw.githubusercontent.com/tang51678/APIKit/master/logo.png" alt="APIKit Pro Logo" width="200"/>
  <p>🔍 专业的API安全测试增强工具，让API安全测试更高效、更智能</p>
  <div>
    <a href="https://github.com/tang51678/APIKit/releases"><img src="https://img.shields.io/github/v/release/tang51678/APIKit?style=flat-square" alt="Release Version"/></a>
    <a href="https://github.com/tang51678/APIKit/stargazers"><img src="https://img.shields.io/github/stars/tang51678/APIKit?style=flat-square" alt="GitHub Stars"/></a>
    <a href="https://github.com/tang51678/APIKit/issues"><img src="https://img.shields.io/github/issues/tang51678/APIKit?style=flat-square" alt="GitHub Issues"/></a>
  </div>
</div>

## 简介

APIKit Pro 是一款专为 Burp Suite 打造的 API 安全测试增强工具，基于 [API-Security/APIKit](https://github.com/API-Security/APIKit) 进行全面增强和优化。它能够自动识别、解析和测试各种类型的 API，显著提升 API 安全测试的效率和准确性。

## 核心功能

### 🔍 多类型 API 智能识别
- 支持 REST、Swagger、GraphQL、SOAP 等多种 API 类型
- 自动解析 API 文档，提取接口信息
- 智能生成测试数据包，加速安全测试流程

### 🚀 灵活的扫描控制
- 精准的扫描范围控制和开关
- 自动/手动扫描模式切换
- 自定义扫描策略，支持精细化接口探测
- **多选 URL 复制功能**：支持批量复制 API 地址，提高工作效率

### 🎨 增强的用户界面
- **状态码 200 绿色显示**：直观识别成功响应
- **标题栏可拖动**：灵活调整表格布局
- **单次点击限制**：优化频繁点击同一 URL 的体验
- 序号列和优化的内容长度显示

### 🛡️ 高级安全特性
- **Bypass 扫描**：支持自定义绕过后缀
- **Cookie 保留**：保持原始会话上下文
- **路径/主机过滤**：精准控制扫描范围，避免重复扫描

## 安装指南

### 从 Release 下载
1. 访问 [GitHub Release 页面](https://github.com/tang51678/APIKit/releases)
2. 下载最新版本的 `APIKit_pro-x.x.x-jar-with-dependencies.jar` 文件
3. 在 Burp Suite 中，通过 "Extender" -> "Extensions" -> "Add" 导入 jar 文件

### 从源码构建
```bash
# 克隆仓库
git clone https://github.com/tang51678/APIKit.git
cd APIKit

# 构建项目
mvn clean package

# 构建成功后，jar 文件将位于 target 目录下
```

## 使用教程

### 基础扫描模式

#### 自动扫描
1. 在 APIKit Pro 面板中开启 **Scanner Enabled**
2. 工具将被动扫描访问流量中的 API 文档
3. 启用 **Auto Request** 可自动扫描所有发现的 API 接口

#### 手动扫描
- 在 Burp Suite 中右键点击请求，选择 **Do Auto API scan** 快速进行 API 指纹探测
- 选择 **Do Target API Scan** 可进行更精细的配置：
  - 配置 API 类型
  - 设置 BasePath
  - 自定义文档路径
  - 配置 Bypass 策略


### 高级过滤功能

#### 路径过滤 (FilterPath)
- 支持精确匹配和模糊匹配
- 可配置多个关键词
- 被过滤路径会标记为 `[FILTERED]`

#### 主机过滤 (FilterHost)
- 灵活的主机名匹配
- 支持通配符 `*`
- 精确控制扫描范围


### URL 批量复制功能
1. 使用 Shift+点击 或 Ctrl+点击 选择多个 API URL
2. 右键点击选择 **Copy URL(s)**
3. 所有选中的 URL 将被复制到剪贴板，每个 URL 占一行

<div align="center">
  <img width="600" alt="多选URL复制" src="https://github.com/user-attachments/assets/24f79631-fd82-4f7f-8ef2-1a2091dd22a" />
</div>


## 实用示例

### Bypass 扫描示例
```
# 使用分号绕过WAF示例
http://localhost:8089/actuator/health;.js
http://localhost:8089/auth/adminlogin;.js?id=123
```

### 配置参考
```
# 过滤特定路径（多个路径用逗号分隔）
FilterPath: /debug/config,/admin/panel

# 过滤特定主机（支持通配符）
FilterHost: example.com,*.test.org
```

## 版本更新日志

### v2.0.0 (最新版本)
- ✨ **多选 URL 复制功能**：支持批量复制 API 地址
- 🎨 **UI 增强**：状态码 200 绿色显示，提升视觉体验
- 🚀 **用户体验优化**：标题栏可拖动，添加单次点击限制
- 🔧 **代码重构**：优化表格选择逻辑，修复多选相关问题

### v1.6.3
- 📊 API 接口列表新增序号列
- 📏 优化内容长度显示
- 🚀 减少 API 接口重复扫描
- 🎯 提高过滤逻辑准确性

## 兼容性
- 完全兼容 Burp Suite Professional 和 Community Edition
- 支持多种 API 类型：REST, Swagger, GraphQL, SOAP
- 适用于各种 Web 应用安全测试场景

## 贡献指南

我们欢迎社区贡献！如果您有任何想法或改进，请：
1. Fork 本仓库
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 免责声明

1. 本工具仅用于授权的企业安全建设和合法的安全测试
2. 使用本工具时需确保符合当地法律法规
3. 使用本工具造成的任何非法后果由使用者承担
4. 使用本工具即视为同意本免责声明

## 致谢

感谢 [API-Security/APIKit](https://github.com/API-Security/APIKit) 项目的原作者，以及所有为 API 安全做出贡献的开发者们。

## 许可证

[MIT License](https://github.com/tang51678/APIKit/blob/master/LICENSE)

---

<div align="center">
  <p>🔒 让 API 安全测试更简单、更高效</p>
  <p><a href="https://github.com/tang51678/APIKit">⭐️ 欢迎 Star 支持项目发展</a></p>
</div>
