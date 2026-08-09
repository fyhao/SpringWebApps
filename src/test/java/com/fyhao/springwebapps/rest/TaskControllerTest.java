package com.fyhao.springwebapps.rest;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fyhao.springwebapps.service.TaskService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {
    @Mock
    private TaskService taskService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TaskController(taskService)).build();
    }

    @Test
    void closeTaskUsesDedicatedDto() throws Exception {
        mockMvc.perform(post("/task/closetask")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"agent1\",\"taskid\":\"task1\"}"))
                .andExpect(status().isOk());

        verify(taskService).closeTask("agent1", "task1");
    }

    @Test
    void transferToAgentUsesDedicatedDto() throws Exception {
        mockMvc.perform(post("/task/requesttransfertoagent")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"agent1\",\"taskid\":\"task1\",\"targetagentid\":\"agent2\"}"))
                .andExpect(status().isOk());

        verify(taskService).requestTransferToAgent("agent1", "task1", "agent2");
    }

    @Test
    void transferToSkillUsesDedicatedDto() throws Exception {
        mockMvc.perform(post("/task/requesttransfertoskill")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"agent1\",\"taskid\":\"task1\",\"targetskill\":\"hotel\"}"))
                .andExpect(status().isOk());

        verify(taskService).requestTransferToSkill("agent1", "task1", "hotel");
    }
}
