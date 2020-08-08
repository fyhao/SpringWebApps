
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
    @Test
	public void testconversation() throws Exception {
        String conversationid = this.restTemplate.getForObject("http://localhost:" + port + "/webchat/createconversation?email=fyhao1@gmail.com",
                String.class);
        System.out.println("Created conversation id: " + conversationid);
        assertThat(conversationid).contains("2");
        String sendmessageresult = this.restTemplate.getForObject("http://localhost:" + port + "/webchat/sendmessage?id=" + conversationid + "&input=test1",
                String.class);
        assertThat(sendmessageresult).contains("0");
        //https://8080-ad1cca16-319c-41ea-88af-31d7c741202d.ws-us02.gitpod.io/webchat/getmessagecount?id=2
        String messagecount = this.restTemplate.getForObject("http://localhost:" + port + "/webchat/getmessagecount?id=" + conversationid,
                String.class);
        assertThat(messagecount).contains("1");
        sendmessageresult = this.restTemplate.getForObject("http://localhost:" + port + "/webchat/sendmessage?id=" + conversationid + "&input=test1",
                String.class);
        assertThat(sendmessageresult).contains("0");
        //https://8080-ad1cca16-319c-41ea-88af-31d7c741202d.ws-us02.gitpod.io/webchat/getmessagecount?id=2
        messagecount = this.restTemplate.getForObject("http://localhost:" + port + "/webchat/getmessagecount?id=" + conversationid,
                String.class);
        assertThat(messagecount).contains("2");
        String context = getcontext(conversationid, "state");
        assertThat(context).contains("bot");
        sendmessage(conversationid, "transferagent");
        context = getcontext(conversationid, "state");
        assertThat(context).contains("agent");
        String channel = getchannel(conversationid);
        assertThat(channel).contains("webchat");
    }
    private void sendmessage(String conversationid, String input) {
        this.restTemplate.getForObject("http://localhost:" + port + "/webchat/sendmessage?id=" + conversationid + "&input=transferagent",
                String.class);
    }
    private String getcontext(String conversationid, String key) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/webchat/findcontext?id=" + conversationid + "&key=" + key,
                String.class);
    }
    private String getchannel(String conversationid) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/webchat/findchannel?id=" + conversationid + "&key=state",
                String.class);
    }
}