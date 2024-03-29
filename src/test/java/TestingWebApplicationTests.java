
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.aspectj.lang.annotation.Before;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyhao.springwebapps.SpringWebMain;
import com.fyhao.springwebapps.TestController;
import com.fyhao.springwebapps.dto.AgentProfileDto;
import com.fyhao.springwebapps.dto.AgentSkillDto;
import com.fyhao.springwebapps.dto.CQueueDto;
import com.fyhao.springwebapps.dto.SkillDto;
import com.fyhao.springwebapps.entity.AgentTerminal;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = SpringWebMain.class)
public class TestingWebApplicationTests {

    @LocalServerPort
    int port;

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
    public void testall() throws Exception {
    	//testconversation();
    	//testhotelbotusecase();
    	//testwebsocketconnectionforcustomer();
    	testwebsocketconnectionforagent();
    	testagentprofileservice();

    	//testqueuemaxlimitreached();
    	//testqueuepriority();

    	//testbargeinConversation();
    	//testsimplifiedwebsocketclient();
    	//testmaxconcurrenttask();
    	//testtransfertasktoanotheragent();
    	
    	//testqueuemultiple();
    	//testconference();
    }
    
    //@Test
    public void testconversation() throws Exception {
        createskillprofile("hotel");
        createagentprofile("sjeffers");
        assignagentskillaction("sjeffers", "hotel", AgentSkillDto.ASSIGNED_TO_AGENT);
        registeragent("sjeffers");
        setagentstatus("sjeffers", AgentTerminal.READY);
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
        // Before transfer agent, sjeffers active task should be zero
        assertThat(getagenttaskscount("sjeffers")).contains("0");
        sendmessage(conversationid, "transferagent");
        assertThat(getlastmessagecontent(conversationid)).contains("you are chatting with our agent");
        assertThat(getcontext(conversationid, "state")).contains("agent");
        // After transfer agent, sjeffers active task should be 1
        assertThat(getagenttaskscount("sjeffers")).contains("1");
        assertThat(getchannel(conversationid)).contains("webchat");
        assertThat(getcontactscount()).contains("4");
        String conversationid2 = createconversation("fyhao1@gmail.com");
        assertThat(getcontactscount()).contains("4");
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
        assertThat(getcontactscount()).contains("4");
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
        // Before to test transferagentfail, we shall unregister agent first
        setagentstatus("sjeffers", AgentTerminal.NOT_READY);
        unregisteragent("sjeffers");
        sendmessage(conversationid2, "transferagentfail");
        assertThat(getcontext(conversationid2, "state")).doesNotContain("agent");
        assertThat(getlastmessagecontent(conversationid2)).contains("agent not available");

    }

    //@Test
    public void testhotelbotusecase() throws Exception {
        // conversationid4 used for full testing now
        // make an agent sjeffers ready to take chat task
        createskillprofile("hotel");
        createagentprofile("sjeffers1");
        assignagentskillaction("sjeffers1", "hotel", AgentSkillDto.ASSIGNED_TO_AGENT);
        registeragent("sjeffers1");
        setagentstatus("sjeffers1", AgentTerminal.READY);
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
        // Before transfer agent, sjeffers active task should be zero
        assertThat(getagenttaskscount("sjeffers1")).contains("0");
        sendmessage(conversationid4, "do you know about abcde?");
        try {Thread.sleep(100);} catch (Exception ex) {}
        assertThat(getcontext(conversationid4, "state")).contains("agent");
        // conversationid4 start to agent
        assertThat(getagenttaskscount("sjeffers1")).contains("1");
        sendagentmessage(conversationid4, "sjeffers1", "hello i am sjeffers, how can i help you?");
        assertThat(getlastmessagefromparty(conversationid4)).contains("sjeffers1");
        assertThat(getlastmessagetoparty(conversationid4)).contains("fyhao1@gmail.com");
        sendmessage(conversationid4, "hi i have some issue");
        assertThat(getlastmessagefromparty(conversationid4)).contains("fyhao1@gmail.com");
        assertThat(getlastmessagetoparty(conversationid4)).contains("sjeffers1");
        // customer initiated bye
        assertThat(getcontext(conversationid4, "state")).contains("agent");
        sendmessage(conversationid4, "bye");
        assertThat(getcontext(conversationid4, "state")).contains("end");
    }

    //@Test
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
        testCaseAns.add("Sorry I am not understand. But agent not available.");
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

