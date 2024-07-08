package com.github.senocak;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class HepsiEmlakApplication {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplicationBuilder(HepsiEmlakApplication.class)
				.bannerMode(Banner.Mode.CONSOLE)
				.logStartupInfo(true)
				.build();
		app.run(args);
	}
}
