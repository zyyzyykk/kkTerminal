package com.kkbpro.terminal.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageInfo {

    private Integer type;

    private String content;

    private Integer rows;

    private Integer cols;
}
