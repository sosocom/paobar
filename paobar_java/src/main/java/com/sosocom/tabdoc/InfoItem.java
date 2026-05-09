package com.sosocom.tabdoc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * "唱: 李健"、"编: 菜鸟吉他" 这类标题下的 label-text 信息条目。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoItem {
    private String label;
    private String text;
}
