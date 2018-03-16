package com.ft.graphql.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphQLRequest {

    private String query;

    private String operationName;

    private Map<String,String> variables;
}
