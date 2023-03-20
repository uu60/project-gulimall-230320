package com.octopus.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopus.common.to.MemberPrice;
import com.octopus.common.to.SkuReductionTo;
import com.octopus.common.utils.PageUtils;
import com.octopus.common.utils.Query;
import com.octopus.gulimall.coupon.dao.SkuFullReductionDao;
import com.octopus.gulimall.coupon.entity.MemberPriceEntity;
import com.octopus.gulimall.coupon.entity.SkuFullReductionEntity;
import com.octopus.gulimall.coupon.entity.SkuLadderEntity;
import com.octopus.gulimall.coupon.service.MemberPriceService;
import com.octopus.gulimall.coupon.service.SkuFullReductionService;
import com.octopus.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;
    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo reductionTo) {
        // 1 保存满减打折，会员价
        if (reductionTo.getFullCount() > 0) {
            SkuLadderEntity ladderEntity = new SkuLadderEntity();
            BeanUtils.copyProperties(reductionTo, ladderEntity);
            ladderEntity.setAddOther(reductionTo.getCountStatus());
            skuLadderService.save(ladderEntity);
        }

        // 2 保存满减信息
        if (reductionTo.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
            SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
            BeanUtils.copyProperties(reductionTo, reductionEntity);
            this.save(reductionEntity);
        }

        // 3 保存会员价格
        List<MemberPrice> memberPrices = reductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrices.stream().map(price -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(reductionTo.getSkuId());
            memberPriceEntity.setMemberLevelId(price.getId());
            memberPriceEntity.setMemberLevelName(price.getName());
            memberPriceEntity.setMemberPrice(price.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).filter(entity -> entity.getMemberPrice().compareTo(new BigDecimal("0")) > 0).collect(Collectors.toList());

        memberPriceService.saveBatch(collect);
    }

}