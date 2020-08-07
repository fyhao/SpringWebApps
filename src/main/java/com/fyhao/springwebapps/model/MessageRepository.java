package com.fyhao.springwebapps.model;

import com.fyhao.springwebapps.entity.Message;

import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<Message, Long> {
    
}