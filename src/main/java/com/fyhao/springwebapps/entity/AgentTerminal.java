package com.fyhao.springwebapps.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
@Entity
public class AgentTerminal implements Serializable {
     

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    @Id
	@GeneratedValue
	@Column(name="id")
    UUID id;
    
    @OneToOne(mappedBy = "agentTerminal")
    private Agent agent;

    @Column(name="status")
    private String status;

    public final static String READY = "READY";
    public final static String NOT_READY = "NOT_READY";
    public final static String BUSY = "BUSY";
    public final static String WORK = "WORK";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}