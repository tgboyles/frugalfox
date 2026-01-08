package com.tgboyles.frugalfox;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HelloController {

@GetMapping("/")
public String index() {
	return "Welcome to the Frugal Fox API! This is the base route. Nothing to see here.";
}

}
