package com.fyhao.springwebapps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.fyhao.springwebapps.entity.Agent;
import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.entity.Task;
import com.fyhao.springwebapps.model.AgentRepository;
import com.fyhao.springwebapps.model.ConversationRepository;
import com.fyhao.springwebapps.model.TaskRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceFeatureTest {
    @Mock private TaskRepository taskRepository;
    @Mock private AgentRepository agentRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private EventPublisher eventPublisher;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService();
        taskService.taskRepository = taskRepository;
        taskService.agentRepository = agentRepository;
        taskService.conversationRepository = conversationRepository;
        taskService.eventPublisher = eventPublisher;
    }

    @Test
    void conferenceInviteAndAcceptanceAddSecondActiveAgent() {
        Conversation conversation = conversationWithTask();
        conversation.saveContext("activeAgents", "agent1");
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThat(taskService.inviteConference("agent1", "agent2", conversation.getId().toString())).isZero();
        assertThat(conversation.findContext("invitedAgent")).isEqualTo("agent2");
        assertThat(taskService.acceptInvite("agent2", conversation.getId().toString())).isZero();

        assertThat(conversation.findContext("activeAgents")).isEqualTo("agent1,agent2");
        assertThat(conversation.findContext("invitedAgent")).isEmpty();
        verify(conversationRepository, org.mockito.Mockito.atLeast(2)).save(conversation);
    }

    @Test
    void bargeInAddsObserverWithoutChangingActiveAgents() {
        Conversation conversation = conversationWithTask();
        conversation.saveContext("activeAgents", "agent1");
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThat(taskService.bargeinConversation("supervisor", conversation.getId().toString())).isZero();

        assertThat(conversation.findContext("activeAgents")).isEqualTo("agent1");
        assertThat(conversation.findContext("bargeinAgents")).isEqualTo("supervisor");
        verify(conversationRepository).save(conversation);
    }

    @Test
    void closingTaskSignalsThatAgentCapacityIsAvailable() {
        Agent agent = new Agent();
        agent.setName("agent1");
        Conversation conversation = conversationWithTask();
        Task task = conversation.getTask();
        task.setAgent(agent);
        task.setStatus("Open");
        agent.getTasks().add(task);
        when(agentRepository.findByName("agent1")).thenReturn(agent);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThat(taskService.closeTask("agent1", task.getId().toString())).isZero();

        assertThat(task.getStatus()).isEqualTo("Closed");
        verify(eventPublisher).publishEvent("agentAvailable");
    }

    private Conversation conversationWithTask() {
        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID());
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setConversation(conversation);
        conversation.setTask(task);
        return conversation;
    }
}
