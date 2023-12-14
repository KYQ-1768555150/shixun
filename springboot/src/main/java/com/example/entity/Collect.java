package com.example.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Collect implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID */
    private Integer id;
    private Integer userId;
    private Integer businessId;
    private Integer goodsId;

    private String businessName;
    private String goodsName;
    private String goodsImg;
    private String goodUnit;
    private Double goodsPrice;
}