package com.nykaa.loyalty;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public abstract class AbstractTest {

    private static boolean setUpIsDone = false;

    @BeforeEach
    public void setUp() {
        if(setUpIsDone == true)
            return;
        ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(SuperstoreLoyaltyApplication.class)
              .run("");
        setUpIsDone=true;
    }
}
