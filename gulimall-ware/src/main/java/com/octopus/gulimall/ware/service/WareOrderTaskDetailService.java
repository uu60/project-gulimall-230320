package com.octopus.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.octopus.common.utils.PageUtils;
import com.octopus.gulimall.ware.entity.WareOrderTaskDetailEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author octopus
 * @email djz6660@icloud.com
 * @date 2023-03-20 02:13:16
 */
public interface WareOrderTaskDetailService extends IService<WareOrderTaskDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

