package com.octopus.gulimall.member.controller;

import com.octopus.common.exception.BizCodeEnum;
import com.octopus.common.utils.PageUtils;
import com.octopus.common.utils.R;
import com.octopus.gulimall.member.entity.MemberEntity;
import com.octopus.gulimall.member.exception.PhoneExistedException;
import com.octopus.gulimall.member.exception.UsernameExistedException;
import com.octopus.gulimall.member.feign.CouponFeignService;
import com.octopus.gulimall.member.service.MemberService;
import com.octopus.gulimall.member.vo.GithubSocialUser;
import com.octopus.gulimall.member.vo.MemberLoginVo;
import com.octopus.gulimall.member.vo.MemberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 会员
 *
 * @author djz
 * @email djz6660@icloud.com
 * @date 2022-09-22 15:35:45
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo memberRegisterVo) {
        try {
            memberService.register(memberRegisterVo);
        } catch (PhoneExistedException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UsernameExistedException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    @PostMapping("/oauth2/github/login")
    public R oauthLogin(@RequestBody GithubSocialUser user) {
        MemberEntity memberEntity = memberService.login(user);
        return memberEntity == null ? R.error() : R.ok().put("data", memberEntity);
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo memberLoginVo) {
        MemberEntity memberEntity = memberService.login(memberLoginVo);
        if (memberEntity != null) {
            return R.ok();
        } else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getCode(),
                    BizCodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getMsg());
        }
    }

    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("Job");
        R memberCoupons = couponFeignService.memberCoupons();
        return R.ok().put("member", memberEntity).put("coupons", memberCoupons.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
