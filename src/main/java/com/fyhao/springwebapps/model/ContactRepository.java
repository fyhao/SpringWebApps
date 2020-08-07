package com.fyhao.springwebapps.model;

import com.fyhao.springwebapps.entity.Contact;

import org.springframework.data.repository.CrudRepository;

public interface ContactRepository extends CrudRepository<Contact, Long> {
    
    public Contact findByEmail(String email);
}