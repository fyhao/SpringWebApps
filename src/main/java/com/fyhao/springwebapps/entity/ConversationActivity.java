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
public class ConversationActivity implements Serializable {

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

    @Column(name="action")
    String action;
    
    // metadata
    @Column(name="agentid")
    String agentid; // assigned agentid or transfer from agentid
    
    @Column(name="targetAgentid")
    String targetAgentid;
    
    @Column(name="targetSkill")
    String targetSkill;
    
    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

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

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getAgentid() {
		return agentid;
	}

	public void setAgentid(String agentid) {
		this.agentid = agentid;
	}

	public String getTargetAgentid() {
		return targetAgentid;
	}

	public void setTargetAgentid(String targetAgentid) {
		this.targetAgentid = targetAgentid;
	}

	public String getTargetSkill() {
		return targetSkill;
	}

	public void setTargetSkill(String targetSkill) {
		this.targetSkill = targetSkill;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Conversation getConversation() {
		return conversation;
	}

	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}
    
    
}