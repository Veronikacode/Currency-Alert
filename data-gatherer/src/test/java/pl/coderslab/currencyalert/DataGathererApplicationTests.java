package pl.coderslab.currencyalert;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.coderslab.gatherer.DataGathererApplication;

@SpringBootTest(classes = DataGathererApplication.class)
@ActiveProfiles("test")
class DataGathererApplicationTests {

    @Test
    void contextLoads() {
    }
}