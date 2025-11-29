package com.kkbpro.terminal.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协作相关
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CooperateInfo {

    private String id;

    private Boolean readOnly;

    private Integer maxCapacity;

}
