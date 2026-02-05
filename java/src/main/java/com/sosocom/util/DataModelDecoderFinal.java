package com.sosocom.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * data-model 解密 - 按位取反方案
 */
public class DataModelDecoderFinal {

    public static void main(String[] args) {
        String encoded = "%C3%9B%C3%99%C3%87%C2%89%C3%98%C2%87%C3%98%C3%8C%C2%89%C3%99%C3%87%C3%9D%C3%93%C3%82%C3%8E%C3%8E%C2%89%C3%85%C3%98%C3%98%C2%9B%C2%87%C2%9B%C2%87%C2%89%C2%89%C3%8A%C2%91%C2%89%C3%8E%C3%9E%C2%87%C2%89%C2%87%C3%B0%C3%8D%C3%B3%C3%8E%C2%9D%C3%85%C2%89%C2%89%C3%85%1B%C3%85%C3%96%C2%89%C3%BB%C3%98%C3%99%C3%98%C2%89%C2%89%C3%8E%C2%9E%C2%92%C2%9C%C3%92%C3%98%C3%82%C3%87%C3%85%C3%8E%C2%93%C3%A7%C2%89%C2%89%C2%89%C3%B9%C3%8E%C3%83%C3%8E%C3%8E%C3%A6%C2%89%C2%91%C3%91%C2%91%C2%89%C2%89%C2%89%C3%B0%C2%87%C3%87%C3%96%C2%9C%C3%84%C3%B2%C3%8F%C3%82%C3%99%C2%89%C2%89%C2%92%C3%83%C2%87%C3%84%C3%8A%C3%8E%C3%85%C2%91%C3%8E%C3%98%C3%8E%C2%89%C2%92L%C3%8A%C2%9E%C3%8E%C3%83%C3%83%C2%91%C3%AA%C3%98%C2%89%C3%82%C3%98%C2%9A%C3%AA%C2%9D%C2%89%C3%9B%C2%9A%C3%8D%C2%89%C3%80%C2%91%C3%BF%C3%9B%C3%8A%C3%98%C3%99(%C3%BB%C3%9B%C2%89%C3%8A%C3%99%C3%82%C3%98%C2%89%C2%91%C3%8C%C3%9B%C3%8A%C3%8E%C3%A5%C2%87%C2%99%C3%88%C2%98%C2%87%C3%88%C2%9B%C3%85%C2%91%C2%9A%C2%91%C2%89%C3%87%C2%89%C2%87%C2%81%C2%9C%C3%9E%C2%89%C2%89%C3%8A%C3%83%C2%89%C3%98%C2%89%C2%89%C2%91%C3%8E%C3%8E%C3%8E%C3%84%C3%83%C2%89%C3%8A%C2%91%C3%87%C3%9C%C2%89%C2%89%C3%87%C3%82%C3%9F%C2%89%C3%9F%C3%B6%C2%85%C3%87%C2%89%C2%89%C3%84%C3%8F%C2%9A%C2%89%C2%89%C2%89%C2%89%C3%99%C3%8E%C3%9B%C3%98%C3%85%C3%BC%C2%89%C2%9B%C3%82%C2%93%C2%89%C2%89%C2%89%C2%89%C2%92%C2%91%C3%88%C2%87%C3%84%C3%90%C3%9F%13%C3%85%C3%8E%C2%93%C2%86%C2%9B%C3%8E%C2%89%C2%89%C3%81%C2%9D%C3%99%C2%91%C2%9D%C3%84%C3%8D%C3%8A%C3%8D%C3%8A%C3%8F%C2%9A%C3%98%C3%B0%C3%8E%C3%BA%C2%91%C2%9E%C3%87%C3%8D%C3%82%C3%86%C2%87%C2%98%C3%88%C2%89%C3%82%C3%8E%C3%84%C3%8C%C3%8C%C2%9B%C3%8A%C3%8A%C2%9F%C3%82%C2%89%C2%89%C3%A2%C3%87%05%C3%8A%C2%81%C2%87%C3%8E%13%C3%9F%C2%93%C3%85%C3%98%C3%99%C3%BA%C3%A5%C3%9A%C3%83%C3%8A%C3%9C%C3%9C%C3%82%C3%8E%C3%98%C2%89%C3%87%C3%8E%C3%8A%C3%8A%C3%83%C2%87%C2%9E%C3%98%C3%84%C2%92%C2%89%C2%87%C3%8E%C2%92%C3%99%C2%89%C2%98%C3%8F%C3%9D%C2%92%C3%88%C3%84%C2%99%C2%9B%C3%B0%C3%8A%C3%99%C3%84%C3%87%C3%A8%C3%8E%C3%BB%C3%8F%C3%93%C3%8E%C3%8A%C3%82%C3%98%C2%89%C2%89%C3%98%C3%8C%C3%9F%C3%86%C3%84%3B%C3%85%C3%83%C3%A7%C3%98%C3%9E%C3%9F%C2%87%C2%87%C3%98%C3%98%C3%8E%C3%99%C2%81%C2%99%C3%87%C3%89%C2%89%C3%8A%C3%A7%C3%8E%C3%89%C3%9B%C3%98%C3%8E%C3%92%C3%84%C2%89%C2%87%C3%9E%C2%89%C3%B8%C2%9B%C2%87%C2%9D%C3%98%C2%91%C2%9A%C2%89%C2%91%C3%90%C3%85%C3%98%C3%8A%C3%B3%C2%89%C3%8E%C3%85%C2%91%C3%98%C2%89%C2%89%C3%B8%C2%9F%C2%9B%C2%99%C2%9A%C3%9F%C2%87%C3%98%C2%89%C3%BF%C3%98%C2%9A%C2%87%C2%99%C2%89%C2%91M%C3%86%C2%89%C2%87%C2%91%C3%A7%C3%9C%C2%9A%C3%AA%C3%8A%C3%8A%C3%98%C3%9F%C2%9B%C3%B8%C3%9E%C2%89%C3%98%C3%8E%C3%92%C3%8D%C3%8A%C3%B6%C2%91%C3%BC%C3%98%C3%84%C3%85%C3%8F%C3%8F%C3%86%C2%87%C3%85%C3%98%C2%91%C2%89%C2%9A%C2%9A%C3%9E%C2%87%C3%8D%C3%9F%C2%89%C3%99%C3%82%C3%91%C3%84%C3%BF%C3%85%C3%82%C3%93%C2%9B%C3%82%C3%93%C3%A5%C2%89%C3%87%C3%8A%09%C3%8E%C2%91%C3%9F%C3%98%C3%9A%C3%86%C3%8E%C3%87%C3%84%C3%8E%C2%89%C3%9F%C3%BD%C2%91%C3%85%C2%89%C2%87%C2%91%C3%9E%C3%B3%C3%BD%C2%91%C2%87%C3%8E%C3%8A%C3%82%C3%8E%C3%99%C2%9C%C2%91%C3%87%C2%89%C3%9F%C3%8E%C3%8AN%C2%9A%C3%AA%C2%89%C3%99%C3%82%C3%99%C3%85%C3%B6%C3%85%C3%99%C3%BA%C3%8E%C3%9B%C3%9F%C2%87%C2%89%C2%89%C3%99%C3%83%C3%9F%C3%83%C3%92%C3%86%C3%8D%C2%89%C3%98%C3%B6%C3%B0%C3%9B%C3%85%C3%9F%C2%9C%C3%9E%C3%8E%C3%8A%C3%B3%C3%9E%C3%8A%C3%A7%C3%85%C3%98%C3%82%C2%87%C2%92%C3%8A%C3%84%C3%87%C3%9E%C3%9F%C3%9E%C2%89%C3%91%C3%BD%C2%89%C3%8A%C3%8D%C3%98%C2%89%C2%89%C3%8A%C3%9F%C3%8E%C3%8D%C2%89%C3%9B%C3%99%C3%8E%C2%89%C3%A6%C3%8E%C3%99%C2%89%C3%80%C3%9E%C3%8A%C3%98%C3%8A%C3%8E%C3%86%C3%897%C3%88%C3%84%C2%91%C2%89%C2%91%C2%91%C3%82%C3%88%C2%87%C3%8F%C2%9B%C3%91%C2%9C%C3%BC%C2%89%C2%9A%C2%9A%C3%87%C3%98%C2%91%C2%87%C3%A4%C3%87%C3%AA%C2%91%C3%88%C2%89%C3%82%C3%9B%C3%99%C3%AA%C3%8E%C3%B9%C2%9B%C2%98%C2%91%C3%85N%C3%85%C3%8E%C2%9A%C2%9D%C3%87%C3%B6%C2%98%C3%AF%C3%87";
        
        System.out.println("=== 解密 data-model (按位取反) ===\n");
        
        try {
            // 1. URL 解码
            String urlDecoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8);
            
            // 2. 转换为 Latin-1 字节（原始字节）
            byte[] latin1Bytes = urlDecoded.getBytes(StandardCharsets.ISO_8859_1);
            
            // 3. 按位取反解密
            byte[] decrypted = new byte[latin1Bytes.length];
            for (int i = 0; i < latin1Bytes.length; i++) {
                decrypted[i] = (byte)(~latin1Bytes[i] & 0xFF);
            }
            
            // 4. 转换为字符串
            String result = new String(decrypted, StandardCharsets.UTF_8);
            
            System.out.println("✅ 解密成功!");
            System.out.println("\n解密后内容:");
            System.out.println("=".repeat(80));
            System.out.println(result);
            System.out.println("=".repeat(80));
            
            // 5. 统计分析
            System.out.println("\n统计信息:");
            System.out.println("  原始长度: " + latin1Bytes.length + " 字节");
            System.out.println("  解密后长度: " + result.length() + " 字符");
            System.out.println("  是否包含JSON特征: " + result.contains("{"));
            
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 解密方法（可在其他类中调用）
     */
    public static String decrypt(String encoded) {
        try {
            String urlDecoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8);
            byte[] latin1Bytes = urlDecoded.getBytes(StandardCharsets.ISO_8859_1);
            byte[] decrypted = new byte[latin1Bytes.length];
            for (int i = 0; i < latin1Bytes.length; i++) {
                decrypted[i] = (byte)(~latin1Bytes[i] & 0xFF);
            }
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }
}
