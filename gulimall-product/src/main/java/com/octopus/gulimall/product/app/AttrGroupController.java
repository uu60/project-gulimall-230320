package com.octopus.gulimall.product.app;

import com.octopus.common.utils.PageUtils;
import com.octopus.common.utils.R;
import com.octopus.gulimall.product.entity.AttrEntity;
import com.octopus.gulimall.product.entity.AttrGroupEntity;
import com.octopus.gulimall.product.service.AttrAttrgroupRelationService;
import com.octopus.gulimall.product.service.AttrGroupService;
import com.octopus.gulimall.product.service.AttrService;
import com.octopus.gulimall.product.service.CategoryService;
import com.octopus.gulimall.product.vo.AttrGroupRelationVo;
import com.octopus.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 属性分组
 *
 * @author djz
 * @email djz6660@icloud.com
 * @date 2022-09-22 14:33:14
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private AttrAttrgroupRelationService relationService;

    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos) {
        relationService.saveBatch(vos);
        return R.ok();
    }

    @GetMapping("/{categoryId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("categoryId") Long categoryId) {
        // 查出当前分类下的所有属性分组
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrsByCategoryId(categoryId);
        return R.ok().put("data", vos);
    }

    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> entities = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", entities);
    }

    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId, @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.getNoRelationAttr(params, attrgroupId);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{categoryId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("categoryId") Long categoryId) {
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils pageUtils = attrGroupService.queryPage(params, categoryId);
        return R.ok().put("page", pageUtils);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long categoryId = attrGroup.getCategoryId();
        Long[] path = categoryService.findCategoryPath(categoryId);
        attrGroup.setCategoryPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);
        return R.ok();
    }

    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos) {
        attrService.deleteRelation(vos);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
