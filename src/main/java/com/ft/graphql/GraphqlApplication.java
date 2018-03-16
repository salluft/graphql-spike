package com.ft.graphql;

import com.ft.graphql.resources.GraphQLResource;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.File;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

public class GraphqlApplication extends Application<Configuration> {

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets", "/__api", "index.html"));

    }

    public void run(Configuration configuration, Environment environment) throws Exception {

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse( new File(getClass().getClassLoader().
                getResource("schema.graphqls").getFile()));

        UserDataWiring userDataWiring = new UserDataWiring(new UserApiConnector(), new LicenceApiConnector());

        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type("QueryType",typeWiring -> typeWiring
                        .dataFetcher("user", userDataWiring.userDataFetcher)
                        .dataFetcher("licence",userDataWiring.licenceDataFetcher)
                ).type("Licence",typeWiring-> typeWiring.
                        dataFetcher("users",userDataWiring.licenceUserDataFetcher))
                .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        environment.jersey().register(new JsonProcessingExceptionMapper(true));
        environment.jersey().register(new GraphQLResource(graphQLSchema, userDataWiring));

    }

    public static void main(String[] args) throws Exception {
        new GraphqlApplication().run(args);

    }
}
