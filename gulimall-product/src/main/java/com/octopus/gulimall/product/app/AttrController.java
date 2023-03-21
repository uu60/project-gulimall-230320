package com.octopus.gulimall.product.app;

import com.octopus.common.utils.PageUtils;
import com.octopus.common.utils.R;
import com.octopus.gulimall.product.entity.ProductAttrValueEntity;
import com.octopus.gulimall.product.service.AttrService;
import com.octopus.gulimall.product.service.ProductAttrValueService;
import com.octopus.gulimall.product.vo.AttrRespVo;
import com.octopus.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品属性
 *
 * @author djz
 * @email djz6660@icloud.com
 * @date 2022-09-22 14:33:14
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;

    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrListForSpu(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity> entities = productAttrValueService.baseAttrListForSpu(spuId);
        return R.ok().put("data", entities);
    }

    @GetMapping("/{attrType}/list/{categoryId}")
    public R attrList(@RequestParam Map<String, Object> params, @PathVariable("attrType") String attrType, @PathVariable("categoryId") Long categoryId) {
        PageUtils page = attrService.queryAttrPage(params, attrType, categoryId);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
        AttrRespVo attrRespVo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", attrRespVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update/{spuId}")
    //@RequiresPermissions("product:attr:update")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId, @RequestBody List<ProductAttrValueEntity> entities){
		productAttrValueService.updateSpuAttr(spuId, entities);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
