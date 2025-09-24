package pl.coderslab.provider.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/rates/test")
    public String test() {
        return "DataProvider działa ✅";
    }
}
