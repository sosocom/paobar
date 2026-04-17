package com.sosocom.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * data-model 解密工具 V2 - 深度分析
 */
public class DataModelDecoderV2 {

    public static void main(String[] args) {
        String encoded = "%C3%9B%C3%99%C3%87%C2%89%C3%98%C2%87%C3%98%C3%8C%C2%89%C3%99%C3%87%C3%9D%C3%93%C3%82%C3%8E%C3%8E%C2%89%C3%85%C3%98%C3%98%C2%9B%C2%87%C2%9B%C2%87%C2%89%C2%89%C3%8A%C2%91%C2%89%C3%8E%C3%9E%C2%87%C2%89%C2%87%C3%B0%C3%8D%C3%B3%C3%8E%C2%9D%C3%85%C2%89%C2%89%C3%85%1B%C3%85%C3%96%C2%89%C3%BB%C3%98%C3%99%C3%98%C2%89%C2%89%C3%8E%C2%9E%C2%92%C2%9C%C3%92%C3%98%C3%82%C3%87%C3%85%C3%8E%C2%93%C3%A7%C2%89%C2%89%C2%89%C3%B9%C3%8E%C3%83%C3%8E%C3%8E%C3%A6%C2%89%C2%91%C3%91%C2%91%C2%89%C2%89%C2%89%C3%B0%C2%87%C3%87%C3%96%C2%9C%C3%84%C3%B2%C3%8F%C3%82%C3%99%C2%89%C2%89%C2%92%C3%83%C2%87%C3%84%C3%8A%C3%8E%C3%85%C2%91%C3%8E%C3%98%C3%8E%C2%89%C2%92L%C3%8A%C2%9E%C3%8E%C3%83%C3%83%C2%91%C3%AA%C3%98%C2%89%C3%82%C3%98%C2%9A%C3%AA%C2%9D%C2%89%C3%9B%C2%9A%C3%8D%C2%89%C3%80%C2%91%C3%BF%C3%9B%C3%8A%C3%98%C3%99(%C3%BB%C3%9B%C2%89%C3%8A%C3%99%C3%82%C3%98%C2%89%C2%91%C3%8C%C3%9B%C3%8A%C3%8E%C3%A5%C2%87%C2%99%C3%88%C2%98%C2%87%C3%88%C2%9B%C3%85%C2%91%C2%9A%C2%91%C2%89%C3%87%C2%89%C2%87%C2%81%C2%9C%C3%9E%C2%89%C2%89%C3%8A%C3%83%C2%89%C3%98%C2%89%C2%89%C2%91%C3%8E%C3%8E%C3%8E%C3%84%C3%83%C2%89%C3%8A%C2%91%C3%87%C3%9C%C2%89%C2%89%C3%87%C3%82%C3%9F%C2%89%C3%9F%C3%B6%C2%85%C3%87%C2%89%C2%89%C3%84%C3%8F%C2%9A%C2%89%C2%89%C2%89%C2%89%C3%99%C3%8E%C3%9B%C3%98%C3%85%C3%BC%C2%89%C2%9B%C3%82%C2%93%C2%89%C2%89%C2%89%C2%89%C2%92%C2%91%C3%88%C2%87%C3%84%C3%90%C3%9F%13%C3%85%C3%8E%C2%93%C2%86%C2%9B%C3%8E%C2%89%C2%89%C3%81%C2%9D%C3%99%C2%91%C2%9D%C3%84%C3%8D%C3%8A%C3%8D%C3%8A%C3%8F%C2%9A%C3%98%C3%B0%C3%8E%C3%BA%C2%91%C2%9E%C3%87%C3%8D%C3%82%C3%86%C2%87%C2%98%C3%88%C2%89%C3%82%C3%8E%C3%84%C3%8C%C3%8C%C2%9B%C3%8A%C3%8A%C2%9F%C3%82%C2%89%C2%89%C3%A2%C3%87%05%C3%8A%C2%81%C2%87%C3%8E%13%C3%9F%C2%93%C3%85%C3%98%C3%99%C3%BA%C3%A5%C3%9A%C3%83%C3%8A%C3%9C%C3%9C%C3%82%C3%8E%C3%98%C2%89%C3%87%C3%8E%C3%8A%C3%8A%C3%83%C2%87%C2%9E%C3%98%C3%84%C2%92%C2%89%C2%87%C3%8E%C2%92%C3%99%C2%89%C2%98%C3%8F%C3%9D%C2%92%C3%88%C3%84%C2%99%C2%9B%C3%B0%C3%8A%C3%99%C3%84%C3%87%C3%A8%C3%8E%C3%BB%C3%8F%C3%93%C3%8E%C3%8A%C3%82%C3%98%C2%89%C2%89%C3%98%C3%8C%C3%9F%C3%86%C3%84%3B%C3%85%C3%83%C3%A7%C3%98%C3%9E%C3%9F%C2%87%C2%87%C3%98%C3%98%C3%8E%C3%99%C2%81%C2%99%C3%87%C3%89%C2%89%C3%8A%C3%A7%C3%8E%C3%89%C3%9B%C3%98%C3%8E%C3%92%C3%84%C2%89%C2%87%C3%9E%C2%89%C3%B8%C2%9B%C2%87%C2%9D%C3%98%C2%91%C2%9A%C2%89%C2%91%C3%90%C3%85%C3%98%C3%8A%C3%B3%C2%89%C3%8E%C3%85%C2%91%C3%98%C2%89%C2%89%C3%B8%C2%9F%C2%9B%C2%99%C2%9A%C3%9F%C2%87%C3%98%C2%89%C3%BF%C3%98%C2%9A%C2%87%C2%99%C2%89%C2%91M%C3%86%C2%89%C2%87%C2%91%C3%A7%C3%9C%C2%9A%C3%AA%C3%8A%C3%8A%C3%98%C3%9F%C2%9B%C3%B8%C3%9E%C2%89%C3%98%C3%8E%C3%92%C3%8D%C3%8A%C3%B6%C2%91%C3%BC%C3%98%C3%84%C3%85%C3%8F%C3%8F%C3%86%C2%87%C3%85%C3%98%C2%91%C2%89%C2%9A%C2%9A%C3%9E%C2%87%C3%8D%C3%9F%C2%89%C3%99%C3%82%C3%91%C3%84%C3%BF%C3%85%C3%82%C3%93%C2%9B%C3%82%C3%93%C3%A5%C2%89%C3%87%C3%8A%09%C3%8E%C2%91%C3%9F%C3%98%C3%9A%C3%86%C3%8E%C3%87%C3%84%C3%8E%C2%89%C3%9F%C3%BD%C2%91%C3%85%C2%89%C2%87%C2%91%C3%9E%C3%B3%C3%BD%C2%91%C2%87%C3%8E%C3%8A%C3%82%C3%8E%C3%99%C2%9C%C2%91%C3%87%C2%89%C3%9F%C3%8E%C3%8AN%C2%9A%C3%AA%C2%89%C3%99%C3%82%C3%99%C3%85%C3%B6%C3%85%C3%99%C3%BA%C3%8E%C3%9B%C3%9F%C2%87%C2%89%C2%89%C3%99%C3%83%C3%9F%C3%83%C3%92%C3%86%C3%8D%C2%89%C3%98%C3%B6%C3%B0%C3%9B%C3%85%C3%9F%C2%9C%C3%9E%C3%8E%C3%8A%C3%B3%C3%9E%C3%8A%C3%A7%C3%85%C3%98%C3%82%C2%87%C2%92%C3%8A%C3%84%C3%87%C3%9E%C3%9F%C3%9E%C2%89%C3%91%C3%BD%C2%89%C3%8A%C3%8D%C3%98%C2%89%C2%89%C3%8A%C3%9F%C3%8E%C3%8D%C2%89%C3%9B%C3%99%C3%8E%C2%89%C3%A6%C3%8E%C3%99%C2%89%C3%80%C3%9E%C3%8A%C3%98%C3%8A%C3%8E%C3%86%C3%897%C3%88%C3%84%C2%91%C2%89%C2%91%C2%91%C3%82%C3%88%C2%87%C3%8F%C2%9B%C3%91%C2%9C%C3%BC%C2%89%C2%9A%C2%9A%C3%87%C3%98%C2%91%C2%87%C3%A4%C3%87%C3%AA%C2%91%C3%88%C2%89%C3%82%C3%9B%C3%99%C3%AA%C3%8E%C3%B9%C2%9B%C2%98%C2%91%C3%85N%C3%85%C3%8E%C2%9A%C2%9D%C3%87%C3%B6%C2%98%C3%AF%C3%87";
        
        System.out.println("=== 深度分析 data-model 编码 ===\n");
        
        try {
            // URL 解码
            String urlDecoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8);
            byte[] bytes = urlDecoded.getBytes(StandardCharsets.UTF_8);
            
            System.out.println("1. UTF-8 字节序列分析:");
            System.out.println("   原始UTF-8字符被当作Latin-1读取，产生了双字节编码");
            System.out.println("   例如: C3 9B (UTF-8) -> Û (错误解读)");
            System.out.println("   正确做法: 获取原始字节");
            
            // 将错误的 UTF-8 转回 Latin-1 字节
            byte[] latin1Bytes = urlDecoded.getBytes(StandardCharsets.ISO_8859_1);
            System.out.println("\n2. 转换为 Latin-1 字节:");
            System.out.println("   字节长度: " + latin1Bytes.length);
            System.out.println("   前50字节 (十六进制):");
            for (int i = 0; i < Math.min(50, latin1Bytes.length); i++) {
                System.out.printf("%02X ", latin1Bytes[i] & 0xFF);
                if ((i + 1) % 16 == 0) System.out.println();
            }
            
            // 尝试解密：XOR、减法、异或
            System.out.println("\n\n3. 尝试XOR 0x89解密 (0x89出现频繁):");
            tryDecryptWithXor(latin1Bytes, 0x89, 100);
            
            System.out.println("\n4. 尝试减去固定值:");
            tryDecryptWithSubtraction(latin1Bytes, 128, 100);
            
            System.out.println("\n5. 按位取反:");
            tryDecryptWithBitwiseNot(latin1Bytes, 100);
            
            System.out.println("\n6. 尝试压缩数据:");
            tryDecompress(latin1Bytes);
            
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void tryDecryptWithXor(byte[] bytes, int key, int len) {
        StringBuilder result = new StringBuilder();
        int printable = 0;
        for (int i = 0; i < Math.min(len, bytes.length); i++) {
            int decrypted = (bytes[i] & 0xFF) ^ key;
            char c = (char) decrypted;
            if (c >= 32 && c < 127) printable++;
            result.append((c >= 32 && c < 127) ? c : '.');
        }
        System.out.printf("   可打印字符占比: %.1f%%\n", (printable * 100.0 / Math.min(len, bytes.length)));
        System.out.println("   结果: " + result.toString());
    }
    
    private static void tryDecryptWithSubtraction(byte[] bytes, int sub, int len) {
        StringBuilder result = new StringBuilder();
        int printable = 0;
        for (int i = 0; i < Math.min(len, bytes.length); i++) {
            int decrypted = (bytes[i] & 0xFF) - sub;
            if (decrypted < 0) decrypted += 256;
            char c = (char) decrypted;
            if (c >= 32 && c < 127) printable++;
            result.append((c >= 32 && c < 127) ? c : '.');
        }
        System.out.printf("   可打印字符占比: %.1f%%\n", (printable * 100.0 / Math.min(len, bytes.length)));
        System.out.println("   结果: " + result.toString());
    }
    
    private static void tryDecryptWithBitwiseNot(byte[] bytes, int len) {
        StringBuilder result = new StringBuilder();
        int printable = 0;
        for (int i = 0; i < Math.min(len, bytes.length); i++) {
            int decrypted = ~(bytes[i] & 0xFF) & 0xFF;
            char c = (char) decrypted;
            if (c >= 32 && c < 127) printable++;
            result.append((c >= 32 && c < 127) ? c : '.');
        }
        System.out.printf("   可打印字符占比: %.1f%%\n", (printable * 100.0 / Math.min(len, bytes.length)));
        System.out.println("   结果: " + result.toString());
    }
    
    private static void tryDecompress(byte[] bytes) {
        try {
            // 尝试 GZIP
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bytes);
            java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(bis);
            byte[] decompressed = gis.readAllBytes();
            System.out.println("   ✅ GZIP 解压成功! 长度: " + decompressed.length);
            System.out.println("   内容: " + new String(decompressed, StandardCharsets.UTF_8).substring(0, Math.min(200, decompressed.length)));
        } catch (Exception e) {
            System.out.println("   ❌ 不是 GZIP 压缩");
        }
        
        try {
            // 尝试 Deflate
            java.util.zip.Inflater inflater = new java.util.zip.Inflater();
            inflater.setInput(bytes);
            byte[] result = new byte[bytes.length * 10];
            int len = inflater.inflate(result);
            System.out.println("   ✅ Deflate 解压成功! 长度: " + len);
            System.out.println("   内容: " + new String(result, 0, Math.min(200, len), StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.out.println("   ❌ 不是 Deflate 压缩");
        }
    }
}
