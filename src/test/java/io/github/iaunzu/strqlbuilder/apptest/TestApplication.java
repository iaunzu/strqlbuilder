package io.github.iaunzu.strqlbuilder.apptest;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = TestApplication.class)
@EnableAutoConfiguration
@ActiveProfiles("h2")
@Disabled
@Transactional
public class TestApplication {}
