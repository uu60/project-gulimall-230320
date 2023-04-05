package com.octopus.gulimall.member.exception;

/**
 * @author octopus
 * @date 2023/3/31 20:47
 */
public class PhoneExistedException extends RuntimeException {

    public PhoneExistedException() {
        super("手机号已存在");
    }
}
