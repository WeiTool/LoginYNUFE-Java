# SRUN 校园网登录助手

一个简洁高效的校园网认证管理应用，支持多账户管理、登录/登出、操作日志记录及版本更新检测。

## 📱 功能特性

- **账户管理**
  ✅ 添加/编辑/删除账户
  ✅ 密码加密存储（SharedPreferences + Gson）
  ✅ 区域选择（宿舍区/教学区）

- **网络认证**
  🔑 支持SRUN协议认证
  🌐 自动检测客户端IP
  ⚡ 异步网络请求处理

- **状态管理**
  🟢 实时登录状态显示
  📶 客户端IP地址跟踪
  📜 详细操作日志（登录/登出记录）

- **附加功能**
  🆕 Gitee Release版本检测
  🚀 一键跳转浏览器下载更新
  🗑️ 批量删除账户/日志

## 🛠️ 技术实现

### 核心组件
- **加密模块**  
  `LoginLogout.java` 实现SRUN协议的MD5/SHA1/XEncode加密
- **数据持久化**  
  `AccountManager.java` 使用SharedPreferences存储账户数据
- **网络通信**- **网络通信**  
  `OkHttp3` 实现Gitee API版本检查

### UI框架
- **RecyclerView**  
  卡片式账户列表（`item_account.xml`）
- **Material Design**  
  浮动按钮/FAB/对话框组件
- **自定义适配器**  
  `AccountAdapter` 和 `LogsAdapter`

## 📦 安装指南
仅仅支持Android系统，项目的[releases](https://gitee.com/weitool/login-ynufe-java/releases)当中

## 🎮 使用说明

1. **添加账户**
  点击右下角 ➕ 按钮，输入：
  ▸ 学号（如202305006401）
  ▸ 密码
  ▸ 选择网络区域

2. **登录/登出**
  ▶️ 绿色按钮执行登录
  ⏹️ 红色按钮执行登出

3. **查看日志**
  📖 点击卡片日志按钮
  🧹 支持日志批量清除

4. **版本更新**
  检测到新版本时自动弹出提示
  ⬇️ 点击下载跳转浏览器

## ⚙️ 依赖项

```gradle
dependencies {
    implementation 'com.squareup.okhttp3:okhttp:4.9.3'
    implementation 'com.google.code.gson:gson:2.8.9'
    implementation 'com.google.android.material:material:1.6.0'
}
```

## 📄 协议

本项目采用 GPL-3.0 开源，欢迎贡献代码。使用SRUN协议部分请遵守学校网络管理规定。