package com.fyhao.springwebapps.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class UserAccount implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue
	@Column(name="id")
	UUID id;

    @Column(name="username")
    private String username;

    @Column(name="email")
    private String email;

    @Column(name="status")
    private String status;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserAccount() {
    }

    
}