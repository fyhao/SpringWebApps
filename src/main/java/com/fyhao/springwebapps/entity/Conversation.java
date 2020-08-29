package com.fyhao.springwebapps.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fyhao.springwebapps.util.Util;

@Entity
public class Conversation implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @Id
	@GeneratedValue
	@Column(name="id")
	UUID id;

    @Column(name="startTime")
    Timestamp startTime;

    @Column(name="endTime")
    Timestamp endTime;

    @Column(name="channel")
    String channel;

    @ManyToOne
    @JoinColumn(name = "contact_id")
    Contact contact;

    @OneToMany(cascade = CascadeType.ALL,mappedBy = "conversation")
    private List<Message> messages = new ArrayList<Message>();
    
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "conversation")
    private List<Context> contexts = new ArrayList<Context>();

    @OneToOne(mappedBy = "conversation")
    private Task task;
    
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "conversation")
    private List<ConversationActivity> conversationActivities = new ArrayList<ConversationActivity>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }


    public List<Context> getContexts() {
        return contexts;
    }

    public void setContexts(List<Context> contexts) {
        this.contexts = contexts;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    public Context findContextObject(String key) {
        List<Context> contexts = getContexts();
        if(contexts == null || contexts.isEmpty()) return null;
        Optional<Context> o = contexts.stream().filter(context -> context.key.equals(key)).findFirst();
        if(!o.isEmpty()) {
            Context context = o.get();
            return context;
        }
        return null;
    }
    public String findContext(String key) {
        Context obj = findContextObject(key);
        return obj != null ? obj.getValue() : null;
    }
    public void saveContext(String key, String value) {
        Context context = findContextObject(key);
        if(context == null) {
            context = new Context();
            context.setKey(key);
            context.setValue(value);
            context.setConversation(this);
            getContexts().add(context);
        }
        else {
            context.setValue(value);
            context.setConversation(this);
        }
    }
    public boolean findContextBool(String key) {
        String value = findContext(key);
        if(value != null && value.equals("true")) {
            return true;
        }
        return false;
    }
    public void saveContextBool(String key, boolean value) {
        String str = value ? "true" : "false";
        saveContext(key, str);
    }

    public void addMessageWithInput(String input) {
        Message message = new Message();
        message.setContent(input);
        message.setCreatedTime(new java.sql.Timestamp(new Date().getTime()));
        message.setFromparty(this.getContact().getEmail());
        String state = findContext("state");
        if(state.equals("agent")) {
            String agentName = findContext("agentName");
            message.setToparty(agentName);
        }
        else if(state.equals("bot")) {
            message.setToparty("bot");
        }
        message.setConversation(this);
        getMessages().add(message);
    }
    public void addSystemMessageWithInput(String input) {
        Message message = new Message();
        message.setContent(input);
        message.setCreatedTime(new java.sql.Timestamp(new Date().getTime()));
        message.setFromparty("system");
        message.setToparty(this.getContact().getEmail());
        message.setConversation(this);
        getMessages().add(message);
    }
    public void addAgentMessageWithInput(String agentName, String input) {
        Message message = new Message();
        message.setContent(input);
        message.setCreatedTime(new java.sql.Timestamp(new Date().getTime()));
        message.setFromparty(agentName);
        message.setToparty(this.getContact().getEmail());
        message.setConversation(this);
        getMessages().add(message);
    }
    public void addBotMessageWithInput(String input) {
        Message message = new Message();
        message.setContent(input);
        message.setCreatedTime(new java.sql.Timestamp(new Date().getTime()));
        message.setFromparty("bot");
        message.setToparty(this.getContact().getEmail());
        message.setConversation(this);
        getMessages().add(message);
    }

    public void endConversation() {
        saveContext("state", "end");
        setEndTime(Util.getSQLTimestamp(new Date()));
    }
    
    public void addActivity(String action) {
    	addActivityFull(action, null, null, null);
    }
    public void addActivityWithAgent(String action, String agentid) {
    	addActivityFull(action, agentid, null, null);
    }
    public void addTransferToAgentActivity(String action, String agentid, String targetAgentid) {
    	addActivityFull(action, agentid, targetAgentid, null);
    }
    public void addTransferToSkillActivity(String action, String agentid, String targetSkill) {
    	addActivityFull(action, agentid, null, targetSkill);
    }
    public void addActivityFull(String action, String agentid, String targetAgentid, String targetSkill) {
    	ConversationActivity act = new ConversationActivity();
    	act.setAction(action);
    	act.setAgentid(agentid);
    	act.setTargetAgentid(targetAgentid);
    	act.setTargetSkill(targetSkill);
    	act.setStartTime(new java.sql.Timestamp(new Date().getTime()));
    	getConversationActivities().add(act);
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

	public List<ConversationActivity> getConversationActivities() {
		return conversationActivities;
	}

	public void setConversationActivities(List<ConversationActivity> conversationActivities) {
		this.conversationActivities = conversationActivities;
	}
    
    
}