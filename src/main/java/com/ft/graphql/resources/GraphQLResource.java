package com.ft.graphql.resources;

import com.ft.graphql.UserDataWiring;
import com.ft.graphql.model.GraphQLRequest;
import com.ft.graphql.utill.JsonKit;
import com.ft.graphql.utill.QueryParameters;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.schema.GraphQLSchema;
import org.apache.http.HttpStatus;
import org.dataloader.DataLoaderRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.util.HashMap;
import java.util.Map;

import static graphql.ExecutionInput.newExecutionInput;
import static graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentationOptions.newOptions;
import static java.util.Arrays.asList;

@Path("/")
public class GraphQLResource {

    private GraphQLSchema graphQLSchema;
    private UserDataWiring userDataWiring;

    public GraphQLResource(GraphQLSchema graphQLSchema, UserDataWiring userDataWiring) {
        this.graphQLSchema = graphQLSchema;
        this.userDataWiring = userDataWiring;
    }

    @POST
    @Path("graphql")
    public Response getUser(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, GraphQLRequest graphQLRequest){
        //QueryParameters parameters = QueryParameters.from(httpServletRequest);
        if (graphQLRequest.getQuery() == null) {
            return Response.status(400).build();
        }

        ExecutionInput.Builder executionInput = newExecutionInput()
                .query(graphQLRequest.getQuery())
                .operationName(graphQLRequest.getOperationName())
                .variables(getVariables(graphQLRequest.getVariables()));

        DataLoaderRegistry dataLoaderRegistry  = userDataWiring.getDataLoaderRegistry();

        DataLoaderDispatcherInstrumentation dlInstrumentation =
                new DataLoaderDispatcherInstrumentation(dataLoaderRegistry, newOptions().includeStatistics(true));

        Instrumentation instrumentation = new ChainedInstrumentation(
                asList(new TracingInstrumentation(), dlInstrumentation)
        );

        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema)
                .instrumentation(instrumentation)
                .build();

        ExecutionResult executionResult = graphQL.execute(executionInput.build());

        return Response.status(200).entity(executionResult.toSpecification()).build();



    }

    private static Map<String, Object> getVariables(Object variables) {
        if (variables instanceof Map) {
            Map<?, ?> inputVars = (Map) variables;
            Map<String, Object> vars = new HashMap<>();
            inputVars.forEach((k, v) -> vars.put(String.valueOf(k), v));
            return vars;
        }
        return JsonKit.toMap(String.valueOf(variables));
    }

}
