package com.octopus.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PurchaseDoneVo {

    @NotNull
    // 采购单id
    private Long id;
    private List<PurchaseDoneItemVo> items;

}
