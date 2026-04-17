# 🚀 快速操作指南

## 需要您完成的最后一步

### 📋 操作清单

**将示例图片放到 app 目录中**：

```bash
# 如果图片在项目根目录
cp ok_tab.jpg guitar-chord-app/public/
cp error_tab.jpg guitar-chord-app/public/

# 或者直接拖拽文件到 guitar-chord-app/public/ 文件夹
```

### ✅ 完成后的文件结构

```
guitar-chord-app/
├── public/
│   ├── ok_tab.jpg      ⬅ 支持的谱类型示例
│   └── error_tab.jpg   ⬅ 不支持的谱类型示例
├── src/
│   └── views/
│       └── Crawler.vue  ✅ 已更新，包含图片展示
└── ...
```

### 🎨 页面预览

访问扒谱页面后，您将看到：

1. **输入框和获取按钮**（原有功能）
2. **使用说明**（原有说明）
3. **支持的谱类型** ⬅ 新增
   - ✓ 支持带和弦标注的谱
     - 显示 ok_tab.jpg
     - 绿色边框和提示
   - ✗ 不支持纯图片格式的谱
     - 显示 error_tab.jpg
     - 红色边框和提示（半透明）

### 🔍 如何验证

```bash
cd guitar-chord-app
npm run dev
```

然后在浏览器打开扒谱页面，滚动到底部查看示例图片。

---

**就这么简单！** 🎉
