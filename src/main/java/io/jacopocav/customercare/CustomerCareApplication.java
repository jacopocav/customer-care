package io.jacopocav.customercare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CustomerCareApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerCareApplication.class, args);
    }
}
