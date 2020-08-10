package com.fyhao.springwebapps.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
@Entity
public class Message implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @Id
	@GeneratedValue
	@Column(name="id")
	UUID id;

    @Column(name="createdTime")
    Timestamp createdTime;

    @Column(name="fromparty")
    String fromparty;

    @Column(name="toparty")
    String toparty;

    @Column(name="content")
    String content;

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

	public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

	public Conversation getConversation() {
		return conversation;
	}

	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	

	public Timestamp getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Timestamp createdTime) {
		this.createdTime = createdTime;
	}

    public String getFromparty() {
        return fromparty;
    }

    public void setFromparty(String fromparty) {
        this.fromparty = fromparty;
    }

    public String getToparty() {
        return toparty;
    }

    public void setToparty(String toparty) {
        this.toparty = toparty;
    }
}