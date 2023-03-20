package com.octopus.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopus.common.constant.WareConstant;
import com.octopus.common.utils.PageUtils;
import com.octopus.common.utils.Query;
import com.octopus.gulimall.ware.dao.PurchaseDao;
import com.octopus.gulimall.ware.entity.PurchaseDetailEntity;
import com.octopus.gulimall.ware.entity.PurchaseEntity;
import com.octopus.gulimall.ware.service.PurchaseDetailService;
import com.octopus.gulimall.ware.service.PurchaseService;
import com.octopus.gulimall.ware.service.WareSkuService;
import com.octopus.gulimall.ware.vo.MergeVo;
import com.octopus.gulimall.ware.vo.PurchaseDoneItemVo;
import com.octopus.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;
    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreveivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );
        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        } else {
            PurchaseEntity byId = this.getById(purchaseId);
            // 如果不是新建和分配状态（已经被领取开始采购了），就忽略合单
            if (byId.getStatus() != WareConstant.PurchaseStatusEnum.CREATED.getCode() && byId.getStatus() != WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return;
            }
        }

        // 获取到采购项ID
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().filter(item -> {
            PurchaseDetailEntity byId = purchaseDetailService.getById(item);
            return byId.getStatus() == WareConstant.PurchaseDetailStatusEnum.CREATED.getCode() || byId.getStatus() == WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode();
        }).map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();

            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Override
    public void received(List<Long> ids) {
        // 1. 确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity entity = this.getById(id);
            return entity;
        }).filter(entity ->
                entity.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() || entity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()
        ).map(entity -> {
            entity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            entity.setUpdateTime(new Date());
            return entity;
        }).collect(Collectors.toList());

        // 2. 改变采购单的状态
        this.updateBatchById(collect);

        // 3. 改变采购项的状态
        collect.forEach(entity -> {
            List<PurchaseDetailEntity> detailEntities = purchaseDetailService.listDetailByPurchaseId(entity.getId());
            detailEntities.forEach(entity1 -> {
                entity1.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
            });
            purchaseDetailService.updateBatchById(detailEntities);
        });
    }

    @Override
    @Transactional
    public void done(PurchaseDoneVo doneVo) {
        // 1. 获取采购单id
        Long purchaseId = doneVo.getId();
        PurchaseEntity byId = getById(purchaseId);
        if (byId.getStatus() == WareConstant.PurchaseStatusEnum.FINISHED.getCode()) {
            return;
        }

        // 2. 改变采购项的状态
        List<PurchaseDoneItemVo> items = doneVo.getItems();
        Boolean flag = true;
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseDoneItemVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HAS_ERROR.getCode()) {
                flag = false;
                detailEntity.setStatus(item.getStatus());
            } else {
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode());
                // 3. 将成功采购的进行入库
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }
        purchaseDetailService.updateBatchById(updates);

        // 改变采购单的状态
        PurchaseEntity entity = new PurchaseEntity();
        entity.setId(purchaseId);
        entity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISHED.getCode() :
                WareConstant.PurchaseStatusEnum.HAS_ERROR.getCode());
        this.updateById(entity);
    }

}