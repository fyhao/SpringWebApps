package com.fyhao.springwebapps.rest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fyhao.springwebapps.service.AgentProfileService;
import com.fyhao.springwebapps.service.MessagingService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PostEndpointTest {
    @Mock
    private MessagingService messagingService;

    @Mock
    private AgentProfileService agentProfileService;

    @Test
    void conversationAndMessageMutationsRequirePost() throws Exception {
        WebchatController controller = new WebchatController();
        controller.messagingService = messagingService;
        when(messagingService.createConversation("customer@example.com")).thenReturn("conversation1");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/webchat/createconversation").param("email", "customer@example.com"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/webchat/createconversation").param("email", "customer@example.com"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/webchat/sendmessage").param("id", "conversation1").param("input", "hello"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/webchat/sendagentmessage")
                .param("id", "conversation1").param("agentname", "agent1").param("input", "hello"))
                .andExpect(status().isOk());

        verify(messagingService).createConversation("customer@example.com");
        verify(messagingService).sendCustomerMessage("conversation1", "hello");
        verify(messagingService).sendAgentMessage("conversation1", "agent1", "hello");
    }

    @Test
    void maxConcurrentTaskMutationRequiresPost() throws Exception {
        AgentprofileController controller = new AgentprofileController();
        controller.agentProfileService = agentProfileService;
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/agentprofile/setmaxconcurrenttaskofagent")
                .param("agentname", "agent1").param("maxconcurrenttask", "3"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/agentprofile/setmaxconcurrenttaskofagent")
                .param("agentname", "agent1").param("maxconcurrenttask", "3"))
                .andExpect(status().isOk());

        verify(agentProfileService).setMaxConcurrentTaskOfAgent("agent1", 3);
    }
}
