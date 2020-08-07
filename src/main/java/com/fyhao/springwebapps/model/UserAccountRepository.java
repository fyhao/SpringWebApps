package com.fyhao.springwebapps.model;

import com.fyhao.springwebapps.entity.UserAccount;

import org.springframework.data.repository.CrudRepository;

public interface UserAccountRepository extends CrudRepository<UserAccount, Long> {
    
}