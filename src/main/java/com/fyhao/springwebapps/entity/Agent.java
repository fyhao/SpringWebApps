package com.fyhao.springwebapps.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;

@Entity
public class Agent implements Serializable {
     

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    @Id
	@GeneratedValue
	@Column(name="id")
	UUID id;

    @Column(name="name")
    private String name;

    @Column(name="createdTime")
    private Timestamp createdTime;

    @ManyToMany
    @JoinTable(
    name = "agent_skill", 
    joinColumns = @JoinColumn(name = "agent_id"), 
    inverseJoinColumns = @JoinColumn(name = "skill_id"))
    Set<Skill> agentSkills;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "agentterminal_id", referencedColumnName = "id")
    AgentTerminal agentTerminal;

    @OneToMany(mappedBy = "agent")
    private List<Task> tasks = new ArrayList<Task>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public Set<Skill> getAgentSkills() {
        return agentSkills;
    }

    public void setAgentSkills(Set<Skill> agentSkills) {
        this.agentSkills = agentSkills;
    }

	public AgentTerminal getAgentTerminal() {
		return agentTerminal;
	}

	public void setAgentTerminal(AgentTerminal agentTerminal) {
		this.agentTerminal = agentTerminal;
	}

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    
}