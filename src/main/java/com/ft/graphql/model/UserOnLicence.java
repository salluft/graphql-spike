package com.ft.graphql.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserOnLicence {

    private String userId;
    private String userStartDate;
    private String userEndDate;
    @JsonProperty("isActive")
    private boolean isActive;

}
