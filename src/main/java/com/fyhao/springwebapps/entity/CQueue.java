package com.fyhao.springwebapps.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class CQueue implements Serializable {
     

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

    @Column(name="maxwaittime")
    private long maxwaittime;

    @Column(name="skilllist")
    private String skilllist;

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

	public long getMaxwaittime() {
		return maxwaittime;
	}

	public void setMaxwaittime(long maxwaittime) {
		this.maxwaittime = maxwaittime;
	}

	public String getSkilllist() {
		return skilllist;
	}

	public void setSkilllist(String skilllist) {
		this.skilllist = skilllist;
	}
    
}
