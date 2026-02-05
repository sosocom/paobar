package com.sosocom.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 网页资源下载工具（仅供学习研究）
 * 
 * ⚠️ 警告：
 * 1. 此工具仅供个人学习研究使用
 * 2. 不得用于商业用途
 * 3. 需遵守目标网站的 robots.txt 和使用条款
 * 4. JS/CSS 文件可能受版权保护
 */
public class ResourceDownloader {

    private final String baseUrl;
    private final String outputDir;

    public ResourceDownloader(String baseUrl, String outputDir) {
        this.baseUrl = baseUrl;
        this.outputDir = outputDir;
    }

    /**
     * 下载页面及其资源（仅供学习）
     */
    public void downloadPageResources(String pageUrl) throws IOException {
        System.out.println("⚠️  警告：此工具仅供个人学习研究使用");
        System.out.println("请确保遵守相关法律法规和网站使用条款\n");

        Document doc = Jsoup.connect(pageUrl)
                .userAgent("Mozilla/5.0")
                .timeout(30000)
                .get();

        // 创建输出目录
        Files.createDirectories(Paths.get(outputDir));

        // 下载 CSS
        System.out.println("=== 下载 CSS 文件 ===");
        Elements cssLinks = doc.select("link[rel=stylesheet]");
        for (Element css : cssLinks) {
            String href = css.absUrl("href");
            if (!href.isEmpty()) {
                downloadFile(href, "css");
            }
        }

        // 下载 JS
        System.out.println("\n=== 下载 JS 文件 ===");
        Elements scripts = doc.select("script[src]");
        for (Element script : scripts) {
            String src = script.absUrl("src");
            if (!src.isEmpty()) {
                downloadFile(src, "js");
            }
        }

        // 保存 HTML
        System.out.println("\n=== 保存 HTML ===");
        String html = doc.html();
        Path htmlPath = Paths.get(outputDir, "page.html");
        Files.writeString(htmlPath, html);
        System.out.println("保存 HTML: " + htmlPath);

        // 提取 data-model
        Element dataModel = doc.selectFirst("[data-model]");
        if (dataModel != null) {
            String model = dataModel.attr("data-model");
            Path modelPath = Paths.get(outputDir, "data-model.txt");
            Files.writeString(modelPath, model);
            System.out.println("保存 data-model: " + modelPath);
        }

        System.out.println("\n✅ 完成！资源已保存到: " + outputDir);
        System.out.println("\n⚠️  提醒：");
        System.out.println("1. 这些资源仅供个人学习使用");
        System.out.println("2. 不要用于商业目的或公开分发");
        System.out.println("3. 建议使用开源吉他谱渲染库替代");
    }

    private void downloadFile(String url, String type) {
        try {
            String fileName = getFileNameFromUrl(url);
            Path typeDir = Paths.get(outputDir, type);
            Files.createDirectories(typeDir);
            Path outputPath = typeDir.resolve(fileName);

            System.out.println("下载: " + url + " -> " + outputPath);

            ReadableByteChannel rbc = Channels.newChannel(new URL(url).openStream());
            FileOutputStream fos = new FileOutputStream(outputPath.toFile());
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();

        } catch (Exception e) {
            System.err.println("下载失败: " + url + " - " + e.getMessage());
        }
    }

    private String getFileNameFromUrl(String url) {
        String[] parts = url.split("/");
        String fileName = parts[parts.length - 1];
        // 移除查询参数
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        return fileName;
    }

    public static void main(String[] args) throws IOException {
        String pageUrl = "https://yopu.co/view/xXAxzLL1";
        String outputDir = "./downloaded_resources";

        ResourceDownloader downloader = new ResourceDownloader(pageUrl, outputDir);
        downloader.downloadPageResources(pageUrl);

        System.out.println("\n⚠️  重要提示：");
        System.out.println("如需在生产环境使用，请考虑以下合法方案：");
        System.out.println("1. 使用 AlphaTab 等开源吉他谱渲染库");
        System.out.println("2. 购买商业授权的吉他谱软件");
        System.out.println("3. 开发自己的吉他谱渲染引擎");
    }
}
