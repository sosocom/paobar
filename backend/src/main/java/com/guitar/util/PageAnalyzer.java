package com.guitar.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * 网页结构分析工具
 */
public class PageAnalyzer {

    public static void main(String[] args) throws IOException {
        String url = "https://yopu.co/view/xXAxzLL1";
        
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .timeout(30000)
                .get();

        System.out.println("=== 页面标题 ===");
        System.out.println(doc.title());
        
        System.out.println("\n=== 所有图片 ===");
        Elements images = doc.select("img");
        for (int i = 0; i < images.size(); i++) {
            Element img = images.get(i);
            System.out.println((i+1) + ". src: " + img.absUrl("src"));
            System.out.println("   alt: " + img.attr("alt"));
            System.out.println("   width: " + img.attr("width") + ", height: " + img.attr("height"));
        }
        
        System.out.println("\n=== Script 标签 ===");
        Elements scripts = doc.select("script");
        for (Element script : scripts) {
            String src = script.attr("src");
            if (!src.isEmpty()) {
                System.out.println("外部JS: " + src);
            } else {
                String content = script.html();
                if (content.contains("tab") || content.contains("chord") || content.contains("guitar")) {
                    System.out.println("--- 内联JS包含吉他谱相关内容 ---");
                    System.out.println(content.substring(0, Math.min(500, content.length())));
                    System.out.println("...(省略)");
                }
            }
        }
        
        System.out.println("\n=== Meta 标签 ===");
        Elements metas = doc.select("meta");
        for (Element meta : metas) {
            System.out.println(meta.toString());
        }
        
        System.out.println("\n=== Canvas/SVG 元素 ===");
        Elements canvas = doc.select("canvas");
        System.out.println("Canvas数量: " + canvas.size());
        Elements svg = doc.select("svg");
        System.out.println("SVG数量: " + svg.size());
        
        System.out.println("\n=== 主要内容容器 ===");
        Elements mainContents = doc.select("main, article, .content, .main, #content, #main");
        for (Element content : mainContents) {
            System.out.println("标签: " + content.tagName() + ", class: " + content.className() + ", id: " + content.id());
        }
    }
}
