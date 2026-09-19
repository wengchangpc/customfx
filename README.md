# Custom FX (Self) — Forge 1.20.1

纯客户端模组：给你的角色添加 **发光描边 / 彩色名字 / 旋转光环**，**仅自己可见**（服务器与其他玩家零感知）。

## 特性

| 特效 | 说明 |
|---|---|
| ✨ 发光描边 | 通过客户端侧 `setGlowingTag` 实现，仅本地渲染，不同步 |
| 🌈 彩色名字 | 拦截本地名字渲染（`RenderNameTagEvent`），支持彩虹循环或固定色 |
| 💫 旋转光环 | 自定义渲染层（`RenderLayer`），悬浮头顶的水平发光圆环，随时间旋转 |

全部基于 Forge 官方事件 API，**不使用 Mixin**，稳定可靠。

## 使用

1. 把 `customfx-1.0.0.jar` 放进 `.minecraft/mods/`（Forge 1.20.1）
2. 启动游戏即可看到效果（默认：描边 + 彩虹光环 + 彩虹名字全开）
3. 游戏内指令：
   - `/customfx glow` — 开关发光描边
   - `/customfx halo` — 开关旋转光环
   - `/customfx name` — 开关彩色名字
   - `/customfx reload` — 重读配置文件

## 配置文件

首次启动自动生成 `config/CustomFX/settings.txt`：

```properties
glow=true
halo=true
name=true
haloColor=rainbow
nameColor=rainbow
```

- 颜色可填 `rainbow`（随时间循环的彩虹）或十六进制，如 `#FF8800`
- 改完后 `/customfx reload` 生效（`glow`/`halo`/`name` 三个开关用指令即可）

## 兼容性

- `displayTest="IGNORE_ALL_VERSION"`：可加入未安装本模组的服务器
- 与 CustomCape / CustomSkin 模组完全兼容，可叠加使用
- 光环同时适配 classic（Steve）与 slim（Alex）模型

## 构建方式

推送到 GitHub 后 Actions 自动构建（JDK 17 + Gradle 8.1.1），在 Actions 页面底部 Artifacts 下载 jar；打 `v*` 标签自动发布 Release。

## 许可

MIT
