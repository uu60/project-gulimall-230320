package com.octopus.gulimall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopus.common.utils.PageUtils;
import com.octopus.common.utils.Query;
import com.octopus.gulimall.member.dao.MemberDao;
import com.octopus.gulimall.member.entity.MemberEntity;
import com.octopus.gulimall.member.entity.MemberLevelEntity;
import com.octopus.gulimall.member.exception.PhoneExistedException;
import com.octopus.gulimall.member.exception.UsernameExistedException;
import com.octopus.gulimall.member.service.MemberLevelService;
import com.octopus.gulimall.member.service.MemberService;
import com.octopus.gulimall.member.vo.GithubSocialUser;
import com.octopus.gulimall.member.vo.MemberLoginVo;
import com.octopus.gulimall.member.vo.MemberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo memberRegisterVo) {
        MemberEntity memberEntity = new MemberEntity();
        // 设置默认等级
        MemberLevelEntity memberLevelEntity = memberLevelService.getDefaultLevel();
        memberEntity.setLevelId(memberLevelEntity.getId());
        // 检查手机号和用户名是否唯一，不唯一就抛异常
        checkPhoneUnique(memberRegisterVo.getPhone());
        checkUsernameUnique(memberRegisterVo.getUserName());

        memberEntity.setMobile(memberRegisterVo.getPhone());
        memberEntity.setUsername(memberRegisterVo.getUserName());

        // 密码加密存储
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(memberRegisterVo.getPassword());
        memberEntity.setPassword(encode);

        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkUsernameUnique(String username) {
        Long count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new UsernameExistedException();
        }
    }

    @Override
    public void checkPhoneUnique(String phone) {
        Long count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneExistedException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo memberLoginVo) {
        String username = memberLoginVo.getLoginacct();
        String password = memberLoginVo.getPassword();

        // 1. 去数据库查询
        MemberEntity memberEntity =
                baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", username).or().eq("mobile",
                        username));

        if (memberEntity == null) {
            return null;
        } else {
            String realPassword = memberEntity.getPassword();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(password, realPassword);
            if (matches) {
                return memberEntity;
            } else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity login(GithubSocialUser user) {
        String id = user.getId();
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("uid", id));
        if (memberEntity != null) {
            // 这个用户已经注册
            memberEntity.setAccessToken(user.getAccessToken());
            baseMapper.updateById(memberEntity);
            return memberEntity;
        }
        // 需要注册一个
        memberEntity = new MemberEntity();
        memberEntity.setUid(id);
        memberEntity.setNickname(user.getLogin());
        memberEntity.setAccessToken(user.getAccessToken());
        baseMapper.insert(memberEntity);
        return memberEntity;
    }

}