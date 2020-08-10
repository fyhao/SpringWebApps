package com.fyhao.springwebapps.model;

import java.util.UUID;

import com.fyhao.springwebapps.entity.UserAccount;

import org.springframework.data.repository.CrudRepository;

public interface UserAccountRepository extends CrudRepository<UserAccount, UUID> {
    
}