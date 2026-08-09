package com.fyhao.springwebapps;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

class SpringWebMainTest {
    @Test
    void applicationSupportsExecutableWarAndServletDeployment() {
        assertThat(SpringBootServletInitializer.class).isAssignableFrom(SpringWebMain.class);
    }
}
