package com.choikang.back.exception;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/errorPage", produces = "application/text; charset=utf8")
public class ExceptionController {
    @GetMapping("404")
    public String show404ErrorPage() {
        return "error/404";
    }

    @GetMapping("500")
    public String show500ErrorPage() {
        return "error/500";
    }
}
