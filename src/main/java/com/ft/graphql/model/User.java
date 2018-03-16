package com.ft.graphql.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
public class User {
    @JsonProperty("userId")
    private String id;
    private boolean isMyFTUser;
    private boolean isActive;
    private float rfvScore;
}
