package com.octopus.gulimall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author octopus
 * @date 2023/3/31 20:00
 */
@Data
public class MemberRegisterVo {
    private String userName;

    private String password;

    private String phone;
}
