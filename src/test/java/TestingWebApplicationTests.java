
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyhao.springwebapps.*;
import com.fyhao.springwebapps.dto.AgentProfileDto;
import com.fyhao.springwebapps.dto.AgentSkillDto;
import com.fyhao.springwebapps.dto.SkillDto;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

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
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/test/", String.class))
                .contains("Hello, World");
    }

    @Test
    public void testconversation() throws Exception {
        String conversationid = this.restTemplate.getForObject(
                "http://localhost:" + port + "/webchat/createconversation?email=fyhao1@gmail.com", String.class);
        assertThat(getlastmessagefromparty(conversationid)).contains("system");
        assertThat(getlastmessagetoparty(conversationid)).contains("fyhao1@gmail.com");
        String sendmessageresult = this.restTemplate.getForObject(
                "http://localhost:" + port + "/webchat/sendmessage?id=" + conversationid + "&input=test1",
                String.class);
        assertThat(sendmessageresult).contains("0");
        assertThat(getlastmessagefromparty(conversationid)).contains("fyhao1@gmail.com");
        assertThat(getlastmessagetoparty(conversationid)).contains("bot");
        // https://8080-ad1cca16-319c-41ea-88af-31d7c741202d.ws-us02.gitpod.io/webchat/getmessagecount?id=2
        String messagecount = this.restTemplate.getForObject(
                "http://localhost:" + port + "/webchat/getmessagecount?id=" + conversationid, String.class);
        assertThat(messagecount).contains("2");
        sendmessageresult = this.restTemplate.getForObject(
                "http://localhost:" + port + "/webchat/sendmessage?id=" + conversationid + "&input=test1",
                String.class);
        assertThat(sendmessageresult).contains("0");
        // https://8080-ad1cca16-319c-41ea-88af-31d7c741202d.ws-us02.gitpod.io/webchat/getmessagecount?id=2
        messagecount = this.restTemplate.getForObject(
                "http://localhost:" + port + "/webchat/getmessagecount?id=" + conversationid, String.class);
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

    }

    @Test
    public void testhotelbotusecase() throws Exception {
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
        assertThat(getlastmessagecontent(conversationid4))
                .contains("Thank you for booking with us. What else we can help?");
        assertThat(getcontext(conversationid4, "finalbookinginfo")).contains("book time: 9:00am");
        // conversationid4 talking to bot book second time with negative confirmation
        assertThat(getcontext(conversationid4, "botmenu")).contains("home");
        sendmessage(conversationid4, "book hotel");
        assertThat(getcontext(conversationid4, "finalbookinginfo")).isNullOrEmpty();
        assertThat(getcontext(conversationid4, "botmenu")).contains("menubookhoteltime");
        sendmessage(conversationid4, "10:00am");
        assertThat(getlastmessagecontent(conversationid4)).contains("Confirm to book hotel on 10:00am?");
        sendmessage(conversationid4, "no");
        assertThat(getcontext(conversationid4, "botmenu")).contains("home");
        assertThat(getcontext(conversationid4, "finalbookinginfo")).isNullOrEmpty();
        // conversationid4 talking to bot but bot decided handover to agent
        assertThat(getcontext(conversationid4, "state")).contains("bot");
        sendmessage(conversationid4, "do you know about abcde?");
        assertThat(getcontext(conversationid4, "state")).contains("agent");
        // conversationid4 start to agent
        sendagentmessage(conversationid4, "sjeffers", "hello i am sjeffers, how can i help you?");
        assertThat(getlastmessagefromparty(conversationid4)).contains("sjeffers");
        assertThat(getlastmessagetoparty(conversationid4)).contains("fyhao1@gmail.com");
        sendmessage(conversationid4, "hi i have some issue");
        assertThat(getlastmessagefromparty(conversationid4)).contains("fyhao1@gmail.com");
        assertThat(getlastmessagetoparty(conversationid4)).contains("sjeffers");
        // customer initiated bye
        assertThat(getcontext(conversationid4, "state")).contains("agent");
        sendmessage(conversationid4, "bye");
        assertThat(getcontext(conversationid4, "state")).contains("end");
    }

    @Test
    public void testwebsocketconnectionforcustomer() throws Exception {
        CompletableFuture<String> futureConversationid = new CompletableFuture<>();
        CompletableFuture<String> futureTestCompletion = new CompletableFuture<>();
        List<String> testCaseCust = new ArrayList<String>();
        List<String> testCaseAns = new ArrayList<String>();
        String conversationid5 = createconversationwithchannel("fyhao1@gmail.com", "webchathotel");
        testCaseCust.add("Hello I need help");
        testCaseAns.add("This is abc hotel.");
        testCaseCust.add("book hotel");
        testCaseAns.add("When you want to book hotel?");
        testCaseCust.add("11:00am");
        testCaseAns.add("Confirm to book hotel on 11:00am?");
        testCaseCust.add("yes");
        testCaseAns.add("Thank you for booking with us. What else we can help?");
        testCaseCust.add("do you know about abcde?");
        testCaseAns.add("Sorry I am not understand. Will handover to agent.");
        testCaseAns.add("hello i am sjeffers, how can i help you?");
        try {
            WebSocketClient webSocketClient = new StandardWebSocketClient();

            WebSocketSession webSocketSession = webSocketClient.doHandshake(new TextWebSocketHandler() {
                int c = 0;
                boolean isAfterSendAgent = false;

                @Override
                public void handleTextMessage(WebSocketSession session, TextMessage message) {
                    // LOGGER.info("received message - " + message.getPayload());
                    JsonParser springParser = JsonParserFactory.getJsonParser();
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> jsonMap = springParser.parseMap(message.getPayload());
                    if (jsonMap.get("action").equals("connectionready")) {
                        String conversationid = (String) jsonMap.get("conversationid");
                        sendChatMessage(session, conversationid, testCaseCust.get(c));
                    } else if (jsonMap.get("action").equals("chatMessageReceived")) {
                        String conversationid = (String) jsonMap.get("conversationid");
                        String content = (String) jsonMap.get("content");
                        futureConversationid.complete(conversationid);
                        if (!content.equals(testCaseAns.get(c))) {
                            futureTestCompletion.complete("error");
                            return;
                        }
                        c++;
                        if (isAfterSendAgent) {
                            futureTestCompletion.complete("completed");
                            return;
                        }
                        if (c >= testCaseCust.size()) {
                            // test send agent message
                            sendagentmessage(conversationid5, "sjeffers", "hello i am sjeffers, how can i help you?");
                            isAfterSendAgent = true;
                        } else {
                            sendChatMessage(session, conversationid, testCaseCust.get(c));
                        }
                    }
                }

                @Override
                public void afterConnectionEstablished(WebSocketSession session) {
                    // LOGGER.info("established connection - " + session);
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> jsonMap = new HashMap<String, Object>();
                    jsonMap.put("action", "register");
                    jsonMap.put("conversationid", conversationid5);
                    jsonMap.put("serverport", port);
                    try {
                        String message = objectMapper.writeValueAsString(jsonMap);
                        session.sendMessage(new TextMessage(message));
                    } catch (Exception ex) {

                    }
                }

                private void sendChatMessage(WebSocketSession session, String conversationid, String chatMessage) {
                    Map<String, Object> jsonMap = new HashMap<String, Object>();
                    jsonMap.put("action", "sendChatMessage");
                    jsonMap.put("conversationid", conversationid);
                    jsonMap.put("chatMessage", chatMessage);
                    sendMessage(session, jsonMap);
                }

                private void sendMessage(WebSocketSession session, Map<String, Object> jsonMap) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        String message = objectMapper.writeValueAsString(jsonMap);
                        session.sendMessage(new TextMessage(message));
                    } catch (Exception ex) {

                    }
                }
            }, new WebSocketHttpHeaders(),
                    URI.create("ws://localhost:" + port + "/channel?conversationid=" + conversationid5)).get();

        } catch (Exception e) {
            // LOGGER.error("Exception while accessing websockets", e);
        }
        // hold and wait
        assertThat(futureConversationid.get(2, SECONDS)).contains(conversationid5);
        assertThat(futureTestCompletion.get(2, SECONDS)).contains("completed");
    }

    @Test
    public void testwebsocketconnectionforagent() throws Exception {
        CompletableFuture<String> futureTestCompletion = new CompletableFuture<>();

        try {
            WebSocketClient webSocketClient = new StandardWebSocketClient();

            WebSocketSession webSocketSession = webSocketClient.doHandshake(new TextWebSocketHandler() {
                int c = 0;
                boolean isAfterSendAgent = false;

                @Override
                public void handleTextMessage(WebSocketSession session, TextMessage message) {
                    JsonParser springParser = JsonParserFactory.getJsonParser();
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> jsonMap = springParser.parseMap(message.getPayload());
                    if (jsonMap.get("action").equals("connectionready")) {
                        futureTestCompletion.complete("completed");
                    }
                }

                @Override
                public void afterConnectionEstablished(WebSocketSession session) {
                    // LOGGER.info("established connection - " + session);
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> jsonMap = new HashMap<String, Object>();
                    jsonMap.put("action", "register");
                    jsonMap.put("agentid", "sjeffers");
                    jsonMap.put("serverport", port);
                    sendMessage(session, jsonMap);
                }

                private void sendChatMessage(WebSocketSession session, String conversationid, String chatMessage) {
                    Map<String, Object> jsonMap = new HashMap<String, Object>();
                    jsonMap.put("action", "sendChatMessage");
                    jsonMap.put("conversationid", conversationid);
                    jsonMap.put("chatMessage", chatMessage);
                    sendMessage(session, jsonMap);
                }

                private void sendMessage(WebSocketSession session, Map<String, Object> jsonMap) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        String message = objectMapper.writeValueAsString(jsonMap);
                        session.sendMessage(new TextMessage(message));
                    } catch (Exception ex) {

                    }
                }
            }, new WebSocketHttpHeaders(), URI.create("ws://localhost:" + port + "/agent")).get();

        } catch (Exception e) {
            // LOGGER.error("Exception while accessing websockets", e);
        }
        // hold and wait
        assertThat(futureTestCompletion.get(2, SECONDS)).contains("completed");
    }

    @Test
    public void testagentprofileservice() throws Exception {
        // create skills
        assertThat(getskillcount()).contains("0");
        createskillprofile("english");
        assertThat(getskillcount()).contains("1");
        createskillprofile("mandarin");
        assertThat(getskillcount()).contains("2");
        // create agents
        assertThat(getagentcount()).contains("0");
        createagentprofile("sjeffers");
        assertThat(getagentcount()).contains("1");
        createagentprofile("rbarrows");
        assertThat(getagentcount()).contains("2");
        // assign skills to agent
        assertThat(getskillnamesofagent("sjeffers")).hasSize(0);
        assertThat(getskillnamesofagent("rbarrows")).hasSize(0);
        assignagentskillaction("sjeffers", "english", AgentSkillDto.ASSIGNED_TO_AGENT);
        assertThat(getskillnamesofagent("sjeffers")).hasSize(1);
        assertThat(getskillnamesofagent("rbarrows")).hasSize(0);
        assignagentskillaction("sjeffers", "english", AgentSkillDto.ASSIGNED_TO_AGENT);
        assertThat(getskillnamesofagent("sjeffers")).hasSize(1);
        assertThat(getskillnamesofagent("rbarrows")).hasSize(0);
        assignagentskillaction("sjeffers", "mandarin", AgentSkillDto.ASSIGNED_TO_AGENT);
        assertThat(getskillnamesofagent("sjeffers")).hasSize(2);
        assertThat(getskillnamesofagent("rbarrows")).hasSize(0);
        // remove skills from agent
        assertThat(getskillnamesofagent("sjeffers")).contains("english");
        assertThat(getskillnamesofagent("sjeffers")).contains("mandarin");
        assignagentskillaction("sjeffers", "english", AgentSkillDto.REMOVED_FROM_AGENT);
        assertThat(getskillnamesofagent("sjeffers")).contains("mandarin");
        assertThat(getskillnamesofagent("sjeffers")).doesNotContain("english");
        assertThat(getskillnamesofagent("sjeffers")).hasSize(1);
        // assign agent to skill
        assertThat(getskillnamesofagent("rbarrows")).hasSize(0);
        assertThat(getskillnamesofagent("rbarrows")).doesNotContain("english");
        assignagentskillaction("rbarrows", "english", AgentSkillDto.ASSIGNED_TO_SKILL);
        assertThat(getskillnamesofagent("rbarrows")).contains("english");
        assertThat(getskillnamesofagent("sjeffers")).hasSize(1);
        assertThat(getskillnamesofagent("rbarrows")).hasSize(1);
        // remove agent from skill
        assignagentskillaction("rbarrows", "english", AgentSkillDto.REMOVED_FROM_SKILL);
        assertThat(getskillnamesofagent("rbarrows")).hasSize(0);
        assertThat(getskillnamesofagent("rbarrows")).doesNotContain("english");
        // register agent
        assertThat(getagentterminalscount()).contains("0");
        registeragent("sjeffers");
        assertThat(getagentterminalscount()).contains("1");
        registeragent("rbarrows");
        assertThat(getagentterminalscount()).contains("2");
        // unregister agent
        unregisteragent("sjeffers");
        assertThat(getagentterminalscount()).contains("1");
        unregisteragent("rbarrows");
        assertThat(getagentterminalscount()).contains("0");
    }
    private String registeragent(String agentName) {
        AgentProfileDto dto = new AgentProfileDto();
        dto.setName(agentName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = this.restTemplate.postForEntity("http://localhost:" + port + "/agentterminal/registeragent", request,
                String.class);
        return resp.getBody();
    }
    private String unregisteragent(String agentName) {
        AgentProfileDto dto = new AgentProfileDto();
        dto.setName(agentName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = this.restTemplate.postForEntity("http://localhost:" + port + "/agentterminal/unregisteragent", request,
                String.class);
        return resp.getBody();
    }
    private String getagentterminalscount() {
        return this.restTemplate.getForObject("http://localhost:" + port + "/agentterminal/getagentterminalscount",
                String.class);
    }
    private String createagentprofile(String agentName) {
        AgentProfileDto dto = new AgentProfileDto();
        dto.setName(agentName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = this.restTemplate.postForEntity("http://localhost:" + port + "/agentprofile/createagentprofile", request,
                String.class);
        return resp.getBody();
    }
    private String createskillprofile(String skillName) {
        SkillDto dto = new SkillDto();
        dto.setName(skillName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = this.restTemplate.postForEntity("http://localhost:" + port + "/agentprofile/createskillprofile", request,
                String.class);
        return resp.getBody();
    }
    private String assignagentskillaction(String agentName, String skillName, String action) {
        AgentSkillDto dto = new AgentSkillDto();
        dto.setAgent(agentName);
        dto.setSkill(skillName);
        dto.setAction(action);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = this.restTemplate.postForEntity("http://localhost:" + port + "/agentprofile/assignagentskillaction", request,
                String.class);
        return resp.getBody();
    }
    private List<String> getskillnamesofagent(String agent) {
        String resp = this.restTemplate.getForObject("http://localhost:" + port + "/agentprofile/getskillnamesofagent?agent=" + agent,
                String.class);
        JsonParser springParser = JsonParserFactory.getJsonParser();
        List<Object> list = springParser.parseList(resp);
        List<String> strList = list.stream()
                           .map( Object::toString )
                           .collect( Collectors.toList() );
        return strList;
    }
    private String getagentcount() {
        return this.restTemplate.getForObject("http://localhost:" + port + "/agentprofile/getagentcount",
                String.class);
    }
    private String getskillcount() {
        return this.restTemplate.getForObject("http://localhost:" + port + "/agentprofile/getskillcount",
                String.class);
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