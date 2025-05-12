package org.ganjp.blog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String getWelcomeMessage() {
        return "Welcome to Gan Jian Ping Blog APIs";
    }
}

