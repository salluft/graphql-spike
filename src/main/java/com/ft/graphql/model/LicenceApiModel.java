package com.ft.graphql.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LicenceApiModel {

    @JsonProperty("licenceId")
    private String id;

    @JsonProperty("users")
    private List<UserOnLicence> users;
}
