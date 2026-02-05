package com.sosocom.crawler;

import com.sosocom.entity.Song;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Yopu.co 网站爬虫
 */
@Slf4j
@Component
public class YopuCrawler {

    @Value("${crawler.user-agent}")
    private String userAgent;

    @Value("${crawler.timeout}")
    private int timeout;

    /**
     * 爬取吉他谱详情
     */
    public Song crawlTabDetail(String url) throws IOException {
        log.info("开始爬取吉他谱: {}", url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(timeout)
                    .get();

            Song song = new Song();
            song.setOriginalUrl(url);

            // 解析标题 (格式: 歌名 - 歌手 吉他弹唱谱)
            String title = doc.title();
            log.info("页面标题: {}", title);
            parseTitle(title, song);

            // ============================================
            // 核心：获取 header 标签的下一个兄弟节点
            // ============================================
            Element headerElement = doc.selectFirst("header");
            Element targetElement = null;
            
            if (headerElement != null) {
                // 获取 header 的下一个兄弟节点
                targetElement = headerElement.nextElementSibling();
                if (targetElement != null) {
                    log.info("找到 header 的下一个兄弟节点: <{}>, HTML长度: {}", 
                            targetElement.tagName(), targetElement.html().length());
                }
            }
            
            // 如果没找到 header，尝试其他方式
            if (targetElement == null) {
                log.warn("未找到 header 标签，尝试其他选择器");
                targetElement = doc.selectFirst(".layout.no-print, .main, article");
                if (targetElement != null) {
                    log.info("使用备用选择器找到内容，长度: {}", targetElement.html().length());
                }
            }
            
            if (targetElement != null) {
                // ============================================
                // 1. 提取 xhe-meta 内容保存到 meta 字段
                // ============================================
                Element metaElement = targetElement.selectFirst(".xhe-meta");
                if (metaElement != null) {
                    String metaText = metaElement.text();
                    song.setMeta(metaText);
                    log.info("提取 xhe-meta 内容: {}", metaText);
                }
                
                // ============================================
                // 2. 移除 xhe-header 元素（不保存）
                // ============================================
                Elements headerElements = targetElement.select(".xhe-header");
                if (!headerElements.isEmpty()) {
                    headerElements.remove();
                    log.info("已移除 {} 个 xhe-header 元素", headerElements.size());
                }
                
                // ============================================
                // 3. 保存处理后的 HTML 内容
                // ============================================
                String cleanedHtml = targetElement.outerHtml();
                song.setTabContent(cleanedHtml);
                log.info("成功保存吉他谱HTML内容，长度: {}", cleanedHtml.length());
            }

            // 解析歌词（从隐藏的 article 标签或主要内容中提取文本）
            Element lyricsElement = doc.selectFirst("article");
            if (lyricsElement != null) {
                String lyrics = lyricsElement.text();
                song.setLyrics(lyrics);
                log.info("从 article 提取歌词长度: {}", lyrics.length());
            } else if (targetElement != null) {
                // 从目标元素中提取纯文本作为歌词
                String lyrics = targetElement.text();
                if (lyrics.length() > 50) {
                    song.setLyrics(lyrics);
                    log.info("从目标元素提取歌词长度: {}", lyrics.length());
                }
            }

            // 尝试解析其他信息（调式、变调夹等）
            parseTabInfo(doc, song);

            song.setStatus(1);
            song.setViewCount(0);
            song.setFavoriteCount(0);

            log.info("爬取成功: {} - {}, meta: {}", 
                    song.getSongName(), song.getArtist(), 
                    song.getMeta() != null ? song.getMeta() : "无");
            return song;

        } catch (IOException e) {
            log.error("爬取失败: {}", url, e);
            throw e;
        }
    }

    /**
     * 解析标题，提取歌名和歌手
     */
    private void parseTitle(String title, Song song) {
        // 去掉常见后缀
        title = title.replaceAll("吉他[弹唱谱|谱].*$", "").trim();
        
        // 尝试按 " - " 分割
        if (title.contains(" - ")) {
            String[] parts = title.split(" - ", 2);
            song.setSongName(parts[0].trim());
            song.setArtist(parts[1].trim());
        } else if (title.contains("-")) {
            String[] parts = title.split("-", 2);
            song.setSongName(parts[0].trim());
            song.setArtist(parts[1].trim());
        } else {
            // 无法分割，全部作为歌名
            song.setSongName(title);
            song.setArtist("未知");
        }
    }

    /**
     * 解析吉他谱信息（调式、变调夹等）
     */
    private void parseTabInfo(Document doc, Song song) {
        // 尝试查找包含调式信息的元素
        Elements infoElements = doc.select(".tab-info, .song-info, .meta-info");
        
        for (Element element : infoElements) {
            String text = element.text();
            
            // 解析原调
            if (text.contains("原调")) {
                String key = extractKey(text, "原调");
                if (key != null) song.setOriginalKey(key);
            }
            
            // 解析选调/演奏调
            if (text.contains("选调") || text.contains("演奏调")) {
                String key = extractKey(text, "选调|演奏调");
                if (key != null) song.setPlayKey(key);
            }
            
            // 解析变调夹
            if (text.contains("变调夹")) {
                Integer capo = extractCapo(text);
                if (capo != null) song.setCapo(capo);
            }
            
            // 解析节拍
            if (text.contains("节拍")) {
                String beat = extractBeat(text);
                if (beat != null) song.setBeat(beat);
            }
        }
    }

    private String extractKey(String text, String pattern) {
        // 匹配调式：C, D, E, F, G, A, B (可能带#或b)
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern + "[:：]?\\s*([A-G][#b]?)");
        java.util.regex.Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private Integer extractCapo(String text) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("变调夹[:：]?\\s*(\\d+)");
        java.util.regex.Matcher m = p.matcher(text);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return null;
    }

    private String extractBeat(String text) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("节拍[:：]?\\s*(\\d+/\\d+)");
        java.util.regex.Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * 爬取列表页
     */
    public java.util.List<String> crawlTabList(String listUrl) throws IOException {
        log.info("开始爬取列表页: {}", listUrl);
        
        Document doc = Jsoup.connect(listUrl)
                .userAgent(userAgent)
                .timeout(timeout)
                .get();

        java.util.List<String> urls = new java.util.ArrayList<>();
        Elements links = doc.select("a[href*=/view/]");
        
        for (Element link : links) {
            String href = link.absUrl("href");
            if (!href.isEmpty() && !urls.contains(href)) {
                urls.add(href);
            }
        }
        
        log.info("找到 {} 个吉他谱链接", urls.size());
        return urls;
    }
}
