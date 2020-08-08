
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
        assertThat(getlastmessagefromparty(conversationid)).contains("system");
        assertThat(getlastmessagetoparty(conversationid)).contains("fyhao1@gmail.com");
        String sendmessageresult = this.restTemplate.getForObject("http://localhost:" + port + "/webchat/sendmessage?id=" + conversationid + "&input=test1",
                String.class);
        assertThat(sendmessageresult).contains("0");
        assertThat(getlastmessagefromparty(conversationid)).contains("fyhao1@gmail.com");
        assertThat(getlastmessagetoparty(conversationid)).contains("bot");
        //https://8080-ad1cca16-319c-41ea-88af-31d7c741202d.ws-us02.gitpod.io/webchat/getmessagecount?id=2
        String messagecount = this.restTemplate.getForObject("http://localhost:" + port + "/webchat/getmessagecount?id=" + conversationid,
                String.class);
        assertThat(messagecount).contains("2");
        sendmessageresult = this.restTemplate.getForObject("http://localhost:" + port + "/webchat/sendmessage?id=" + conversationid + "&input=test1",
                String.class);
        assertThat(sendmessageresult).contains("0");
        //https://8080-ad1cca16-319c-41ea-88af-31d7c741202d.ws-us02.gitpod.io/webchat/getmessagecount?id=2
        messagecount = this.restTemplate.getForObject("http://localhost:" + port + "/webchat/getmessagecount?id=" + conversationid,
                String.class);
        assertThat(messagecount).contains("3");
        assertThat(getcontext(conversationid, "state")).contains("bot");
        sendmessage(conversationid, "transferagent");
        assertThat(getcontext(conversationid, "state")).contains("agent");
        assertThat(getchannel(conversationid)).contains("webchat");
        assertThat(getcontactscount()).contains("1");
        String conversationid2 = createconversation("fyhao1@gmail.com");
        assertThat(getcontactscount()).contains("1");
        assertThat(getchannel(conversationid2)).contains("webchat");
        assertThat(getmessagecount(conversationid)).contains("4");
        assertThat(getmessagecount(conversationid2)).contains("1");
        sendmessage(conversationid2, "hello");
        assertThat(getmessagecount(conversationid)).contains("4");
        assertThat(getmessagecount(conversationid2)).contains("2");
        sendmessage(conversationid, "hello");
        assertThat(getmessagecount(conversationid)).contains("5");
        assertThat(getmessagecount(conversationid2)).contains("2");
        String conversationid3 = createconversation("fyhao2@gmail.com");
        assertThat(getcontactscount()).contains("2");
        assertThat(getchannel(conversationid2)).contains("webchat");
        assertThat(getmessagecount(conversationid)).contains("5");
        assertThat(getmessagecount(conversationid2)).contains("2");
        assertThat(getmessagecount(conversationid3)).contains("1");
        assertThat(getconversationendtime(conversationid)).isNullOrEmpty();
        sendmessage(conversationid, "bye");
        assertThat(getconversationendtime(conversationid)).isNotNull();
        assertThat(getconversationendtime(conversationid2)).isNullOrEmpty();
        assertThat(getcontext(conversationid, "state")).contains("end");
        assertThat(getcontext(conversationid2, "state")).doesNotContain("end");
        sendmessage(conversationid2, "transferagentfail");
        assertThat(getcontext(conversationid2, "state")).doesNotContain("agent");
    }
    private String createconversation(String email) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/webchat/createconversation?email=" + email,
                String.class);
    }
    private void sendmessage(String conversationid, String input) {
        this.restTemplate.getForObject("http://localhost:" + port + "/webchat/sendmessage?id=" + conversationid + "&input=" + input,
                String.class);
    }
    private String getmessagecount(String conversationid) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/webchat/getmessagecount?id=" + conversationid,
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
    private String getcontactscount() {
        return this.restTemplate.getForObject("http://localhost:" + port + "/webchat/getcontactscount",
                String.class);
    }
    private String getconversationendtime(String conversationid) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/webchat/getconversationendtime?id=" + conversationid,
                String.class);
    }
    private String getlastmessagefromparty(String conversationid) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/webchat/getlastmessagefromparty?id=" + conversationid,
                String.class);
    }private String getlastmessagetoparty(String conversationid) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/webchat/getlastmessagetoparty?id=" + conversationid,
                String.class);
    }
}