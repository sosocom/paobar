package com.guitar.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

/**
 * 深度页面分析
 */
public class DeepPageAnalyzer {

    public static void main(String[] args) throws IOException {
        String url = "https://yopu.co/view/xXAxzLL1";
        
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .timeout(30000)
                .get();

        System.out.println("=== Article 元素内容 ===");
        Element article = doc.selectFirst("article");
        if (article != null) {
            System.out.println("Article HTML (前1000字符):");
            String html = article.html();
            System.out.println(html.substring(0, Math.min(1000, html.length())));
            System.out.println("\n\nArticle Text (前500字符):");
            String text = article.text();
            System.out.println(text.substring(0, Math.min(500, text.length())));
        }
        
        System.out.println("\n\n=== Body 中的 data- 属性 ===");
        Element body = doc.body();
        body.attributes().forEach(attr -> {
            if (attr.getKey().startsWith("data-")) {
                System.out.println(attr.getKey() + " = " + attr.getValue());
            }
        });
        
        System.out.println("\n=== 所有带 id 的元素 ===");
        doc.select("[id]").forEach(el -> {
            System.out.println("ID: " + el.id() + ", Tag: " + el.tagName() + ", Class: " + el.className());
        });
        
        System.out.println("\n=== 完整页面 HTML 结构概览 ===");
        System.out.println(doc.html().substring(0, Math.min(2000, doc.html().length())));
    }
}
