
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
import com.fyhao.springwebapps.dto.SkillDto;
import com.fyhao.springwebapps.entity.AgentTerminal;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = SpringWebMain.class)
public class Testing2 {

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
    public void testexportimport() throws Exception {
    	String resp = this.restTemplate.getForObject("http://localhost:" + port + "/agentprofile/exportconfig",
                 String.class);
    	JsonParser springParser = JsonParserFactory.getJsonParser();
        Map<String,Object> map = springParser.parseMap(resp);
        List<AgentProfileDto> agents = (List<AgentProfileDto>)map.get("agents");
        //assertThat(agents).hasSizeGreaterThan(0);
        List<SkillDto> skills = (List<SkillDto>)map.get("skills");
        //assertThat(skills).hasSizeGreaterThan(0);
        List<AgentSkillDto> agentSkills = (List<AgentSkillDto>)map.get("agentSkills");
        //assertThat(agentSkills).hasSizeGreaterThan(0);
        // TODO add continue test for import, then export again
        Map<String, Object> req = new HashMap<String, Object>();
        List<Map<String,Object>> reqAgents = new ArrayList<Map<String,Object>>();
        Map<String,Object> agent1 = new HashMap<String,Object>();
        agent1.put("name", "agent1");
        reqAgents.add(agent1);
        req.put("agents", reqAgents);
        List<Map<String,Object>> reqSkills = new ArrayList<Map<String,Object>>();
        Map<String,Object> skill1 = new HashMap<String,Object>();
        skill1.put("name", "skill1");
        reqSkills.add(skill1);
        Map<String,Object> skill2 = new HashMap<String,Object>();
        skill2.put("name", "skill2");
        reqSkills.add(skill2);
        req.put("skills", reqSkills);
        List<Map<String,Object>> reqAs = new ArrayList<Map<String,Object>>();
        Map<String,Object> as1 = new HashMap<String,Object>();
        as1.put("agent", "agent1");
        as1.put("skill", "skill1");
        as1.put("action", "ASSIGNED_TO_AGENT");
        reqAs.add(as1);
        Map<String,Object> as2 = new HashMap<String,Object>();
        as2.put("agent", "agent1");
        as2.put("skill", "skill2");
        as2.put("action", "ASSIGNED_TO_AGENT");
        reqAs.add(as2);
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
       
        
        // verify export again
        resp = this.restTemplate.getForObject("http://localhost:" + port + "/agentprofile/exportconfig",
                String.class);
        map = springParser.parseMap(resp);
        List<Map<String,Object>> agentsMap = (List<Map<String,Object>>)map.get("agents");
        assertThat(agentsMap).hasSize(1);
        assertThat((String)agentsMap.get(0).get("name")).contains("agent1");
        List<Map<String,Object>> skillsMap = (List<Map<String,Object>>)map.get("skills");
        assertThat(skillsMap).hasSize(2);
        assertThat((String)skillsMap.get(0).get("name")).contains("skill1");
        assertThat((String)skillsMap.get(1).get("name")).contains("skill2");
        List<Map<String,Object>> agentSkillsMap = (List<Map<String,Object>>)map.get("agentSkills");
        assertThat(agentSkillsMap).hasSize(2);
        String skill_a = "";
        String skill_b = "";
        if(agentSkillsMap.get(0).get("skill").equals("skill1")) {
        	skill_a = "skill1";
        	skill_b = "skill2";
        }
        else {
        	skill_a = "skill2";
        	skill_b = "skill1";
        }
        assertThat((String)agentSkillsMap.get(0).get("agent")).contains("agent1");
        assertThat((String)agentSkillsMap.get(0).get("skill")).contains(skill_a);
        assertThat((String)agentSkillsMap.get(0).get("action")).contains("ASSIGNED_TO_AGENT");
        assertThat((String)agentSkillsMap.get(1).get("agent")).contains("agent1");
        assertThat((String)agentSkillsMap.get(1).get("skill")).contains(skill_b);
        assertThat((String)agentSkillsMap.get(1).get("action")).contains("ASSIGNED_TO_AGENT");
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