package com.ft.graphql;

import com.ft.networking.client.FtJerseyClientBuilder;
import com.ft.networking.configuration.HttpClientConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

public class UserApiConnector {
    private  Client client;

    public UserApiConnector() {

        Client client = FtJerseyClientBuilder.
                usingHttpClientConfiguration(
                        new HttpClientConfiguration(10000,10000,false))
                .build();
        this.client = client;
    }

    public String getUser(String userId){
        Response  res = client.target("https://lighthouse-api.in.ft.com/insights/users/"+userId)
                .request().get(Response.class);

        if(res.getStatus() == Response.Status.OK.getStatusCode()) {
            return res.readEntity(String.class);
        }
        return null;

    }
}