    //@Test
    public void testwebsocketconnectionforagent() throws Exception {
        // create skills
        createskillprofile("english");
        createskillprofile("mandarin");
        // create agents
        createagentprofile("agent1");
        CompletableFuture<String> futureTestCompletion = new CompletableFuture<>();
        try {
            WebSocketClient webSocketClient = new StandardWebSocketClient();

            WebSocketSession webSocketSession = webSocketClient.doHandshake(new TextWebSocketHandler() {
                int c = 0;
                int testedScenario = 0;

                @Override
                public void handleTextMessage(WebSocketSession session, TextMessage message) {
                    JsonParser springParser = JsonParserFactory.getJsonParser();
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> jsonMap = springParser.parseMap(message.getPayload());
                    if (jsonMap.get("action").equals("connectionready")) {
                        if(getactiveagentterminalnames().contains("agent1")) {
                            setAgentStatus(session, "agent1", AgentTerminal.READY);
                            return;
                        }
                        else {
                            futureTestCompletion.complete("error");
                            return;
                        }
                    }
                    else if(jsonMap.get("action").equals("agentUnregistered")) {
                        String status = (String)jsonMap.get("status");
                        String msg = (String)jsonMap.get("msg");
                        if(testedScenario == 1) {
                            if(status.equals("0") && msg.equals("Failed as agent is not in not ready state")) {
                                testedScenario++;
                                setAgentStatus(session, "agent1", AgentTerminal.NOT_READY);
                            }
                        }
                        else if(testedScenario == 2) {
                            if(status.equals("1") && msg.equals("SUCCESS")) {
                                testedScenario++;
                                futureTestCompletion.complete("completed");
                            }
                        }
                    }
                    else if(jsonMap.get("action").equals("agentStatusChanged")) {
                        String agentid = (String)jsonMap.get("agentid");
                        String oldstatus = (String)jsonMap.get("oldstatus");
                        String newstatus = (String)jsonMap.get("newstatus");
                        if(testedScenario == 0) {
                            if(!agentid.equals("agent1")) {
                                futureTestCompletion.complete("error agentStatusChanged agentid as " + agentid + " instead of " + "agent1");
                                return;
                            }
                            if(!oldstatus.equals(AgentTerminal.NOT_READY)) {
                                futureTestCompletion.complete("error agentStatusChanged oldstatus as " + oldstatus + " instead of " + AgentTerminal.NOT_READY);
                                return;
                            }
                            if(!newstatus.equals(AgentTerminal.READY)) {
                                futureTestCompletion.complete("error agentStatusChanged newstatus as " + newstatus + " instead of " + AgentTerminal.READY);
                                return;
                            }
                            testedScenario++;
                            // test unregister agent when agent still not in not ready mode
                            unregisterAgentSesssion(session, agentid);
                        }
                        else if(testedScenario == 2) {
                            if(!agentid.equals("agent1")) {
                                futureTestCompletion.complete("error agentStatusChanged agentid as " + agentid + " instead of " + "agent1");
                                return;
                            }
                            if(!oldstatus.equals(AgentTerminal.READY)) {
                                futureTestCompletion.complete("error agentStatusChanged oldstatus as " + oldstatus + " instead of " + AgentTerminal.READY);
                                return;
                            }
                            if(!newstatus.equals(AgentTerminal.NOT_READY)) {
                                futureTestCompletion.complete("error agentStatusChanged newstatus as " + newstatus + " instead of " + AgentTerminal.NOT_READY);
                                return;
                            }
                            unregisterAgentSesssion(session, agentid);
                        }
                    }
                }

                @Override
                public void afterConnectionEstablished(WebSocketSession session) {
                    // LOGGER.info("established connection - " + session);
                    
                    if(getactiveagentterminalnames().contains("agent1")) {
                        futureTestCompletion.complete("error");
                        return;
                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> jsonMap = new HashMap<String, Object>();
                    jsonMap.put("action", "register");
                    jsonMap.put("agentid", "agent1");
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
                private void unregisterAgentSesssion(WebSocketSession session, String agentid) {
                    Map<String, Object> jsonMap = new HashMap<String, Object>();
                    jsonMap.put("action", "unregister");
                    jsonMap.put("agentid", agentid);
                    sendMessage(session, jsonMap);
                }
                private void setAgentStatus(WebSocketSession session, String agentid, String status) {
                    Map<String, Object> jsonMap = new HashMap<String, Object>();
                    jsonMap.put("action", "setAgentStatus");
                    jsonMap.put("agentid", agentid);
                    jsonMap.put("status", status);
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
        // unregister agent
        unregisteragent("sjeffers");
        unregisteragent("rbarrows");
    }
    
    //@Test
    public void testagentprofileservice() throws Exception {
    	resetdata();
        // create skills
        String c = getskillcount();
        assertThat(getskillcount()).contains(getskillcount());
        createskillprofile("english");
        assertThat(getskillcount()).contains("" + (Integer.parseInt(c) + 1));
        createskillprofile("mandarin");
        assertThat(getskillcount()).contains("" + (Integer.parseInt(c) + 2));
        // create agents
        String d = getagentcount();
        assertThat(getagentcount()).contains("" + (Integer.parseInt(d) + 0));
        createagentprofile("sjeffers");
        assertThat(getagentcount()).contains("" + (Integer.parseInt(d) + 1));
        createagentprofile("rbarrows");
        assertThat(getagentcount()).contains("" + (Integer.parseInt(d) + 2));
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
        long currentActiveSize = getactiveagentterminalnames().size();
        assertThat(getactiveagentterminalnames()).hasSize((int)currentActiveSize);
        String currentTermCount = getagentterminalscount();
        assertThat(getagentterminalscount()).contains(currentTermCount);
        registeragent("sjeffers");
        assertThat(getagentterminalscount()).contains("" + (Integer.parseInt(currentTermCount) + 1));
        assertThat(getactiveagentterminalnames()).hasSize((int)currentActiveSize + 1);
        assertThat(getactiveagentterminalnames()).contains("sjeffers");
        registeragent("rbarrows");
        assertThat(getagentterminalscount()).contains("" + (Integer.parseInt(currentTermCount) + 2));
        assertThat(getactiveagentterminalnames()).contains("rbarrows");
        // setagentstatus
        assertThat(getagentstatus("sjeffers")).contains(AgentTerminal.NOT_READY);
        setagentstatus("sjeffers", AgentTerminal.READY);
        assertThat(getagentstatus("sjeffers")).contains(AgentTerminal.READY);
        // unregister agent when agent still not in not ready mode should failed
        unregisteragent("sjeffers");
        assertThat(getagentterminalscount()).contains("" + (Integer.parseInt(currentTermCount) + 2));
        setagentstatus("sjeffers", AgentTerminal.NOT_READY);
        assertThat(getagentstatus("sjeffers")).contains(AgentTerminal.READY);
        // unregister agent
        unregisteragent("sjeffers");
        assertThat(getagentterminalscount()).contains("" + (Integer.parseInt(currentTermCount) + 1));
        unregisteragent("rbarrows");
        assertThat(getagentterminalscount()).contains("" + (Integer.parseInt(currentTermCount) + 0));
        // check max concurrenttask of agent
        assertThat(getmaxconcurrenttaskofagent("sjeffers")).contains("3");
        assertThat(getmaxconcurrenttaskofagent("rbarrows")).contains("3");
        setmaxconcurrenttaskofagent("sjeffers",4);
        assertThat(getmaxconcurrenttaskofagent("sjeffers")).contains("4");
        assertThat(getmaxconcurrenttaskofagent("rbarrows")).contains("3");
    }
    //@Test
    public void testsimplifiedwebsocketclient() throws Exception {
    	resetdata();
        // create skills
        createskillprofile("hotel");
        // create queues
        createqueueprofile("hotel:5000:hotel:10:5");
        // create agents
        boolean hasError = false;
        createagentprofile("agent2");
        assignagentskillaction("agent2", "hotel", AgentSkillDto.ASSIGNED_TO_AGENT);
        CompletableFuture<String> futureTestCompletion = new CompletableFuture<>();
        MyAgentClient agent = new MyAgentClient(this, "agent2");
        CompletableFuture<WebSocketSession> agentEstablished = agent.waitNextAgentEstablishedEvent();
        agent.start();
        assertThat(agentEstablished.get(2, SECONDS)).isNotNull();
        agent.registerAgentSession();
        CompletableFuture<Map<String,Object>> incomingReceived = agent.waitNextIncomingTextMessage();
        Map<String,Object> jsonMap = incomingReceived.get(2, SECONDS);
        boolean hasChangedReady = false;
        if (jsonMap.get("action").equals("connectionready")) {
            if(getactiveagentterminalnames().contains("agent2")) {
                agent.setAgentStatus(AgentTerminal.READY);
                hasChangedReady = true;
            }
        }
        if(!hasChangedReady) {
            futureTestCompletion.complete("error");
            hasError = true;
        }
        incomingReceived = agent.waitNextIncomingTextMessage();
        jsonMap = incomingReceived.get(2, SECONDS);
        if(jsonMap.get("action").equals("agentStatusChanged")) { 
            String newstatus = (String)jsonMap.get("newstatus");
            if(!newstatus.equals(AgentTerminal.READY)) {
                futureTestCompletion.complete("error");
                hasError = true;
            }
        }
        String conversationid = createconversationwithchannel("fyhao1@gmail.com", "webchathotel");
        MyCustomerClient customer = new MyCustomerClient(this, conversationid);
        CompletableFuture<WebSocketSession> customerEstablished = customer.waitNextCustomerEstablishedEvent();
        customer.start();
        assertThat(customerEstablished.get(2, SECONDS)).isNotNull();
        customer.registerCustomerSession();
        CompletableFuture<Map<String,Object>> customerIncomingReceived = customer.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(2, SECONDS);
        if (jsonMap.get("action").equals("connectionready")) {
            customer.sendChatMessage("testing by testsimplified");
        }
        customerIncomingReceived = customer.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(2, SECONDS);
        incomingReceived = agent.waitNextIncomingTextMessage();
        if (jsonMap.get("action").equals("chatMessageReceived")) {
            String content = (String) jsonMap.get("content");
            if(!content.equals("This is abc hotel.")) {
                futureTestCompletion.complete("error");
                hasError = true;
            }
            else {
                customer.sendChatMessage("do you know about abcde?");
            }
        }
        customerIncomingReceived = customer.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(2, SECONDS);
        if (jsonMap.get("action").equals("chatMessageReceived")) {
            String content = (String) jsonMap.get("content");
            if(!content.equals("Sorry I am not understand. Will handover to agent.")) {
                futureTestCompletion.complete("error");
                hasError = true;
            }
        }
        else {
            hasError = true;
        }
        customerIncomingReceived = customer.waitNextIncomingTextMessage();
        jsonMap = incomingReceived.get(5, SECONDS);
        String agentConversationid = null;
        if(jsonMap.get("action").equals("incomingTask")) { 
            agentConversationid = (String)jsonMap.get("conversationid");
        }
        else {
            hasError = true;
        }
        jsonMap = customerIncomingReceived.get(2, SECONDS);
        if (jsonMap.get("action").equals("agentJoined")) {
            String joinedAgentid = (String)jsonMap.get("agentid");
        }
        else {
            hasError = true;
        }
        // now agent start send message to customer
        
        agent.sendChatMessage(agentConversationid, "agent sending to customer now");
        customerIncomingReceived = customer.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(2, SECONDS);
       
        if (jsonMap.get("action").equals("chatMessageReceived")) {
            String content = (String) jsonMap.get("content");
            
            if(!content.equals("agent sending to customer now")) {
                futureTestCompletion.complete("error");
                hasError = true;
            }
            
        }
        else {
            hasError = true;
        }
        // now customer start send message to agent
        customer.sendChatMessage("hi i am customer i need help");
        incomingReceived = agent.waitNextIncomingTextMessage();
        jsonMap = incomingReceived.get(2, SECONDS);
        if (jsonMap.get("action").equals("chatMessageReceived")) {
            String content = (String) jsonMap.get("content");
            
            if(!content.equals("hi i am customer i need help")) {
                futureTestCompletion.complete("error");
                hasError = true;
            }
        }
        else {
            hasError = true;
        }

        if(hasError) {
            futureTestCompletion.complete("error");
        }
        else {
            futureTestCompletion.complete("completed");
        }
        // housekeeping cleanup, need to unregister agent to avoid affect other test case
       
        agent.setAgentStatus(AgentTerminal.NOT_READY);
        agent.unregisterAgentSesssion();
        customer.unregisterAgentSesssion();
        // hold and wait
        assertThat(futureTestCompletion.get(2, SECONDS)).contains("completed");
    }
    //@Test
    public void testmaxconcurrenttask() throws Exception {
    	resetdata();
    	// create skills
        createskillprofile("hotel");
        // create queues
        createqueueprofile("hotel:5000:hotel");
        // create agents
        boolean hasError = false;
        
        // create agent
        createagentprofile("agent3");
        assignagentskillaction("agent3", "hotel", AgentSkillDto.ASSIGNED_TO_AGENT);
        CompletableFuture<String> futureTestCompletion = new CompletableFuture<>();
        MyAgentClient agent = new MyAgentClient(this, "agent3");
        CompletableFuture<WebSocketSession> agentEstablished = agent.waitNextAgentEstablishedEvent();
        agent.start();
        assertThat(agentEstablished.get(2, SECONDS)).isNotNull();
        agent.registerAgentSession();
        CompletableFuture<Map<String,Object>> incomingReceived = agent.waitNextIncomingTextMessage();
        Map<String,Object> jsonMap = incomingReceived.get(2, SECONDS);
        agent.setAgentStatus(AgentTerminal.READY);
        
        // create customer
        String conversationid = createconversationwithchannel("fyhao1@gmail.com", "webchathotel");
        MyCustomerClient customer = new MyCustomerClient(this, conversationid);
        CompletableFuture<WebSocketSession> customerEstablished = customer.waitNextCustomerEstablishedEvent();
        customer.start();
        assertThat(customerEstablished.get(2, SECONDS)).isNotNull();
        customer.registerCustomerSession();
        
        CompletableFuture<Map<String,Object>> customerIncomingReceived = customer.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(2, SECONDS);
        incomingReceived = agent.waitNextIncomingTextMessage();
        customer.sendChatMessage("do you know about abcde?");
        customer.waitNextMessage();
        customerIncomingReceived = customer.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(2, SECONDS);
        if (jsonMap.get("action").equals("chatMessageReceived")) {
            String content = (String) jsonMap.get("content");
            if(!content.equals("Sorry I am not understand. Will handover to agent.")) {
                futureTestCompletion.complete("error1 " + content);
                hasError = true;
            }
        }
        else {
        	futureTestCompletion.complete("error11 " + jsonMap.get("action"));
        }
        
        // create customer 2
        String conversationid2 = createconversationwithchannel("fyhao2@gmail.com", "webchathotel");
        MyCustomerClient customer2 = new MyCustomerClient(this, conversationid2);
        CompletableFuture<WebSocketSession> customerEstablished2 = customer2.waitNextCustomerEstablishedEvent();
        customer2.start();
        assertThat(customerEstablished2.get(2, SECONDS)).isNotNull();
        customer2.registerCustomerSession();
        // wait 1st time for connectionready
        customerIncomingReceived = customer2.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(2, SECONDS);
        // send message
        customer2.sendChatMessage("do you know about abcde?");
        customer2.waitNextMessage();
        // then wait 2nd time for chatMessageReceived
        customerIncomingReceived = customer2.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(2, SECONDS);
        if (jsonMap.get("action").equals("chatMessageReceived")) {
            String content = (String) jsonMap.get("content");
            if(!content.equals("Sorry I am not understand. Will handover to agent.")) {
                futureTestCompletion.complete("error2 " + content);
                hasError = true;
            }
        }
        else {
        	futureTestCompletion.complete("error21 " + jsonMap.get("action"));
        }
        // create customer 3
        String conversationid3 = createconversationwithchannel("fyhao3@gmail.com", "webchathotel");
        MyCustomerClient customer3 = new MyCustomerClient(this, conversationid3);
        CompletableFuture<WebSocketSession> customerEstablished3= customer3.waitNextCustomerEstablishedEvent();
        customer3.start();
        assertThat(customerEstablished3.get(2, SECONDS)).isNotNull();
        customer3.registerCustomerSession();
        // wait 1st time for connectionready
        customerIncomingReceived = customer3.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(2, SECONDS);
        // send message
        customer3.sendChatMessage("do you know about abcde?");
        customer3.waitNextMessage();
        // then wait 2nd time for chatMessageReceived
        customerIncomingReceived = customer3.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(2, SECONDS);
        if (jsonMap.get("action").equals("chatMessageReceived")) {
            String content = (String) jsonMap.get("content");
            if(!content.equals("Sorry I am not understand. Will handover to agent.")) {
                futureTestCompletion.complete("error3 " + content);
                hasError = true;
            }
        }
        else {
        	futureTestCompletion.complete("error31 " + jsonMap.get("action"));
        }
        // create customer 4 and expect max concurrent task reached
        String conversationid4 = createconversationwithchannel("fyhao4@gmail.com", "webchathotel");
        MyCustomerClient customer4 = new MyCustomerClient(this, conversationid4);
        CompletableFuture<WebSocketSession> customerEstablished4 = customer4.waitNextCustomerEstablishedEvent();
        customer4.start();
        assertThat(customerEstablished4.get(2, SECONDS)).isNotNull();
        customer4.registerCustomerSession();
        // wait 1st time for connectionready
        customerIncomingReceived = customer4.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(2, SECONDS);
        // send message
        customer4.sendChatMessage("do you know about abcde?");
        customer4.waitNextMessage();
        // then wait 2nd time for chatMessageReceived
        customerIncomingReceived = customer4.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(6, SECONDS);
        if (jsonMap.get("action").equals("chatMessageReceived")) {
            String content = (String) jsonMap.get("content");
            if(!content.equals("Sorry I am not understand. But agent not available.")) {
                futureTestCompletion.complete("error4 " + content);
                hasError = true;
            }
        }
        else {
        	futureTestCompletion.complete("error41 " + jsonMap.get("action"));
        }
        // #70 check multi tasks correct conversationid send to agent
        incomingReceived = agent.waitNextIncomingTextMessage();
        customer.sendChatMessage("customer1 send to agent");
        jsonMap = incomingReceived.get(2, SECONDS);
        // chatMessageReceived for agent
        String agentConversationid = (String) jsonMap.get("conversationid");
        //futureTestCompletion.complete("content " + agentConversationid + "::" + conversationid);
        incomingReceived = agent.waitNextIncomingTextMessage();
        customer2.sendChatMessage("customer2 send to agent");
        jsonMap = incomingReceived.get(2, SECONDS);
        // chatMessageReceived for agent
        String agentConversationid2 = (String) jsonMap.get("conversationid");
        //futureTestCompletion.complete("content " + agentConversationid2 + "::" + conversationid2);
        incomingReceived = agent.waitNextIncomingTextMessage();
        customer3.sendChatMessage("customer3 send to agent");
        jsonMap = incomingReceived.get(2, SECONDS);
        // chatMessageReceived for agent 
        String agentConversationid3 = (String) jsonMap.get("conversationid");
        //futureTestCompletion.complete("content " + agentConversationid3 + "::" + conversationid3);
        assertThat(agentConversationid).contains(conversationid);
        assertThat(agentConversationid2).contains(conversationid2);
        assertThat(agentConversationid3).contains(conversationid3);
        assertThat(agentConversationid).doesNotContain(conversationid2);
        assertThat(agentConversationid).doesNotContain(conversationid3);
        assertThat(agentConversationid2).doesNotContain(conversationid3);
        
        // Check agent tasks
        assertThat(getagentactivetaskscount("agent3")).contains("3");
        assertThat(agent.taskidList.size()).isEqualTo(3);
        
        agent.closeTask(agent.taskidList.get(0));
        incomingReceived = agent.waitNextIncomingTextMessage();
        jsonMap = incomingReceived.get(2, SECONDS);
        if(jsonMap.get("action").equals("taskClosed")) {	
        }
        assertThat(getagentactivetaskscount("agent3")).contains("2");
        
        futureTestCompletion.complete("completed");
        // housekeeping
        
        agent.setAgentStatus(AgentTerminal.NOT_READY);
        agent.unregisterAgentSesssion();
        // hold and wait
        assertThat(futureTestCompletion.get(2, SECONDS)).contains("completed");
    }
    //@Test
    public void testtransfertasktoanotheragent() throws Exception {
    	resetdata();
    	// create skills
        createskillprofile("hotel");
        // create queues
        createqueueprofile("hotel:5000:hotel");
        // create agents
        boolean hasError = false;
        
        // create agent
        createagentprofile("agent4");
        createagentprofile("agent5");
        assignagentskillaction("agent4", "hotel", AgentSkillDto.ASSIGNED_TO_AGENT);
        assignagentskillaction("agent5", "hotel", AgentSkillDto.ASSIGNED_TO_AGENT);
        CompletableFuture<String> futureTestCompletion = new CompletableFuture<>();
        MyAgentClient agent = new MyAgentClient(this, "agent4");
        MyAgentClient agent2 = new MyAgentClient(this, "agent5");
        CompletableFuture<WebSocketSession> agentEstablished = agent.waitNextAgentEstablishedEvent();
        CompletableFuture<WebSocketSession> agentEstablished2 = agent2.waitNextAgentEstablishedEvent();
        agent.start();
        agent2.start();
        assertThat(agentEstablished.get(2, SECONDS)).isNotNull();
        assertThat(agentEstablished2.get(2, SECONDS)).isNotNull();
        agent.registerAgentSession();
        agent2.registerAgentSession();
        CompletableFuture<Map<String,Object>> incomingReceived = agent.waitNextIncomingTextMessage();
        CompletableFuture<Map<String,Object>> incomingReceived2 = agent2.waitNextIncomingTextMessage();
        Map<String,Object> jsonMap = incomingReceived.get(2, SECONDS);
        jsonMap = incomingReceived2.get(2, SECONDS);
        agent2.setAgentStatus(AgentTerminal.READY);
        agent.setAgentStatus(AgentTerminal.NOT_READY);

        // create customer
        String conversationid = createconversationwithchannel("fyhao1@gmail.com", "webchathotel");
        MyCustomerClient customer = new MyCustomerClient(this, conversationid);
        CompletableFuture<WebSocketSession> customerEstablished = customer.waitNextCustomerEstablishedEvent();
        customer.start();
        assertThat(customerEstablished.get(2, SECONDS)).isNotNull();
        customer.registerCustomerSession();
        
        CompletableFuture<Map<String,Object>> customerIncomingReceived = customer.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(2, SECONDS);
        incomingReceived = agent.waitNextIncomingTextMessage();
        incomingReceived2 = agent2.waitNextIncomingTextMessage();
        customer.sendChatMessage("do you know about abcde?");
        customer.waitNextMessage();
        customerIncomingReceived = customer.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(2, SECONDS);
        if (jsonMap.get("action").equals("chatMessageReceived")) {
            String content = (String) jsonMap.get("content");
            if(!content.equals("Sorry I am not understand. Will handover to agent.")) {
                futureTestCompletion.complete("error1 " + content);
                hasError = true;
            }
        }
        else {
        	futureTestCompletion.complete("error11 " + jsonMap.get("action"));
        }
        
        // #70 check multi tasks correct conversationid send to agent
        incomingReceived = agent.waitNextIncomingTextMessage();
        incomingReceived2 = agent2.waitNextIncomingTextMessage();
        customer.sendChatMessage("customer1 send to agent5");
        jsonMap = incomingReceived2.get(2, SECONDS);
        // chatMessageReceived for agent
        String agentConversationid = (String) jsonMap.get("conversationid");
        System.out.println("agentConversationid for agent2: " + agentConversationid);
        // Check agent tasks
        assertThat(agent.taskidList.size()).isEqualTo(0);
        assertThat(agent2.taskidList.size()).isEqualTo(1);
        assertThat(getagentactivetaskscount(agent.agentid)).contains("0");
        assertThat(getagentactivetaskscount(agent2.agentid)).contains("1");
        
        // agent2 request transfer to agent
        agent.setAgentStatus(AgentTerminal.READY);
        incomingReceived = agent.waitNextIncomingTextMessage();
        jsonMap = incomingReceived.get(2, SECONDS);
        incomingReceived = agent.waitNextIncomingTextMessage();
        agent2.requestTransferToAgent(agent, agent2.taskidList.get(0));
        jsonMap = incomingReceived.get(5, SECONDS);
        assertThat((String)jsonMap.get("action")).contains("incomingTask");
        assertThat(getagentactivetaskscount(agent.agentid)).contains("1");
        assertThat(getagentactivetaskscount(agent2.agentid)).contains("0");


        // agent transfer again back to agent2, but via skill now
        agent.setAgentStatus(AgentTerminal.NOT_READY);
        agent2.setAgentStatus(AgentTerminal.READY);
        incomingReceived2 = agent2.waitNextIncomingTextMessage();
        jsonMap = incomingReceived2.get(2, SECONDS);
        agent.requestTransferToSkill("hotel", agent.taskidList.get(0));
        incomingReceived2 = agent2.waitNextIncomingTextMessage();
        jsonMap = incomingReceived2.get(2, SECONDS);
        assertThat((String)jsonMap.get("action")).contains("incomingTask");
        assertThat(getagentactivetaskscount(agent.agentid)).contains("0");
        assertThat(getagentactivetaskscount(agent2.agentid)).contains("1");

        // agent2 transfer again back to agent, via skill, and at the same time, agent2 still in ready mode
        agent.setAgentStatus(AgentTerminal.READY);
        agent2.setAgentStatus(AgentTerminal.READY);
        incomingReceived = agent.waitNextIncomingTextMessage();
        jsonMap = incomingReceived.get(2, SECONDS);
        agent2.requestTransferToSkill("hotel", agent2.taskidList.get(0));
        incomingReceived = agent.waitNextIncomingTextMessage();
        jsonMap = incomingReceived.get(2, SECONDS);
        assertThat((String)jsonMap.get("action")).contains("incomingTask");
        assertThat(getagentactivetaskscount(agent.agentid)).contains("1");
        assertThat(getagentactivetaskscount(agent2.agentid)).contains("0");

        // agent start typing to customer
        agent.startTyping(conversationid);
        customerIncomingReceived = customer.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(2, SECONDS);
        assertThat((String)jsonMap.get("action")).contains("agentStartedTyping");
        agent.stopTyping(conversationid);
        customerIncomingReceived = customer.waitNextIncomingTextMessage();
        jsonMap = customerIncomingReceived.get(2, SECONDS);
        assertThat((String)jsonMap.get("action")).contains("agentStoppedTyping");

        // customer start typing to agent
        customer.startTyping();
        incomingReceived = agent.waitNextIncomingTextMessage();
        jsonMap = incomingReceived.get(2, SECONDS);
        assertThat((String)jsonMap.get("action")).contains("customerStartedTyping");
        customer.stopTyping();
        incomingReceived = agent.waitNextIncomingTextMessage();
        jsonMap = incomingReceived.get(2, SECONDS);
        assertThat((String)jsonMap.get("action")).contains("customerStoppedTyping");

        futureTestCompletion.complete("completed");
        // housekeeping
        
        agent.setAgentStatus(AgentTerminal.NOT_READY);
        agent2.setAgentStatus(AgentTerminal.NOT_READY);
        agent.unregisterAgentSesssion();
        agent2.unregisterAgentSesssion();
        // hold and wait
        assertThat(futureTestCompletion.get(2, SECONDS)).contains("completed");
    }
     
     //@Test
     public void testqueuemaxlimitreached() throws Exception {
    	 resetdata();
     	// create skills
         createskillprofile("hotel");
         // create queues
         createqueueprofile("hotel:5000:hotel:0");
         // create agents
         boolean hasError = false;
         
         // create agent
         createagentprofile("agent6");
         assignagentskillaction("agent6", "hotel", AgentSkillDto.ASSIGNED_TO_AGENT);
         CompletableFuture<String> futureTestCompletion = new CompletableFuture<>();
         MyAgentClient agent = new MyAgentClient(this, "agent6");
         CompletableFuture<WebSocketSession> agentEstablished = agent.waitNextAgentEstablishedEvent();
         agent.start();
         assertThat(agentEstablished.get(2, SECONDS)).isNotNull();
         agent.registerAgentSession();
         CompletableFuture<Map<String,Object>> incomingReceived = agent.waitNextIncomingTextMessage();
         Map<String,Object> jsonMap = incomingReceived.get(2, SECONDS);
         agent.setAgentStatus(AgentTerminal.READY);
         
         // create customer
         String conversationid = createconversationwithchannel("fyhao1@gmail.com", "webchathotel");
         MyCustomerClient customer = new MyCustomerClient(this, conversationid);
         CompletableFuture<WebSocketSession> customerEstablished = customer.waitNextCustomerEstablishedEvent();
         customer.start();
         assertThat(customerEstablished.get(2, SECONDS)).isNotNull();
         customer.registerCustomerSession();
         
         CompletableFuture<Map<String,Object>> customerIncomingReceived = customer.waitNextIncomingTextMessage();
         jsonMap = customerIncomingReceived.get(2, SECONDS);
         incomingReceived = agent.waitNextIncomingTextMessage();
         customer.sendChatMessage("do you know about abcde?");
         customer.waitNextMessage();
         customerIncomingReceived = customer.waitNextIncomingTextMessage();
         jsonMap = customerIncomingReceived.get(2, SECONDS);
         if (jsonMap.get("action").equals("chatMessageReceived")) {
             String content = (String) jsonMap.get("content");
             if(!content.equals("Sorry I am not understand. Will handover to agent.")) {
                 futureTestCompletion.complete("error1 " + content);
                 hasError = true;
             }
         }
         else {
         	futureTestCompletion.complete("error11 " + jsonMap.get("action"));
         }
         if(!hasError) {
        	 futureTestCompletion.complete("completed"); 
         }
         // housekeeping
         
         agent.setAgentStatus(AgentTerminal.NOT_READY);
         agent.unregisterAgentSesssion();
         // hold and wait
         assertThat(futureTestCompletion.get(2, SECONDS)).contains("error1 Sorry I am not understand. But agent not available as queue full.");
     }
     
     //@Test
     public void testqueuepriority() throws Exception {
    	 resetdata();
     	 // create skills
         createskillprofile("hotel");
         // create queues
         createqueueprofile("hotel:5000:hotel:1000:5");
         createqueueprofile("hotelpriority:5000:hotel:1000:10");
         // create agents
         boolean hasError = false;
         
         // create agent
         createagentprofile("agent7");
         setmaxconcurrenttaskofagent("agent7",1);
         assignagentskillaction("agent7", "hotel", AgentSkillDto.ASSIGNED_TO_AGENT);
         CompletableFuture<String> futureTestCompletion = new CompletableFuture<>();
         MyAgentClient agent = new MyAgentClient(this, "agent7");
         CompletableFuture<WebSocketSession> agentEstablished = agent.waitNextAgentEstablishedEvent();
         agent.start();
         assertThat(agentEstablished.get(2, SECONDS)).isNotNull();
         agent.registerAgentSession();
         CompletableFuture<Map<String,Object>> incomingReceived = agent.waitNextIncomingTextMessage();
         Map<String,Object> jsonMap = incomingReceived.get(2, SECONDS);
         //agent.setAgentStatus(AgentTerminal.READY);
         
         // create customer
         String conversationid = createconversationwithchannel("fyhao1@gmail.com", "webchathotel");
         MyCustomerClient customer = new MyCustomerClient(this, conversationid);
         CompletableFuture<WebSocketSession> customerEstablished = customer.waitNextCustomerEstablishedEvent();
         customer.start();
         assertThat(customerEstablished.get(2, SECONDS)).isNotNull();
         customer.registerCustomerSession();
         CompletableFuture<Map<String,Object>> customerIncomingReceived = customer.waitNextIncomingTextMessage();
         jsonMap = customerIncomingReceived.get(2, SECONDS);
         

         String conversationid2 = createconversationwithchannel("fyhao2@gmail.com", "webchathotel");
         MyCustomerClient customer2 = new MyCustomerClient(this, conversationid2);
         CompletableFuture<WebSocketSession> customerEstablished2 = customer2.waitNextCustomerEstablishedEvent();
         customer2.start();
         assertThat(customerEstablished2.get(2, SECONDS)).isNotNull();
         customer2.registerCustomerSession();
         CompletableFuture<Map<String,Object>> customerIncomingReceived2 = customer2.waitNextIncomingTextMessage();
         jsonMap = customerIncomingReceived2.get(2, SECONDS);
         
         
         incomingReceived = agent.waitNextIncomingTextMessage();
         System.out.println("customer1 conversation " + conversationid);
         System.out.println("customer2 conversation " + conversationid2);
         customer.sendChatMessage("do you know about abcde?");
         try {Thread.sleep(100); } catch (Exception ex) {}
         customer2.sendChatMessage("do you know about abcde? urgent");
         agent.setAgentStatus(AgentTerminal.READY);
         customer2.waitNextMessage();
         customerIncomingReceived2 = customer2.waitNextIncomingTextMessage();
         jsonMap = customerIncomingReceived2.get(5, SECONDS);
         if (jsonMap.get("action").equals("chatMessageReceived")) {
             String content = (String) jsonMap.get("content");
             if(!content.equals("Sorry I am not understand. Will handover to agent.")) {
                 futureTestCompletion.complete("error1 " + content);
                 hasError = true;
             }
         }
         else {
         	futureTestCompletion.complete("error11 " + jsonMap.get("action"));
         }
         if(!hasError) {
        	 futureTestCompletion.complete("completed"); 
         }
         // housekeeping
         
         agent.setAgentStatus(AgentTerminal.NOT_READY);
         agent.unregisterAgentSesssion();
         // hold and wait
         assertThat(futureTestCompletion.get(2, SECONDS)).contains("completed");
     }
     //@Test
     public void testqueuemultiple() throws Exception {
    	 resetdata();
    	// create skills
    	 for(int i = 0; i < 10; i++) {
             createskillprofile("hotel" + i);
             // create queues
             createqueueprofile("hotel:5000:hotel" + i + ":1000:5");
    	 }
         // create agents
         boolean hasError = false;
         
         // create agent
         for(int i = 0; i < 3; i++) {
        	 createagentprofile("agent" + i);
             assignagentskillaction("agent" + i, "hotel" + i, AgentSkillDto.ASSIGNED_TO_AGENT);
             CompletableFuture<String> futureTestCompletion = new CompletableFuture<>();
             MyAgentClient agent = new MyAgentClient(this, "agent" + i);
             CompletableFuture<WebSocketSession> agentEstablished = agent.waitNextAgentEstablishedEvent();
             agent.start();
             assertThat(agentEstablished.get(2, SECONDS)).isNotNull();
             agent.registerAgentSession();
             CompletableFuture<Map<String,Object>> incomingReceived = agent.waitNextIncomingTextMessage();
             Map<String,Object> jsonMap = incomingReceived.get(2, SECONDS);
             //agent.setAgentStatus(AgentTerminal.READY);

             
             // create customer
             String conversationid = createconversationwithchannel("fyhao" + i + "@gmail.com", "webchatselfservicetransfer");
             MyCustomerClient customer = new MyCustomerClient(this, conversationid);
             CompletableFuture<WebSocketSession> customerEstablished = customer.waitNextCustomerEstablishedEvent();
             customer.start();
             assertThat(customerEstablished.get(2, SECONDS)).isNotNull();
             customer.registerCustomerSession();
             CompletableFuture<Map<String,Object>> customerIncomingReceived = customer.waitNextIncomingTextMessage();
             jsonMap = customerIncomingReceived.get(2, SECONDS);

             agent.setAgentStatus(AgentTerminal.READY);

             incomingReceived = agent.waitNextIncomingTextMessage();
             customer.sendChatMessage("queue:hotel");
             try {Thread.sleep(100); } catch (Exception ex) {}
             customer.sendChatMessage("transfer");
             customerIncomingReceived = customer.waitNextIncomingTextMessage();
             try {Thread.sleep(100); } catch (Exception ex) {}
             futureTestCompletion.complete("completed"); 
             
             // housekeeping
             
             agent.setAgentStatus(AgentTerminal.NOT_READY);
             agent.unregisterAgentSesssion();
         }
         
     }
     
     //@Test
     public void testconference() throws Exception {
    	 resetdata();
     	 // create skills
         createskillprofile("hotel");
         // create queues
         createqueueprofile("hotel:5000:hotel:1000:5");
         // create agents
         boolean hasError = false;
         
         // create agent
         createagentprofile("agent8");
         assignagentskillaction("agent8", "hotel", AgentSkillDto.ASSIGNED_TO_AGENT);
         CompletableFuture<String> futureTestCompletion = new CompletableFuture<>();
         MyAgentClient agent = new MyAgentClient(this, "agent8");
         CompletableFuture<WebSocketSession> agentEstablished = agent.waitNextAgentEstablishedEvent();
         agent.start();
         assertThat(agentEstablished.get(2, SECONDS)).isNotNull();
         agent.registerAgentSession();
         CompletableFuture<Map<String,Object>> incomingReceived = agent.waitNextIncomingTextMessage();
         Map<String,Object> jsonMap = incomingReceived.get(2, SECONDS);
         agent.setAgentStatus("READY");
         // create 2nd agent
         createagentprofile("agent9");
         assignagentskillaction("agent9", "hotel", AgentSkillDto.ASSIGNED_TO_AGENT);
         CompletableFuture<String> futureTestCompletion2 = new CompletableFuture<>();
         MyAgentClient agent2 = new MyAgentClient(this, "agent9");
         CompletableFuture<WebSocketSession> agentEstablished2 = agent2.waitNextAgentEstablishedEvent();
         agent2.start();
         assertThat(agentEstablished2.get(2, SECONDS)).isNotNull();
         agent2.registerAgentSession();
         CompletableFuture<Map<String,Object>> incomingReceived2 = agent2.waitNextIncomingTextMessage();
         Map<String,Object> jsonMap2 = incomingReceived2.get(2, SECONDS);
         //agent.setAgentStatus(AgentTerminal.READY);
         
         // create customer
         String conversationid = createconversationwithchannel("fyhao9@gmail.com", "webchatselfservicetransfer");
         MyCustomerClient customer = new MyCustomerClient(this, conversationid);
         CompletableFuture<WebSocketSession> customerEstablished = customer.waitNextCustomerEstablishedEvent();
         customer.start();
         assertThat(customerEstablished.get(2, SECONDS)).isNotNull();
         customer.registerCustomerSession();
         CompletableFuture<Map<String,Object>> customerIncomingReceived = customer.waitNextIncomingTextMessage();
         jsonMap = customerIncomingReceived.get(2, SECONDS);

         incomingReceived = agent.waitNextIncomingTextMessage();
         customer.waitNextMessage();
         assertThat(customer.sendChatMessageWait("addskill:car")).contains("skill created");
         assertThat(customer.sendChatMessageWait("addqueue:car1:5000:car:10:5")).contains("queue is created");
         assertThat(customer.sendChatMessageWait("listqueue")).contains("car1");
         assertThat(customer.sendChatMessageWait("assignskill:agent8:car")).contains("Assigned");
         assertThat(customer.sendChatMessageWait("queue:car1")).contains("set queueToGo");
         assertThat(customer.sendChatMessageWait("transfer")).contains("Will handover");
         agent.inviteConference(agent2, conversationid);
         assertThat(agent2.waitNextAction()).contains("agentInvited");
         agent2.acceptInvite(conversationid);
         assertThat(agent2.waitNextAction()).contains("incomingTask");
         customer.sendChatMessage("send from customer");
         assertThat(agent.waitNextKey("content")).contains("send from customer");
         assertThat(agent2.waitNextKey("content")).contains("send from customer");
         agent.sendChatMessage(conversationid, "send from agent 1");
         assertThat(customer.waitNextMessage()).contains("send from agent 1");
         assertThat(agent2.waitNextKey("content")).contains("send from agent 1");
         agent2.sendChatMessage(conversationid, "send from agent 2");
         assertThat(customer.waitNextMessage()).contains("send from agent 2");
         assertThat(agent.waitNextKey("content")).contains("send from agent 2");
         if(!hasError) {
        	 futureTestCompletion.complete("completed"); 
         }
         // housekeeping
         
         agent.setAgentStatus(AgentTerminal.NOT_READY);
         agent.unregisterAgentSesssion();
         agent2.setAgentStatus(AgentTerminal.NOT_READY);
         agent2.unregisterAgentSesssion();
         
         // hold and wait
         assertThat(futureTestCompletion.get(2, SECONDS)).contains("completed");
     }
     
     //@Test
     public void testbargeinConversation() throws Exception {
    	 resetdata();
     	 // create skills
         createskillprofile("hotel");
         // create queues
         createqueueprofile("hotel:5000:hotel:1000:5");
         // create agents
         boolean hasError = false;
         
         // create agent
         createagentprofile("agent10");
         assignagentskillaction("agent10", "hotel", AgentSkillDto.ASSIGNED_TO_AGENT);
         CompletableFuture<String> futureTestCompletion = new CompletableFuture<>();
         MyAgentClient agent = new MyAgentClient(this, "agent10");
         CompletableFuture<WebSocketSession> agentEstablished = agent.waitNextAgentEstablishedEvent();
         agent.start();
         assertThat(agentEstablished.get(2, SECONDS)).isNotNull();
         agent.registerAgentSession();
         CompletableFuture<Map<String,Object>> incomingReceived = agent.waitNextIncomingTextMessage();
         Map<String,Object> jsonMap = incomingReceived.get(2, SECONDS);
         agent.setAgentStatus("READY");
         // create 2nd agent
         createagentprofile("agent11");
         assignagentskillaction("agent11", "hotel", AgentSkillDto.ASSIGNED_TO_AGENT);
         CompletableFuture<String> futureTestCompletion2 = new CompletableFuture<>();
         MyAgentClient agent2 = new MyAgentClient(this, "agent11");
         CompletableFuture<WebSocketSession> agentEstablished2 = agent2.waitNextAgentEstablishedEvent();
         agent2.start();
         assertThat(agentEstablished2.get(2, SECONDS)).isNotNull();
         agent2.registerAgentSession();
         CompletableFuture<Map<String,Object>> incomingReceived2 = agent2.waitNextIncomingTextMessage();
         Map<String,Object> jsonMap2 = incomingReceived2.get(2, SECONDS);
         //agent.setAgentStatus(AgentTerminal.READY);
         
         // create customer
         String conversationid = createconversationwithchannel("fyhao111@gmail.com", "webchatselfservicetransfer");
         MyCustomerClient customer = new MyCustomerClient(this, conversationid);
         CompletableFuture<WebSocketSession> customerEstablished = customer.waitNextCustomerEstablishedEvent();
         customer.start();
         assertThat(customerEstablished.get(2, SECONDS)).isNotNull();
         customer.registerCustomerSession();
         CompletableFuture<Map<String,Object>> customerIncomingReceived = customer.waitNextIncomingTextMessage();
         jsonMap = customerIncomingReceived.get(2, SECONDS);
         incomingReceived = agent.waitNextIncomingTextMessage();
         
         customer.waitNextMessage();
         
         assertThat(customer.sendChatMessageWait("addskill:car")).contains("skill created");
         assertThat(customer.sendChatMessageWait("addqueue:car1:5000:car:10:5")).contains("queue is created");
         assertThat(customer.sendChatMessageWait("listqueue")).contains("car1");
         assertThat(customer.sendChatMessageWait("assignskill:agent10:car")).contains("Assigned");
         assertThat(customer.sendChatMessageWait("queue:car1")).contains("set queueToGo");
         assertThat(customer.sendChatMessageWait("transfer")).contains("Will handover");
         
         assertThat(agent.waitNextAction()).contains("incomingTask");
         assertThat(agent.waitNextKey("content")).contains("transfer");
         agent2.bargeinConversation(conversationid);
         assertThat(agent2.waitNextAction()).contains("incomingTask");
         customer.sendChatMessage("send from customer");
         assertThat(agent.waitNextKey("content")).contains("send from customer");
         assertThat(agent2.waitNextKey("content")).contains("send from customer");
         agent.sendChatMessage(conversationid, "send from agent 1");
         assertThat(customer.waitNextMessage()).contains("send from agent 1");
         assertThat(agent2.waitNextKey("content")).contains("send from agent 1");
         agent2.sendChatMessage(conversationid, "send from agent 2");
         
         if(!hasError) {
        	 futureTestCompletion.complete("completed"); 
         }
         // housekeeping
         
         agent.setAgentStatus(AgentTerminal.NOT_READY);
         agent.unregisterAgentSesssion();
         agent2.setAgentStatus(AgentTerminal.NOT_READY);
         agent2.unregisterAgentSesssion();
         customer.unregisterAgentSesssion();
         
         // hold and wait
         assertThat(futureTestCompletion.get(2, SECONDS)).contains("completed");
     }
     private void resetdata() {
    	 Map<String, Object> req = new HashMap<String, Object>();
         List<Map<String,Object>> reqAgents = new ArrayList<Map<String,Object>>();
         req.put("agents", reqAgents);
         List<Map<String,Object>> reqSkills = new ArrayList<Map<String,Object>>();
         req.put("skills", reqSkills);
         List<Map<String,Object>> reqAs = new ArrayList<Map<String,Object>>();
         req.put("agentSkills", reqAs);
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         ObjectMapper objectMapper = new ObjectMapper();
         String message = null;
         try {
             message = objectMapper.writeValueAsString(req);
         } catch (JsonProcessingException e) {
         }
         
         HttpEntity<String> request = new HttpEntity<String>(message, headers);
         ResponseEntity<String> resp1 = this.restTemplate.postForEntity("http://localhost:" + port + "/agentprofile/importconfig", request,
                 String.class);
     }
    private List<String> getactiveagentterminalnames() {
        String resp = this.restTemplate.getForObject("http://localhost:" + port + "/agentterminal/getactiveagentterminalnames",
                String.class);
        JsonParser springParser = JsonParserFactory.getJsonParser();
        List<Object> list = springParser.parseList(resp);
        List<String> strList = list.stream()
                           .map( Object::toString )
                           .collect( Collectors.toList() );
        return strList;
    }
    private String setagentstatus(String agentName, String status) {
        AgentProfileDto dto = new AgentProfileDto();
        dto.setName(agentName);
        dto.setStatus(status);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = this.restTemplate.postForEntity("http://localhost:" + port + "/agentterminal/setagentstatus", request,
                String.class);
        return resp.getBody();
    }
    private String getagentstatus(String agent) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/agentterminal/getagentstatus?agent=" + agent,
                String.class);
    }
    private String getagenttaskscount(String agent) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/task/getagenttaskscount?agentid=" + agent,
                String.class);
    }
    private String getagentactivetaskscount(String agent) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/task/getagentactivetaskscount?agentid=" + agent,
                String.class);
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
    private String createqueueprofile(String queueprofile) {
    	String[] arr = queueprofile.split("\\:");
    	String cqueuename = arr[0];
    	long maxwaittime = Long.parseLong(arr[1]);
    	String skilllist = arr[2];
    	CQueueDto dto = new CQueueDto();
    	dto.setName(cqueuename);
    	dto.setMaxwaittime(maxwaittime);
    	dto.setSkilllist(skilllist);
    	dto.setMaxlimit(-1);
    	if(arr.length > 3) {
    		long maxlimit = Long.parseLong(arr[3]);
    		dto.setMaxlimit(maxlimit);
    	}
    	if(arr.length > 4) {
    		long priority = Long.parseLong(arr[4]);
    		dto.setPriority(priority);
    	}
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = this.restTemplate.postForEntity("http://localhost:" + port + "/agentprofile/createcqueueprofile", request,
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
    private String getmaxconcurrenttaskofagent(String agent) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/agentprofile/getmaxconcurrenttaskofagent?agentname=" + agent,
                String.class);
    }
    private String setmaxconcurrenttaskofagent(String agent, int maxconcurrenttask) {
        return this.restTemplate.getForObject("http://localhost:" + port + "/agentprofile/setmaxconcurrenttaskofagent?agentname=" + agent + "&maxconcurrenttask=" + maxconcurrenttask,
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