
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

import com.fyhao.springwebapps.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = SpringWebMain.class)
public class TestingWebApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    TestController test;
    @Test
    public void contextLoads() {
        
    }
	@Test
	public void greetingShouldReturnDefaultMessage() throws Exception {
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/test/",
				String.class)).contains("Hello, World");
	}
}