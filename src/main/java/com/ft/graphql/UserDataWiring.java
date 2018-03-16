package com.ft.graphql;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.graphql.model.Licence;
import com.ft.graphql.model.LicenceApiModel;
import com.ft.graphql.model.User;
import com.ft.graphql.model.UserOnLicence;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.SneakyThrows;
import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class UserDataWiring {

    private UserApiConnector userApiConnector;
    private LicenceApiConnector licenceApiConnector;
    private DataLoaderRegistry dataLoaderRegistry;
    private ObjectMapper mapper = new ObjectMapper();

    public UserDataWiring(UserApiConnector userApiConnector, LicenceApiConnector licenceApiConnector) {
        this.userApiConnector = userApiConnector;
        this.licenceApiConnector = licenceApiConnector;
    }

    public DataLoaderRegistry getDataLoaderRegistry() {
        this.dataLoaderRegistry = new DataLoaderRegistry();
        dataLoaderRegistry.register("users", newUserDataLoader());
        dataLoaderRegistry.register("licences", newLicenceDataLoader());
        return dataLoaderRegistry;
    }

    public DataLoader<String, User> getUserDataLoader() {
        return dataLoaderRegistry.getDataLoader("users");
    }

    public DataLoader<String, Licence> getLicenceDataLoader() {
        return dataLoaderRegistry.getDataLoader("licences");
    }

    private DataLoader<String, User> newUserDataLoader() {
        return new DataLoader<>(userBatchLoader);
    }

    private DataLoader<String, Licence> newLicenceDataLoader() {
        return new DataLoader<>(licenceBatchLoader);
    }

    // a batch loader function that will be called with N or more keys for batch loading
    private BatchLoader<String, User> userBatchLoader = keys -> {
        return CompletableFuture.supplyAsync(() -> getUserDataViaBatchHTTPApi(keys)/*, Executors.newFixedThreadPool(10)*/);
    };

    private BatchLoader<String, Licence> licenceBatchLoader =
            keys -> CompletableFuture.supplyAsync(() -> getLicenceDataViaBatchHTTPApi(keys));

    public DataFetcher userDataFetcher = (DataFetchingEnvironment environment) -> {
        String id = environment.getArgument("id");
        return this.getUserDataLoader().load(id);
    };

    public DataFetcher licenceDataFetcher = (DataFetchingEnvironment environment) -> {
        String id = environment.getArgument("id");
        return this.getLicenceDataLoader().load(id);
    };

    public DataFetcher licenceUserDataFetcher = (DataFetchingEnvironment environment) -> {

        Licence licence = environment.getSource();
        return this.getUserDataLoader().loadMany(new ArrayList<>(licence.getUsers()));
    };

    private List<User> getUserDataViaBatchHTTPApi(List<String> userIds) {

        return userIds.stream().map(userId ->   userApiConnector.getUser(userId)).
                map(user -> convert(user, User.class)).
                collect(Collectors.toList());
    }

    private List<Licence> getLicenceDataViaBatchHTTPApi(List<String> licenceIds) {

        return licenceIds.stream().map(licenceId -> licenceApiConnector.getLicence(licenceId)).
                map(user -> convert(user, LicenceApiModel.class)).
                map(licenceApiModel -> Licence.builder().id(licenceApiModel.getId()).users(licenceApiModel.getUsers().
                        stream().
                        map(userOnLicence -> userOnLicence.getUserId()).collect(Collectors.toSet())).build()).
                collect(Collectors.toList());
    }

    @SneakyThrows
    private <T> T convert(String data, Class<T> clazz) {
        return mapper.readValue(data, clazz);

    }


}
