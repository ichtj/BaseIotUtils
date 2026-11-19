# Nginx Android 控制台

入口为 `com.ichtj.basetools.nginx.NginxAty`，主工具页面原有的 Nginx 入口可直接使用。
当前项目的 `APP_CHOOSE` 仍决定应用启动入口；也可以直接打开本页面：

```text
adb shell am start -n com.ichtj.netrecord/com.ichtj.basetools.nginx.NginxAty
```

如果切换了 `APP_CHOOSE`，将上面的包名替换为实际 applicationId。

## 界面操作

- 打开页面只准备环境并检查版本，点击“启动服务”才启动。
- 默认监听 `0.0.0.0:8080`，页面显示可用的 IPv4 地址；无网络时显示实际缺失状态。
- 支持启动、停止、配置重载、配置校验、版本信息、日志查看和页面内预览。
- 退出页面或切到后台时，停止当前页面启动的实例；返回页面不会自动重新启动。
- 执行中的按钮状态、命令输出、退出码、耗时、失败和超时原因都会显示。
- 默认实例启动时检查专属 `/health` 响应，重载时检查实际重载日志与健康响应。
- 当前项目使用系统 UID，预览弹窗通过 HTTP 读取并显示真实页面及状态码，兼容系统 UID 的 WebView 限制。

## 原生命令输入

在日志上方输入命令，点击“发送”，也可使用键盘发送键。兼容完整指令和仅参数两种写法：

```text
nginx -v
-V
nginx -t
nginx -T
nginx -h
nginx -s reload
nginx -s reopen
nginx -s quit
nginx -s stop
nginx -t -g "env ICHTJ_NGINX_TEST=value;"
nginx -p "/data/user/0/你的包名/files/custom nginx/" -c "custom nginx.conf" -g "daemon off;"
```

参数不设置白名单，直接交给随 APK 打包的 Nginx。支持单引号、双引号和转义；
错误参数显示 Nginx 的原生错误和退出码。输入的是 Nginx 的 argv，不经过 Shell，
管道、Shell 变量展开和重定向不是这里的指令语法。stdout/stderr 已合并显示。

未指定路径时，默认传入当前会话的 `-p <session>/ -c conf/nginx.conf`。
自定义 `-p`、`-c`、`-e`、`-g` 等参数继续传入 Nginx，其文件与权限要求以 Nginx 为准。
本页管理一个当前启动实例；已有实例运行时，先停止再使用新的启动参数。
原生启动命令会记录参数，后续操作和退出清理使用同一组参数。
自定义配置的监听地址、日志和 PID 路径以配置为准；仅在确认默认健康端点时启用内置测试页按钮。
原生后台启动返回 0 与 HTTP 健康检查成功分别显示，不将信号命令成功误报为已验证的服务响应。

短命令超时为 5 秒，默认服务启动检测最长 8 秒。持续运行的原生进程由会话跟踪。
界面保留最近 96 KB 输出；每次读取日志最多取各文件末尾 32 KB。“清空显示”不删除运行日志。

## 文件与打包

- `app/libs/arm64-v8a/libnginx.so`
- `app/libs/armeabi-v7a/libnginx.so`
- `app/libs/nginx-build-info.json`：版本及 SHA-256。
- `src/main/assets/nginx/conf/`：默认配置和 MIME 表。
- `src/main/assets/nginx/licenses/`：Nginx、OpenSSL、PCRE2 许可证。

产物为 PIE 可执行程序，不能通过 `System.loadLibrary()` 加载。
`app/build.gradle` 将 `libs` 纳入 jniLibs，启用安装时解压，并保留 Nginx 原始文件。
运行时从 `applicationInfo.nativeLibraryDir` 执行，不复制二进制到可写数据目录。
每次页面会话使用独立的私有配置、PID、测试页和日志目录；正常释放时清理本会话文件。
自定义 `-p` 指向的目录由用户管理，不自动删除。

Nginx 产物最低支持 Android 11 / API 30，两个 ABI 都具有 16 KB ELF 段对齐。
项目原有最低 SDK 不变，低于 Android 11 的设备在本页面显示不支持提示。
界面新增字符串提供英文默认资源和简体中文资源。

## 验证

使用 JDK 17，在项目根目录执行：

```text
gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --offline
```

安装应用及测试 APK 后，只运行 Nginx 用例：

```text
adb shell am instrument -w -e class com.ichtj.basetools.nginx.NginxControllerTest com.ichtj.netrecord.test/androidx.test.runner.AndroidJUnitRunner
```

2026-11-18 已在 Android 11 设备通过 6 项集成测试：正常命令及重载/停止、端口占用错误、
切后台停止、启动中退出、原生参数及错误反馈、带空格的自定义路径与前台/后台模式清理。
两套 Nginx 在最终 APK 中的 SHA-256 与来源产物一致。

Android 15 及以上尚未实机验证。当前实测设备未连接局域网，已验证监听所有 IPv4 接口及
通过 ADB 转发的 HTTP 访问，尚未验证其他局域网设备的实际访问。
