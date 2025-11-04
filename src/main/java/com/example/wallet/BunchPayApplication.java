package com.example.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class BunchPayApplication {

	public static void main(String[] args) {
		// Forzar IPv4 para evitar problemas con resoluciones IPv6 en conexiones
		// externas (DB, etc.)
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.net.preferIPv6Addresses", "false");
		SpringApplication.run(BunchPayApplication.class, args);
	}

}
