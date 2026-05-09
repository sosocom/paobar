package com.sosocom.crawler;

import com.sosocom.entity.Song;
import com.sosocom.tabdoc.TabHtmlNormalizer;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;

/**
 * Selenium WebDriver 爬虫 - 支持动态渲染的网页
 */
@Slf4j
@Component
public class SeleniumCrawler {

    @Value("${crawler.timeout:30000}")
    private int timeout;

    @Value("${crawler.headless:true}")
    private boolean headless;

    @Autowired
    private TabHtmlNormalizer tabHtmlNormalizer;

    /**
     * 初始化 WebDriverManager
     */
    @PostConstruct
    public void init() {
        try {
            log.info("初始化 WebDriverManager，自动下载匹配的 ChromeDriver...");
            WebDriverManager.chromedriver().setup();
            log.info("ChromeDriver 初始化成功");
        } catch (Exception e) {
            log.error("ChromeDriver 初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 使用 Selenium 爬取动态渲染的吉他谱
     */
    public Song crawlTabDetail(String url) {
        WebDriver driver = null;
        try {
            log.info("开始使用 Selenium 爬取吉他谱: {}", url);

            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            if (headless) {
                options.addArguments("--headless"); // 无头模式
            }
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");
            options.addArguments("--remote-allow-origins=*");
            
            // 禁用自动化检测
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.setExperimentalOption("useAutomationExtension", false);

            // 创建 WebDriver（WebDriverManager 已自动配置驱动路径）
            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofMillis(timeout));

            log.info("Chrome 浏览器启动成功");

            // 访问页面
            driver.get(url);
            log.info("页面加载完成");

            // 等待内容渲染完成
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            // 等待主要内容区域加载
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".xhe-sheet, .layout, article")));
            log.info("等待页面渲染完成");

            // 额外等待确保 JS 完全执行
            Thread.sleep(2000);

            // 获取渲染后的完整 HTML
            String pageSource = driver.getPageSource();
            log.info("获取渲染后的 HTML，长度: {}", pageSource.length());

            // 关闭浏览器
            driver.quit();
            driver = null;

            // 使用 Jsoup 解析渲染后的 HTML
            Document doc = Jsoup.parse(pageSource);
            
            Song song = new Song();
            song.setOriginalUrl(url);

            // 解析标题
            String title = doc.title();
            log.info("页面标题: {}", title);
            parseTitle(title, song);

            // ============================================
            // 只获取 .main 下的 .sheet-container 内容（包含和弦）
            // ============================================
            Element sheetContainer = doc.selectFirst(".main .sheet-container, main .sheet-container");
            String targetHtml = "";
            
            if (sheetContainer != null) {
                // 仅支持包含 xhe-sheet（文本谱）内容
                boolean hasXheSheet = sheetContainer.selectFirst(".xhe-sheet") != null;
                if (!hasXheSheet) {
                    throw new RuntimeException("本初衷是便捷看谱弹唱， 暂不支持六线谱爬取，请更新文本谱链接");
                }
                targetHtml = sheetContainer.outerHtml();
                log.info("找到 .sheet-container，HTML长度: {}", targetHtml.length());
            } else {
                // 备用选择器
                log.warn("未找到 .sheet-container，尝试其他选择器");
                Element fallbackElement = doc.selectFirst(".sheet-container");
                if (fallbackElement != null) {
                    targetHtml = fallbackElement.outerHtml();
                    log.info("使用 .sheet-container，长度: {}", targetHtml.length());
                } else {
                    // 最后备选：整个 .main 区域
                    Element mainElement = doc.selectFirst(".main, main");
                    if (mainElement != null) {
                        targetHtml = mainElement.outerHtml();
                        log.info("使用 .main 作为备选，长度: {}", targetHtml.length());
                    }
                }
            }
            
            // ============================================
            // 处理 HTML 内容
            // ============================================
            if (!targetHtml.isEmpty()) {
                // 解析 HTML 以提取 meta 和移除 header
                Document targetDoc = Jsoup.parse(targetHtml);
                
                // 1. 提取 xhe-meta 内容保存到 meta 字段
                Element metaElement = targetDoc.selectFirst(".xhe-meta");
                if (metaElement != null) {
                    String metaText = metaElement.text();
                    song.setMeta(metaText);
                    log.info("提取 xhe-meta 内容: {}", metaText);
                }
                
                // 2. 移除 xhe-header 元素（不保存）
                Elements headerElements = targetDoc.select(".xhe-header");
                if (!headerElements.isEmpty()) {
                    headerElements.remove();
                    log.info("已移除 {} 个 xhe-header 元素", headerElements.size());
                }
                
                // 3. 清洗 HTML：简化 SVG 标签，移除不必要的元素
                targetHtml = cleanHtmlContent(targetDoc.body().html());
                log.info("清洗后的 HTML 长度: {}", targetHtml.length());
            }
            
            // 规范化 HTML → TabDocument JSON 入库（不再保留原始 HTML）
            if (!targetHtml.isEmpty()) {
                String tabJson = tabHtmlNormalizer.normalizeToJson(targetHtml, song.getSongName());
                song.setTabContentJson(tabJson);
                log.info("成功规范化吉他谱 JSON，长度: {}（源 HTML 长度: {}）",
                        tabJson.length(), targetHtml.length());

                boolean hasChords = targetHtml.contains("xhe-chord-anchor") || targetHtml.contains("hexi-chord");
                log.info("是否包含和弦信息: {}", hasChords ? "是" : "否");
            } else {
                log.warn("未找到有效的吉他谱内容");
            }

            // 解析歌词 - 从 sheet-container 内提取
            if (sheetContainer != null) {
                String lyrics = sheetContainer.text();
                song.setLyrics(lyrics);
                log.info("从 .sheet-container 提取歌词长度: {}", lyrics.length());
            } else {
                Element lyricsElement = doc.selectFirst("article, main");
                if (lyricsElement != null) {
                    String lyrics = lyricsElement.text();
                    song.setLyrics(lyrics);
                    log.info("从备用元素提取歌词长度: {}", lyrics.length());
                }
            }

            // 解析其他信息
            parseTabInfo(doc, song);

            song.setStatus(1);
            song.setViewCount(0);
            song.setFavoriteCount(0);

            log.info("爬取成功: {} - {}, meta: {}", 
                    song.getSongName(), song.getArtist(), 
                    song.getMeta() != null ? song.getMeta() : "无");
            
            return song;

        } catch (Exception e) {
            log.error("Selenium 爬取失败: {}", e.getMessage(), e);
            throw new RuntimeException("爬取失败: " + e.getMessage(), e);
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.error("关闭浏览器失败", e);
                }
            }
        }
    }

    /**
     * 清洗 HTML 内容 - 简化 SVG 和移除不必要的元素
     */
    private String cleanHtmlContent(String html) {
        try {
            Document doc = Jsoup.parse(html);
            
            // 1. 清洗 SVG - 只保留 text 标签
            var svgElements = doc.select("svg");
            log.info("找到 {} 个 SVG 元素需要清洗", svgElements.size());
            
            int cleanedCount = 0;
            for (Element svg : svgElements) {
                var textElements = svg.select("text");
                
                if (!textElements.isEmpty()) {
                    svg.empty();
                    for (Element text : textElements) {
                        svg.appendChild(text.clone());
                    }
                    
                    // 调整 SVG 尺寸
                    svg.attr("width", "40");
                    svg.attr("height", "20");
                    
                    cleanedCount++;
                }
            }
            log.info("成功清洗 {} 个 SVG 元素", cleanedCount);
            
            // 2. 移除 xhe-info 标签（唱、编信息）
            var xheInfoElements = doc.select(".xhe-info");
            log.info("找到 {} 个 .xhe-info 元素需要移除", xheInfoElements.size());
            for (Element element : xheInfoElements) {
                element.remove();
            }
            
            // 3. 移除 xhe-body 的 padding-top 样式
            var xheBodyElements = doc.select(".xhe-body");
            log.info("找到 {} 个 .xhe-body 元素需要清理样式", xheBodyElements.size());
            for (Element element : xheBodyElements) {
                String style = element.attr("style");
                if (style != null && !style.isEmpty()) {
                    // 移除 padding-top
                    style = style.replaceAll("padding-top\\s*:\\s*[^;]+;?", "");
                    // 清理多余的空格和分号
                    style = style.trim().replaceAll(";+", ";");
                    if (style.endsWith(";")) {
                        style = style.substring(0, style.length() - 1);
                    }
                    element.attr("style", style);
                }
            }
            
            return doc.body().html();
            
        } catch (Exception e) {
            log.error("清洗 HTML 内容失败: {}", e.getMessage(), e);
            return html; // 失败时返回原始 HTML
        }
    }

    /**
     * 解析标题（格式: 歌名 - 歌手 吉他弹唱谱）
     */
    private void parseTitle(String title, Song song) {
        if (title != null && title.contains("-")) {
            String[] parts = title.split("-");
            if (parts.length >= 2) {
                song.setSongName(parts[0].trim());
                String artistPart = parts[1].trim();
                // 移除 "吉他弹唱谱" 等后缀
                artistPart = artistPart.replaceAll("吉他.*谱.*$", "").trim();
                song.setArtist(artistPart);
            }
        } else {
            song.setSongName(title);
            song.setArtist("未知");
        }
    }

    /**
     * 解析吉他谱详细信息
     */
    private void parseTabInfo(Document doc, Song song) {
        try {
            // 查找包含调式、变调夹等信息的元素
            Element metaElement = doc.selectFirst(".meta, .info, .tab-info");
            if (metaElement != null) {
                String metaText = metaElement.text();
                
                // 解析原调
                if (metaText.contains("原调")) {
                    String originalKey = extractValue(metaText, "原调[:：]\\s*([A-G][#b]?m?)");
                    song.setOriginalKey(originalKey);
                }
                
                // 解析选调
                if (metaText.contains("选调") || metaText.contains("演奏调")) {
                    String playKey = extractValue(metaText, "[选演奏]调[:：]\\s*([A-G][#b]?m?)");
                    song.setPlayKey(playKey);
                }
                
                // 解析变调夹
                if (metaText.contains("变调夹")) {
                    String capoStr = extractValue(metaText, "变调夹[:：]\\s*(\\d+)");
                    if (capoStr != null) {
                        try {
                            song.setCapo(Integer.parseInt(capoStr));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                
                // 解析节拍
                if (metaText.contains("节拍")) {
                    String beat = extractValue(metaText, "节拍[:：]\\s*(\\d+/\\d+)");
                    song.setBeat(beat);
                }
            }
        } catch (Exception e) {
            log.warn("解析吉他谱信息失败: {}", e.getMessage());
        }
    }

    /**
     * 从文本中提取匹配的值
     */
    private String extractValue(String text, String pattern) {
        try {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(text);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            log.debug("提取值失败: {}", e.getMessage());
        }
        return null;
    }
}
