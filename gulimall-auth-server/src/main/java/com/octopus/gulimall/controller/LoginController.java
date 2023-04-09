package com.octopus.gulimall.controller;

import com.octopus.common.utils.R;
import com.octopus.gulimall.feign.MemberFeignService;
import com.octopus.gulimall.vo.UserLoginVo;
import com.octopus.gulimall.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @author octopus
 * @date 2023/3/31 18:58
 */
@Controller
public class LoginController {

    @Autowired
    MemberFeignService memberFeignService;

    @PostMapping("/register")
    public String register(@Valid UserRegisterVo userRegisterVo, BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();

            result.getFieldErrors().forEach(fieldError -> {
                String msg = fieldError.getDefaultMessage();
                if (errors.containsKey(fieldError.getField())) {
                    msg = errors.get(fieldError.getField()) + "; " + fieldError.getDefaultMessage();
                }
                errors.put(fieldError.getField(), msg);
            });

            redirectAttributes.addFlashAttribute("errors", errors);
            // 校验出错转发到注册页
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        // 跳过验证码


        R resp = null;
        try {
            resp = memberFeignService.register(userRegisterVo);
            if (resp.getCode() == 0) { // 成功

                return "redirect:http://auth.gulimall.com/login.html";
            } else {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", (String) resp.get("msg"));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {
        R resp = memberFeignService.login(vo);
        // 远程登录
        if (resp.getCode() == 0) {
            return "redirect:http://gulimall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", (String) resp.get("msg"));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

}
