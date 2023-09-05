package com.kkbpro.terminal.Pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class FrontSocketInfo {

    // 信息类型 0：文本命令 1：快捷键
    private Integer type;

    // 命令内容
    private String content;
}
