
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
        assertThat(getlastmessagecontent(conversationid)).contains("you are chatting with our agent");
        assertThat(getcontext(conversationid, "state")).contains("agent");
        assertThat(getchannel(conversationid)).contains("webchat");
        assertThat(getcontactscount()).contains("1");
        String conversationid2 = createconversation("fyhao1@gmail.com");
        assertThat(getcontactscount()).contains("1");
        assertThat(getchannel(conversationid2)).contains("webchat");
        assertThat(getmessagecount(conversationid)).contains("5");
        assertThat(getmessagecount(conversationid2)).contains("1");
        sendmessage(conversationid2, "hello");
        assertThat(getmessagecount(conversationid)).contains("5");
        assertThat(getmessagecount(conversationid2)).contains("2");
        sendmessage(conversationid, "hello");
        assertThat(getmessagecount(conversationid)).contains("6");
        assertThat(getmessagecount(conversationid2)).contains("2");
        String conversationid3 = createconversation("fyhao2@gmail.com");
        assertThat(getcontactscount()).contains("2");
        assertThat(getchannel(conversationid2)).contains("webchat");
        assertThat(getmessagecount(conversationid)).contains("6");
        assertThat(getmessagecount(conversationid2)).contains("2");
        assertThat(getmessagecount(conversationid3)).contains("1");
        assertThat(getconversationendtime(conversationid)).isNullOrEmpty();
        sendagentmessage(conversationid, "sjeffers", "I am agent how can i help u?");
        assertThat(getmessagecount(conversationid)).contains("7");
        assertThat(getlastmessagefromparty(conversationid)).contains("sjeffers");
        assertThat(getlastmessagetoparty(conversationid)).contains("fyhao1@gmail.com");
        assertThat(getlastmessagecontent(conversationid)).contains("I am agent how can i help u?");
        sendmessage(conversationid, "bye");
        assertThat(getconversationendtime(conversationid)).isNotNull();
        assertThat(getconversationendtime(conversationid2)).isNullOrEmpty();
        assertThat(getcontext(conversationid, "state")).contains("end");
        assertThat(getcontext(conversationid2, "state")).doesNotContain("end");
        sendmessage(conversationid2, "transferagentfail");
        assertThat(getcontext(conversationid2, "state")).doesNotContain("agent");
        assertThat(getlastmessagecontent(conversationid2)).contains("agent not available");
        // conversationid4 used for full testing now
        String conversationid4 = createconversationwithchannel("fyhao1@gmail.com", "webchathotel");
        assertThat(getchannel(conversationid4)).contains("webchathotel");
        assertThat(getcontext(conversationid4, "state")).contains("bot");
        // conversationid4 talking to bot
        sendmessage(conversationid4, "hello");
        assertThat(getlastmessagecontent(conversationid4)).contains("This is abc hotel.");
        assertThat(getlastmessagefromparty(conversationid4)).contains("bot");
        assertThat(getlastmessagetoparty(conversationid4)).contains("fyhao1@gmail.com");
        assertThat(getcontext(conversationid4, "botmenu")).contains("home");
        sendmessage(conversationid4, "book hotel");
        assertThat(getlastmessagecontent(conversationid4)).contains("When you want to book hotel?");
        assertThat(getcontext(conversationid4, "botmenu")).contains("menubookhoteltime");
        assertThat(getlastmessagefromparty(conversationid4)).contains("bot");
        assertThat(getlastmessagetoparty(conversationid4)).contains("fyhao1@gmail.com");
        sendmessage(conversationid4, "9:00am");
        assertThat(getlastmessagecontent(conversationid4)).contains("Confirm to book hotel on 9:00am?");
        sendmessage(conversationid4, "yes");
        assertThat(getlastmessagecontent(conversationid4)).contains("Thank you for booking with us. What else we can help?");
        assertThat(getcontext(conversationid4, "finalbookinginfo")).contains("book time: 9:00am");
        // conversationid4 talking to bot book second time with negative confirmation
        assertThat(getcontext(conversationid4, "botmenu")).contains("home");
        sendmessage(conversationid4, "book hotel");
        assertThat(getcontext(conversationid4, "finalbookinginfo")).isNullOrEmpty();
        assertThat(getcontext(conversationid4, "botmenu")).contains("menubookhoteltime");
        sendmessage(conversationid4, "9:00am");
        sendmessage(conversationid4, "no");
        assertThat(getcontext(conversationid4, "botmenu")).contains("home");
        assertThat(getcontext(conversationid4, "finalbookinginfo")).isNullOrEmpty();
        // conversationid4 talking to bot but bot decided handover to agent
        assertThat(getcontext(conversationid4, "state")).contains("bot");
        sendmessage(conversationid4, "do you know about abcde?");
        assertThat(getcontext(conversationid4, "state")).contains("agent");
        // conversationid4 start to agent
        // TODO sendagentmessage
    }
    private String createconversation(String email) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/webchat/createconversation?email=" + email,
                String.class);
    }
    private String createconversationwithchannel(String email, String channel) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/webchat/createconversationwithchannel?email=" + email + "&channel=" + channel,
                String.class);
    }
    private void sendmessage(String conversationid, String input) {
        this.restTemplate.getForObject("http://localhost:" + port + "/webchat/sendmessage?id=" + conversationid + "&input=" + input,
                String.class);
    }
    private void sendagentmessage(String conversationid, String agentname, String input) {
        this.restTemplate.getForObject("http://localhost:" + port + "/webchat/sendagentmessage?id=" + conversationid + "&agentname=" + agentname + "&input=" + input,
                String.class);
    }
    private void sendbotmessage(String conversationid, String input) {
        this.restTemplate.getForObject("http://localhost:" + port + "/webchat/sendbotmessage?id=" + conversationid + "&input=" + input,
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
    }
    private String getlastmessagetoparty(String conversationid) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/webchat/getlastmessagetoparty?id=" + conversationid,
                String.class);
    }
    private String getlastmessagecontent(String conversationid) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/webchat/getlastmessagecontent?id=" + conversationid,
                String.class);
    }
}