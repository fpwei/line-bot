package org.fpwei.line.core.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("org.fpwei.line.core.dao")
@EntityScan("org.fpwei.line.core.entity")
public class CoreConfig {
}
