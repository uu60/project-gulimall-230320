package com.octopus.gulimall.product.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.octopus.common.utils.PageUtils;
import com.octopus.common.utils.R;
import com.octopus.gulimall.product.entity.BrandEntity;
import com.octopus.gulimall.product.entity.CategoryBrandRelationEntity;
import com.octopus.gulimall.product.service.CategoryBrandRelationService;
import com.octopus.gulimall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 品牌分类关联
 *
 * @author djz
 * @email djz6660@icloud.com
 * @date 2022-09-22 14:33:14
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 获取品牌关联的分类
     */
    @GetMapping("/category/list")
    //@RequiresPermissions("product:categorybrandrelation:list")
    public R categoryList(@RequestParam("brandId") Long brandId){
        List<CategoryBrandRelationEntity> data =
                categoryBrandRelationService.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));

        return R.ok().put("data", data);
    }

    @GetMapping("/brands/list")
    public R relationBrandList(@RequestParam(value = "catId", required = true) Long catId) {
        List<BrandEntity> entities = categoryBrandRelationService.getBrandsByCatId(catId);
        List<BrandVo> collect = entities.stream().map(entity -> {
            BrandVo brandVo = new BrandVo();
            brandVo.setBrandId(entity.getBrandId());
            brandVo.setBrandName(entity.getName());
            return brandVo;
        }).collect(Collectors.toList());
        return R.ok().put("data", collect);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
