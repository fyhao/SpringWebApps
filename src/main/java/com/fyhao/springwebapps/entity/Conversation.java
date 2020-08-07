package com.fyhao.springwebapps.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Conversation implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    private Long id;

    @Column(name="startTime")
    Timestamp startTime;

    @Column(name="endTime")
    Timestamp endTime;

    @Column(name="channel")
    String channel;

    @ManyToOne
    @JoinColumn(name = "contact_id")
    Contact contact;

    @OneToMany(mappedBy = "conversation")
    private List<Message> messages = new ArrayList<Message>();
    
    @OneToMany(mappedBy = "conversation")
    private List<Context> contexts = new ArrayList<Context>();
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String findContext(String key) {
        List<Context> contexts = getContexts();
        if(contexts == null || contexts.isEmpty()) return null;
        Optional<Context> o = contexts.stream().filter(context -> context.key.equals(key)).findFirst();
        if(!o.isEmpty()) {
            Context context = o.get();
            return context.getValue();
        }
        return null;
    }
    public void addContext(String key, String value) {
        Context context = new Context();
        context.setKey(key);
        context.setValue(value);
        getContexts().add(context);
    }

    public void addMessageWithInput(String input) {
        Message message = new Message();
        message.setContent(input);
        message.setCreatedTime(new java.sql.Timestamp(new Date().getTime()));
        message.setFromparty(this.getContact().getEmail());
        String state = findContext("state");
        message.setToparty(state);
        message.setConversation(this);
        getMessages().add(message);
    }
}