package com.kkbpro.terminal.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 私钥
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateKey {

    private String content;

    private String passphrase;

}
