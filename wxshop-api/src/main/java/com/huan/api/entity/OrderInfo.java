package com.huan.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OrderInfo {
    List<GoodsInfo> goods;
}
