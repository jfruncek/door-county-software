/*
 * com.example.newapp.entities.Item.java
 * Created on Feb 23, 2011 by jfruncek
 * Copyright 2011, TeraMedica Healthcare Technology, Inc. All Rights Reserved.
 */
package com.example.newapp.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.tapestry5.beaneditor.NonVisual;

/**
 * TODO add description for Item 
 * 
 * @author jfruncek
 */
@Entity
public class Item {

    @Id
    @NonVisual
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String title;
    private String url;
    @NonVisual
    private int votes;
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public int getVotes() {
        return votes;
    }
    public void setVotes(int votes) {
        this.votes = votes;
    }
    
}
