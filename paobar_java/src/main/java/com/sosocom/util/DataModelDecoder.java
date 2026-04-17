package com.sosocom.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * data-model 解密工具
 */
public class DataModelDecoder {

    public static void main(String[] args) {
        String encoded = "%C3%9B%C3%99%C3%87%C2%89%C3%98%C2%87%C3%98%C3%8C%C2%89%C3%99%C3%87%C3%9D%C3%93%C3%82%C3%8E%C3%8E%C2%89%C3%85%C3%98%C3%98%C2%9B%C2%87%C2%9B%C2%87%C2%89%C2%89%C3%8A%C2%91%C2%89%C3%8E%C3%9E%C2%87%C2%89%C2%87%C3%B0%C3%8D%C3%B3%C3%8E%C2%9D%C3%85%C2%89%C2%89%C3%85%1B%C3%85%C3%96%C2%89%C3%BB%C3%98%C3%99%C3%98%C2%89%C2%89%C3%8E%C2%9E%C2%92%C2%9C%C3%92%C3%98%C3%82%C3%87%C3%85%C3%8E%C2%93%C3%A7%C2%89%C2%89%C2%89%C3%B9%C3%8E%C3%83%C3%8E%C3%8E%C3%A6%C2%89%C2%91%C3%91%C2%91%C2%89%C2%89%C2%89%C3%B0%C2%87%C3%87%C3%96%C2%9C%C3%84%C3%B2%C3%8F%C3%82%C3%99%C2%89%C2%89%C2%92%C3%83%C2%87%C3%84%C3%8A%C3%8E%C3%85%C2%91%C3%8E%C3%98%C3%8E%C2%89%C2%92L%C3%8A%C2%9E%C3%8E%C3%83%C3%83%C2%91%C3%AA%C3%98%C2%89%C3%82%C3%98%C2%9A%C3%AA%C2%9D%C2%89%C3%9B%C2%9A%C3%8D%C2%89%C3%80%C2%91%C3%BF%C3%9B%C3%8A%C3%98%C3%99(%C3%BB%C3%9B%C2%89%C3%8A%C3%99%C3%82%C3%98%C2%89%C2%91%C3%8C%C3%9B%C3%8A%C3%8E%C3%A5%C2%87%C2%99%C3%88%C2%98%C2%87%C3%88%C2%9B%C3%85%C2%91%C2%9A%C2%91%C2%89%C3%87%C2%89%C2%87%C2%81%C2%9C%C3%9E%C2%89%C2%89%C3%8A%C3%83%C2%89%C3%98%C2%89%C2%89%C2%91%C3%8E%C3%8E%C3%8E%C3%84%C3%83%C2%89%C3%8A%C2%91%C3%87%C3%9C%C2%89%C2%89%C3%87%C3%82%C3%9F%C2%89%C3%9F%C3%B6%C2%85%C3%87%C2%89%C2%89%C3%84%C3%8F%C2%9A%C2%89%C2%89%C2%89%C2%89%C3%99%C3%8E%C3%9B%C3%98%C3%85%C3%BC%C2%89%C2%9B%C3%82%C2%93%C2%89%C2%89%C2%89%C2%89%C2%92%C2%91%C3%88%C2%87%C3%84%C3%90%C3%9F%13%C3%85%C3%8E%C2%93%C2%86%C2%9B%C3%8E%C2%89%C2%89%C3%81%C2%9D%C3%99%C2%91%C2%9D%C3%84%C3%8D%C3%8A%C3%8D%C3%8A%C3%8F%C2%9A%C3%98%C3%B0%C3%8E%C3%BA%C2%91%C2%9E%C3%87%C3%8D%C3%82%C3%86%C2%87%C2%98%C3%88%C2%89%C3%82%C3%8E%C3%84%C3%8C%C3%8C%C2%9B%C3%8A%C3%8A%C2%9F%C3%82%C2%89%C2%89%C3%A2%C3%87%05%C3%8A%C2%81%C2%87%C3%8E%13%C3%9F%C2%93%C3%85%C3%98%C3%99%C3%BA%C3%A5%C3%9A%C3%83%C3%8A%C3%9C%C3%9C%C3%82%C3%8E%C3%98%C2%89%C3%87%C3%8E%C3%8A%C3%8A%C3%83%C2%87%C2%9E%C3%98%C3%84%C2%92%C2%89%C2%87%C3%8E%C2%92%C3%99%C2%89%C2%98%C3%8F%C3%9D%C2%92%C3%88%C3%84%C2%99%C2%9B%C3%B0%C3%8A%C3%99%C3%84%C3%87%C3%A8%C3%8E%C3%BB%C3%8F%C3%93%C3%8E%C3%8A%C3%82%C3%98%C2%89%C2%89%C3%98%C3%8C%C3%9F%C3%86%C3%84%3B%C3%85%C3%83%C3%A7%C3%98%C3%9E%C3%9F%C2%87%C2%87%C3%98%C3%98%C3%8E%C3%99%C2%81%C2%99%C3%87%C3%89%C2%89%C3%8A%C3%A7%C3%8E%C3%89%C3%9B%C3%98%C3%8E%C3%92%C3%84%C2%89%C2%87%C3%9E%C2%89%C3%B8%C2%9B%C2%87%C2%9D%C3%98%C2%91%C2%9A%C2%89%C2%91%C3%90%C3%85%C3%98%C3%8A%C3%B3%C2%89%C3%8E%C3%85%C2%91%C3%98%C2%89%C2%89%C3%B8%C2%9F%C2%9B%C2%99%C2%9A%C3%9F%C2%87%C3%98%C2%89%C3%BF%C3%98%C2%9A%C2%87%C2%99%C2%89%C2%91M%C3%86%C2%89%C2%87%C2%91%C3%A7%C3%9C%C2%9A%C3%AA%C3%8A%C3%8A%C3%98%C3%9F%C2%9B%C3%B8%C3%9E%C2%89%C3%98%C3%8E%C3%92%C3%8D%C3%8A%C3%B6%C2%91%C3%BC%C3%98%C3%84%C3%85%C3%8F%C3%8F%C3%86%C2%87%C3%85%C3%98%C2%91%C2%89%C2%9A%C2%9A%C3%9E%C2%87%C3%8D%C3%9F%C2%89%C3%99%C3%82%C3%91%C3%84%C3%BF%C3%85%C3%82%C3%93%C2%9B%C3%82%C3%93%C3%A5%C2%89%C3%87%C3%8A%09%C3%8E%C2%91%C3%9F%C3%98%C3%9A%C3%86%C3%8E%C3%87%C3%84%C3%8E%C2%89%C3%9F%C3%BD%C2%91%C3%85%C2%89%C2%87%C2%91%C3%9E%C3%B3%C3%BD%C2%91%C2%87%C3%8E%C3%8A%C3%82%C3%8E%C3%99%C2%9C%C2%91%C3%87%C2%89%C3%9F%C3%8E%C3%8AN%C2%9A%C3%AA%C2%89%C3%99%C3%82%C3%99%C3%85%C3%B6%C3%85%C3%99%C3%BA%C3%8E%C3%9B%C3%9F%C2%87%C2%89%C2%89%C3%99%C3%83%C3%9F%C3%83%C3%92%C3%86%C3%8D%C2%89%C3%98%C3%B6%C3%B0%C3%9B%C3%85%C3%9F%C2%9C%C3%9E%C3%8E%C3%8A%C3%B3%C3%9E%C3%8A%C3%A7%C3%85%C3%98%C3%82%C2%87%C2%92%C3%8A%C3%84%C3%87%C3%9E%C3%9F%C3%9E%C2%89%C3%91%C3%BD%C2%89%C3%8A%C3%8D%C3%98%C2%89%C2%89%C3%8A%C3%9F%C3%8E%C3%8D%C2%89%C3%9B%C3%99%C3%8E%C2%89%C3%A6%C3%8E%C3%99%C2%89%C3%80%C3%9E%C3%8A%C3%98%C3%8A%C3%8E%C3%86%C3%897%C3%88%C3%84%C2%91%C2%89%C2%91%C2%91%C3%82%C3%88%C2%87%C3%8F%C2%9B%C3%91%C2%9C%C3%BC%C2%89%C2%9A%C2%9A%C3%87%C3%98%C2%91%C2%87%C3%A4%C3%87%C3%AA%C2%91%C3%88%C2%89%C3%82%C3%9B%C3%99%C3%AA%C3%8E%C3%B9%C2%9B%C2%98%C2%91%C3%85N%C3%85%C3%8E%C2%9A%C2%9D%C3%87%C3%B6%C2%98%C3%AF%C3%87";
        
        System.out.println("=== 分析 data-model 编码 ===\n");
        
        // 步骤 1: URL 解码
        try {
            String urlDecoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8);
            System.out.println("1. URL 解码后长度: " + urlDecoded.length());
            System.out.println("   前100字符: " + urlDecoded.substring(0, Math.min(100, urlDecoded.length())));
            System.out.println("   字符分布:");
            analyzeCharacters(urlDecoded);
            
            // 步骤 2: 尝试 Base64 解码
            System.out.println("\n2. 尝试 Base64 解码:");
            try {
                byte[] base64Decoded = Base64.getDecoder().decode(urlDecoded);
                String base64String = new String(base64Decoded, StandardCharsets.UTF_8);
                System.out.println("   Base64 解码成功!");
                System.out.println("   解码后内容: " + base64String.substring(0, Math.min(200, base64String.length())));
            } catch (Exception e) {
                System.out.println("   ❌ 不是 Base64 编码");
            }
            
            // 步骤 3: 字节分析
            System.out.println("\n3. 字节分析:");
            byte[] bytes = urlDecoded.getBytes(StandardCharsets.UTF_8);
            System.out.println("   字节长度: " + bytes.length);
            System.out.println("   前20字节 (十六进制):");
            for (int i = 0; i < Math.min(20, bytes.length); i++) {
                System.out.printf("   [%2d] 0x%02X (%3d) '%c'\n", 
                    i, bytes[i] & 0xFF, bytes[i] & 0xFF, 
                    isPrintable((char)(bytes[i] & 0xFF)) ? (char)(bytes[i] & 0xFF) : '.');
            }
            
            // 步骤 4: XOR 解密尝试
            System.out.println("\n4. 尝试简单 XOR 解密:");
            tryXorDecrypt(bytes);
            
            // 步骤 5: 位移解密
            System.out.println("\n5. 尝试位移解密:");
            tryShiftDecrypt(bytes);
            
            // 步骤 6: 寻找 JSON 模式
            System.out.println("\n6. 寻找可能的 JSON 结构:");
            findJsonPatterns(urlDecoded);
            
        } catch (Exception e) {
            System.err.println("解码失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void analyzeCharacters(String str) {
        int ascii = 0, utf8 = 0, control = 0, printable = 0;
        for (char c : str.toCharArray()) {
            if (c < 128) ascii++;
            else utf8++;
            if (c < 32 || c == 127) control++;
            if (c >= 32 && c < 127) printable++;
        }
        System.out.printf("   ASCII: %d, UTF-8扩展: %d, 控制字符: %d, 可打印: %d\n", 
            ascii, utf8, control, printable);
    }
    
    private static boolean isPrintable(char c) {
        return c >= 32 && c < 127;
    }
    
    private static void tryXorDecrypt(byte[] bytes) {
        // 尝试常见的 XOR 密钥
        int[] keys = {0x42, 0x1B, 0x5A, 0x7F, 0xFF, 0xAA, 0x55};
        for (int key : keys) {
            StringBuilder result = new StringBuilder();
            boolean likelyCorrect = true;
            
            for (int i = 0; i < Math.min(50, bytes.length); i++) {
                int decrypted = (bytes[i] & 0xFF) ^ key;
                char c = (char) decrypted;
                
                // 检查是否像有效的文本
                if (decrypted < 32 || decrypted > 126) {
                    if (decrypted != 10 && decrypted != 13) { // 允许换行
                        likelyCorrect = false;
                    }
                }
                result.append(isPrintable(c) ? c : '.');
            }
            
            if (likelyCorrect) {
                System.out.printf("   XOR 0x%02X 可能: %s\n", key, result.toString());
            }
        }
    }
    
    private static void tryShiftDecrypt(byte[] bytes) {
        // 尝试减去一个固定值
        int[] shifts = {1, 2, 3, 5, 10, 32, 64, 128};
        for (int shift : shifts) {
            StringBuilder result = new StringBuilder();
            int validChars = 0;
            
            for (int i = 0; i < Math.min(50, bytes.length); i++) {
                int decrypted = (bytes[i] & 0xFF) - shift;
                if (decrypted < 0) decrypted += 256;
                
                char c = (char) decrypted;
                if (isPrintable(c) || c == '\n' || c == '\r') {
                    validChars++;
                }
                result.append(isPrintable(c) ? c : '.');
            }
            
            if (validChars > 40) { // 80%以上是有效字符
                System.out.printf("   位移 -%d 可能: %s\n", shift, result.toString());
            }
        }
    }
    
    private static void findJsonPatterns(String str) {
        // 寻找可能的 JSON 标记
        String[] patterns = {"{", "}", "[", "]", ":", "\"", "title", "chord", "lyrics", "content"};
        for (String pattern : patterns) {
            int count = str.split(pattern, -1).length - 1;
            if (count > 0) {
                System.out.printf("   '%s' 出现: %d 次\n", pattern, count);
            }
        }
    }
}
