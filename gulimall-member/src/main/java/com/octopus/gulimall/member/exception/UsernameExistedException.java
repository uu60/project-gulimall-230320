package com.octopus.gulimall.member.exception;

/**
 * @author octopus
 * @date 2023/3/31 20:47
 */
public class UsernameExistedException extends RuntimeException {

    public UsernameExistedException() {
        super("用户名已存在");
    }
}
