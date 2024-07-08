package com.github.senocak.service;

import com.github.senocak.config.DataSourceConfig;
import com.github.senocak.domain.User;
import com.github.senocak.util.AppConstants;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class Listeners {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Listeners.class);
    private final DataSourceConfig dataSourceConfig;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public Listeners(DataSourceConfig dataSourceConfig, UserService userService, PasswordEncoder passwordEncoder) {
        this.dataSourceConfig = dataSourceConfig;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent(ApplicationReadyEvent event) {
        if (event.getApplicationContext().getEnvironment().acceptsProfiles(Profiles.of("integration-test"))) {
            return;
        }

        if ("create".equals(dataSourceConfig.getDdl())) {
            final String pass = passwordEncoder.encode("asenocak");
            User user1 = new User("anil1", "anil1@senocak.com", pass,
                    List.of(AppConstants.RoleName.ROLE_USER.getRole(), AppConstants.RoleName.ROLE_ADMIN.getRole()),
                    new Date());
            user1.setId(UUID.fromString("2cb9374e-4e52-4142-a1af-16144ef4a27d"));
            userService.save(user1);

            User user2 = new User("anil2", "anil2@gmail.com", pass,
                    List.of(AppConstants.RoleName.ROLE_USER.getRole()),
                    new Date());
            user2.setId(UUID.fromString("3cb9374e-4e52-4142-a1af-16144ef4a27d"));
            userService.save(user2);

            User user3 = new User("anil3", "anil3@gmail.com", pass,
                    List.of(AppConstants.RoleName.ROLE_USER.getRole()), null);
            user3.setId(UUID.fromString("4cb9374e-4e52-4142-a1af-16144ef4a27d"));
            userService.save(user3);

            log.info("Seeding completed");
        }
    }
}