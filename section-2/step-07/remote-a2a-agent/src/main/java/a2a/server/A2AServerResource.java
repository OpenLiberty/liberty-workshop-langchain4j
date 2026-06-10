package a2a.server;

import static io.a2a.transport.jsonrpc.context.JSONRPCContextKeys.METHOD_NAME_KEY;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import io.a2a.common.A2AHeaders;
import io.a2a.server.ExtendedAgentCard;
import io.a2a.server.ServerCallContext;
import io.a2a.server.auth.UnauthenticatedUser;
import io.a2a.server.auth.User;
import io.a2a.server.extensions.A2AExtensions;
import io.a2a.server.util.async.Internal;
import io.a2a.spec.AgentCard;
import io.a2a.spec.CancelTaskRequest;
import io.a2a.spec.DeleteTaskPushNotificationConfigParams;
import io.a2a.spec.DeleteTaskPushNotificationConfigRequest;
import io.a2a.spec.GetAuthenticatedExtendedCardRequest;
import io.a2a.spec.GetTaskPushNotificationConfigParams;
import io.a2a.spec.GetTaskPushNotificationConfigRequest;
import io.a2a.spec.GetTaskRequest;
import io.a2a.spec.InternalError;
import io.a2a.spec.InvalidParamsError;
import io.a2a.spec.InvalidRequestError;
import io.a2a.spec.JSONParseError;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.JSONRPCErrorResponse;
import io.a2a.spec.JSONRPCMessage;
import io.a2a.spec.JSONRPCRequest;
import io.a2a.spec.JSONRPCResponse;
import io.a2a.spec.ListTaskPushNotificationConfigParams;
import io.a2a.spec.ListTaskPushNotificationConfigRequest;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.MethodNotFoundError;
import io.a2a.spec.NonStreamingJSONRPCRequest;
import io.a2a.spec.SendMessageRequest;
import io.a2a.spec.SendStreamingMessageRequest;
import io.a2a.spec.SetTaskPushNotificationConfigRequest;
import io.a2a.spec.StreamingJSONRPCRequest;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskPushNotificationConfig;
import io.a2a.spec.TaskQueryParams;
import io.a2a.spec.TaskResubscriptionRequest;
import io.a2a.spec.UnsupportedOperationError;
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler;
import io.a2a.util.Utils;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.Providers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;

@Path("/")
public class A2AServerResource {
    @Inject
    private Logger logger;

    @Inject
    JSONRPCHandler jsonRpcHandler;

    @Inject
    @ExtendedAgentCard
    Instance<AgentCard> extendedAgentCard;

    // Hook so testing can wait until the async Subscription is subscribed.
    private static volatile Runnable streamingIsSubscribedRunnable;

    @Inject
    @Internal
    Executor executor;

    @Inject
    Instance<CallContextFactory> callContextFactory;

    /**
     * Handles incoming POST requests to the main A2A endpoint. Dispatches the
     * request to the appropriate JSON-RPC handler method and returns the response.
     *
     * @param request the JSON-RPC request
     * @return the JSON-RPC response which may be an error response
     * @throws JsonMappingException 
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleNonStreamingRequests(
        String body,
        @Context HttpServletRequest httpRequest,
        @Context SecurityContext securityContext
    ) {

        ServerCallContext context = createCallContext(httpRequest, securityContext);
        logger.debug("Handling non-streaming request");
        JSONRPCResponse<?> response = null;
        JSONRPCErrorResponse error = null;
        Object requestId = null;

        try {
            JsonObject node;
            try {
                node = JsonParser.parseString(body).getAsJsonObject();
            } catch (Exception e) {
                throw new JSONParseError(e.getMessage());
            }

            JsonElement idElement = node.get("id");
            if (idElement != null && !idElement.isJsonNull() && !idElement.isJsonPrimitive()) {
                throw new InvalidRequestError("Invalid JSON-RPC request: 'id' must be a string, number, or null");
            }
            if (idElement != null && !idElement.isJsonNull() && idElement.isJsonPrimitive()) {
                JsonPrimitive idPrimitive = idElement.getAsJsonPrimitive();
                requestId = idPrimitive.isNumber() ? idPrimitive.getAsLong() : idPrimitive.getAsString();
            }

            JsonElement jsonrpcElement = node.get("jsonrpc");
            if (jsonrpcElement == null || !jsonrpcElement.isJsonPrimitive()
                    || !JSONRPCMessage.JSONRPC_VERSION.equals(jsonrpcElement.getAsString())) {
                throw new InvalidRequestError("Invalid JSON-RPC request: missing or invalid 'jsonrpc' field");
            }

            JsonElement methodElement = node.get("method");
            if (methodElement == null || !methodElement.isJsonPrimitive()) {
                throw new InvalidRequestError("Invalid JSON-RPC request: missing or invalid 'method' field");
            }

            String methodName = methodElement.getAsString();
            context.getState().put(METHOD_NAME_KEY, methodName);

            NonStreamingJSONRPCRequest<?> request = deserializeNonStreamingRequest(node, requestId, methodName);
            response = processNonStreamingRequest(request, context);
        } catch (JSONRPCError e) {
            error = new JSONRPCErrorResponse(requestId, e);
        } catch (JsonSyntaxException e) {
            error = new JSONRPCErrorResponse(requestId, new JSONParseError(e.getMessage()));
        } catch (Throwable t) {
            logger.error("Unexpected error processing request: {}", t.getMessage(), t);
            error = new JSONRPCErrorResponse(requestId, new InternalError(t.getMessage()));
        }

        // String serialized = serializeResponse(response);
        String serialized;
        if (error != null) {
            serialized = Utils.toJsonString(error);
        } else {
            serialized = Utils.toJsonString(response);
        }

        // Return Response with explicit content-type header
        return Response.status(Response.Status.OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .entity(serialized)
            .build();
    }

    /**
     * Handles incoming POST requests to the main A2A endpoint that involve Server-Sent Events (SSE).
     * Uses custom SSE response handling to avoid JAX-RS SSE compatibility issues with async publishers.
     * @throws JsonMappingException 
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void handleStreamingRequests(
        String body,
        @Context HttpServletResponse httpResponse,
        @Context HttpServletRequest httpRequest,
        @Context SecurityContext securityContext,
        @Context Providers providers
    ) throws IOException {
        
        ServerCallContext context = createCallContext(httpRequest, securityContext);
        logger.debug("Handling streaming request with custom SSE response");

        Object requestId = null;
        StreamingJSONRPCRequest<?> request = null;
        try {
            JsonObject node;
            try {
                node = JsonParser.parseString(body).getAsJsonObject();
            } catch (Exception e) {
                throw new JSONParseError(e.getMessage());
            }

            JsonElement idElement = node.get("id");
            if (idElement != null && !idElement.isJsonNull() && !idElement.isJsonPrimitive()) {
                throw new InvalidRequestError("Invalid JSON-RPC request: 'id' must be a string, number, or null");
            }
            if (idElement != null && !idElement.isJsonNull() && idElement.isJsonPrimitive()) {
                JsonPrimitive idPrimitive = idElement.getAsJsonPrimitive();
                requestId = idPrimitive.isNumber() ? idPrimitive.getAsLong() : idPrimitive.getAsString();
            }

            JsonElement jsonrpcElement = node.get("jsonrpc");
            if (  jsonrpcElement == null || !jsonrpcElement.isJsonPrimitive()
               || !JSONRPCMessage.JSONRPC_VERSION.equals(jsonrpcElement.getAsString())
            ) {
                throw new InvalidRequestError("Invalid JSON-RPC request: missing or invalid 'jsonrpc' field");
            }

            JsonElement methodElement = node.get("method");
            if (methodElement == null || !methodElement.isJsonPrimitive()) {
                throw new InvalidRequestError("Invalid JSON-RPC request: missing or invalid 'method' field");
            }

            String methodName = methodElement.getAsString();
            context.getState().put(METHOD_NAME_KEY, methodName);

            request = deserializeStreamingRequest(node, requestId, methodName);
        } catch (JSONRPCError e) {
            logger.debug("Error validating streaming request: {}", e.getMessage());
            sendJsonRpcError(httpResponse, requestId, e);
            return;
        } catch (JsonSyntaxException e) {
            logger.warn("JSON syntax error in streaming request: {}", e.getMessage());
            sendJsonRpcError(httpResponse, requestId, new JSONParseError(e.getMessage()));
            return;
        } catch (Throwable t) {
            logger.error("Unexpected error processing streaming request: {}", t.getMessage(), t);
            sendJsonRpcError(httpResponse, requestId, new InternalError(t.getMessage()));
            return;
        }

        // Set SSE headers manually for proper streaming
        httpResponse.setContentType(MediaType.SERVER_SENT_EVENTS);
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

        try {
            Flow.Publisher<? extends JSONRPCResponse<?>> publisher = createStreamingPublisher(request, context);
            logger.debug("Created streaming publisher: {}", publisher);

            if (publisher != null) {
                logger.debug("Handling custom SSE response for publisher: {}", publisher);
                handleCustomSSEResponse(publisher, httpResponse, context);
            } else {
                logger.debug("Unsupported streaming request type: {}", request.getClass().getSimpleName());
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported streaming request type");
            }
        } catch (JSONRPCError e) {
            logger.debug("Error in streaming request: {}", e.getMessage());
            sendErrorSSE(httpResponse, requestId, e);
        } catch (Throwable e) {
            logger.error("Unexpected error processing streaming request: {}", e.getMessage(), e);
            sendErrorSSE(httpResponse, requestId, new InternalError(e.getMessage()));
        }

        logger.debug("Completed streaming request processing");
    }

    /**
     * Handles incoming GET requests to the agent card endpoint.
     * Returns the agent card in JSON format.
     *
     * @return the agent card
     */
    @GET
    @Path("/.well-known/agent-card.json")
    @Produces(MediaType.APPLICATION_JSON)
    public AgentCard getAgentCard() {
        return jsonRpcHandler.getAgentCard();
    }

    private NonStreamingJSONRPCRequest<?> deserializeNonStreamingRequest(
        JsonObject node,
        Object requestId,
        String methodName
    ) {
        try {
            return switch (methodName) {
                case GetTaskRequest.METHOD -> new GetTaskRequest(
                        JSONRPCMessage.JSONRPC_VERSION, requestId, methodName, deserializeParams(node, TaskQueryParams.class)
                    );
                case CancelTaskRequest.METHOD -> new CancelTaskRequest(
                        JSONRPCMessage.JSONRPC_VERSION, requestId, methodName, deserializeParams(node, TaskIdParams.class)
                    );
                case SendMessageRequest.METHOD -> new SendMessageRequest(
                        JSONRPCMessage.JSONRPC_VERSION, requestId, methodName, deserializeParams(node, MessageSendParams.class)
                    );
                case SetTaskPushNotificationConfigRequest.METHOD -> new SetTaskPushNotificationConfigRequest(
                        JSONRPCMessage.JSONRPC_VERSION, requestId, methodName, deserializeParams(node, TaskPushNotificationConfig.class)
                    );
                case GetTaskPushNotificationConfigRequest.METHOD -> new GetTaskPushNotificationConfigRequest(
                        JSONRPCMessage.JSONRPC_VERSION, requestId, methodName, deserializeParams(node, GetTaskPushNotificationConfigParams.class)
                    );
                case ListTaskPushNotificationConfigRequest.METHOD -> new ListTaskPushNotificationConfigRequest(
                        JSONRPCMessage.JSONRPC_VERSION, requestId, methodName, deserializeParams(node, ListTaskPushNotificationConfigParams.class)
                    );
                case DeleteTaskPushNotificationConfigRequest.METHOD -> new DeleteTaskPushNotificationConfigRequest(
                        JSONRPCMessage.JSONRPC_VERSION, requestId, methodName, deserializeParams(node, DeleteTaskPushNotificationConfigParams.class)
                    );
                case GetAuthenticatedExtendedCardRequest.METHOD -> new GetAuthenticatedExtendedCardRequest(
                        JSONRPCMessage.JSONRPC_VERSION, requestId, methodName, null
                    );
                default -> throw new MethodNotFoundError();
            };
        } catch (JSONRPCError e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidParamsError(e.getMessage());
        }
    }

    private StreamingJSONRPCRequest<?> deserializeStreamingRequest(
        JsonObject node,
        Object requestId,
        String methodName
    ) {
        try {
            return switch (methodName) {
                case SendStreamingMessageRequest.METHOD -> new SendStreamingMessageRequest(
                        JSONRPCMessage.JSONRPC_VERSION, requestId, methodName, deserializeParams(node, MessageSendParams.class)
                    );
                case TaskResubscriptionRequest.METHOD -> new TaskResubscriptionRequest(
                        JSONRPCMessage.JSONRPC_VERSION, requestId, methodName, deserializeParams(node, TaskIdParams.class)
                    );
                default -> throw new MethodNotFoundError();
            };
        } catch (JSONRPCError e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidParamsError(e.getMessage());
        }
    }

    private <T> T deserializeParams(JsonObject node, Class<T> paramsType) throws JsonProcessingException {
        JsonElement paramsElement = node.get("params");
        if (paramsElement == null || paramsElement.isJsonNull()) {
            return null;
        }
        return JsonUtil.fromJson(paramsElement.toString(), paramsType);
    }

    private JSONRPCResponse<?> processNonStreamingRequest(
        NonStreamingJSONRPCRequest<?> request,
        ServerCallContext context
    ) {
        if (request instanceof GetTaskRequest req) {
            return jsonRpcHandler.onGetTask(req, context);
        } else if (request instanceof CancelTaskRequest req) {
            return jsonRpcHandler.onCancelTask(req, context);
        } else if (request instanceof SetTaskPushNotificationConfigRequest req) {
            return jsonRpcHandler.setPushNotificationConfig(req, context);
        } else if (request instanceof GetTaskPushNotificationConfigRequest req) {
            return jsonRpcHandler.getPushNotificationConfig(req, context);
        } else if (request instanceof SendMessageRequest req) {
            return jsonRpcHandler.onMessageSend(req, context);
        } else if (request instanceof ListTaskPushNotificationConfigRequest req) {
            return jsonRpcHandler.listPushNotificationConfig(req, context);
        } else if (request instanceof DeleteTaskPushNotificationConfigRequest req) {
            return jsonRpcHandler.deletePushNotificationConfig(req, context);
        } else if (request instanceof GetAuthenticatedExtendedCardRequest req) {
            return jsonRpcHandler.onGetAuthenticatedExtendedCardRequest(req, context);
        } else {
            return generateErrorResponse(request, new UnsupportedOperationError());
        }
    }

    /**
     * Creates a streaming publisher for the given request.
     * This method runs synchronously to avoid connection closure issues.
     */
    private Flow.Publisher<? extends JSONRPCResponse<?>> createStreamingPublisher(
        StreamingJSONRPCRequest<?> request, 
        ServerCallContext context
    ) {
        if (request instanceof SendStreamingMessageRequest req) {
            return jsonRpcHandler.onMessageSendStream(req, context);
        } else if (request instanceof TaskResubscriptionRequest req) {
            return jsonRpcHandler.onResubscribeToTask(req, context);
        } else {
            return null; // Unsupported request type
        }
    }

    /**
     * Handles the streaming response using custom SSE formatting.
     * This approach avoids JAX-RS SSE compatibility issues with async publishers.
     */
    private void handleCustomSSEResponse(
        Flow.Publisher<? extends JSONRPCResponse<?>> publisher,
        HttpServletResponse response,
        ServerCallContext context
    ) throws IOException {

        PrintWriter writer = response.getWriter();
        AtomicLong eventId = new AtomicLong(0);
        CompletableFuture<Void> streamingComplete = new CompletableFuture<>();

        publisher.subscribe(new Flow.Subscriber<JSONRPCResponse<?>>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                logger.debug("Custom SSE subscriber onSubscribe called");
                this.subscription = subscription;
                subscription.request(1);

                Runnable runnable = streamingIsSubscribedRunnable;
                if (runnable != null) {
                    runnable.run();
                }
            }

            @Override
            public void onNext(JSONRPCResponse<?> item) {
                logger.debug("Custom SSE subscriber onNext called with item: {}", item);
                try {
                    long id = eventId.getAndIncrement();
                    String sseEvent = "data: " + Utils.toJsonString(item) + "\nid: " + id + "\n\n";

                    writer.write(sseEvent);
                    writer.flush();

                    if (writer.checkError()) {
                        logger.info("SSE write failed (likely client disconnect)");
                        handleClientDisconnect();
                        return;
                    }

                    logger.debug("Custom SSE event sent successfully with id: {}", id);
                    subscription.request(1);
                } catch (Exception e) {
                    logger.error("Error writing SSE event: {}", e.getMessage(), e);
                    onError(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                logger.debug("Custom SSE subscriber onError called: {}", throwable.getMessage(), throwable);
                handleClientDisconnect();
                streamingComplete.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                logger.debug("Custom SSE subscriber onComplete called");
                try {
                    writer.close();
                } catch (Exception e) {
                    logger.error("Error closing writer: {}", e.getMessage(), e);
                }
                streamingComplete.complete(null);
            }

            private void handleClientDisconnect() {
                logger.debug("SSE connection closed, calling EventConsumer.cancel() to stop polling loop");
                if (subscription != null) {
                    subscription.cancel();
                }
                // context.invokeEventConsumerCancelCallback();
                try {
                    writer.close();
                } catch (Exception e) {
                    logger.debug("Error closing writer during disconnect: {}", e.getMessage());
                }
            }
        });

        try {
            streamingComplete.get();
        } catch (Exception e) {
            logger.error("Error waiting for streaming completion: {}", e.getMessage(), e);
            throw new IOException("Streaming failed", e);
        }
    }

    private JSONRPCResponse<?> generateErrorResponse(JSONRPCRequest<?> request, JSONRPCError error) {
        return new JSONRPCErrorResponse(request.getId(), error);
    }

    private void sendJsonRpcError(HttpServletResponse response, Object id, JSONRPCError error) {
        try {
            JSONRPCErrorResponse errorResponse = new JSONRPCErrorResponse(id, error);
            String jsonData = Utils.toJsonString(errorResponse);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON);
            response.getWriter().write(jsonData);
            response.getWriter().flush();
        } catch (Exception e) {
            logger.error("Error sending JSON-RPC error response: {}", e.getMessage(), e);
        }
    }

    private void sendErrorSSE(HttpServletResponse response, Object id, JSONRPCError error) {
        try {
            PrintWriter writer = response.getWriter();
            JSONRPCErrorResponse errorResponse = new JSONRPCErrorResponse(id, error);
            String jsonData = Utils.toJsonString(errorResponse);
            writer.write("data: " + jsonData + "\n");
            writer.write("id: 0\n");
            writer.write("\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            logger.error("Error sending SSE error response: {}", e.getMessage(), e);
        }
    }

    public static void setStreamingIsSubscribedRunnable(Runnable streamingIsSubscribedRunnable) {
        A2AServerResource.streamingIsSubscribedRunnable = streamingIsSubscribedRunnable;
    }

    private ServerCallContext createCallContext(HttpServletRequest request, SecurityContext securityContext) {

        if (callContextFactory.isUnsatisfied()) {
            User user;

            if (securityContext.getUserPrincipal() == null) {
                user = UnauthenticatedUser.INSTANCE;
            } else {
                user = new User() {
                    @Override
                    public boolean isAuthenticated() {
                        return true;
                    }

                    @Override
                    public String getUsername() {
                        return securityContext.getUserPrincipal().getName();
                    }
                };
            }
            Map<String, Object> state = new HashMap<>();
            // TODO Python's impl has
            //    state['auth'] = request.auth
            //  in jsonrpc_app.py. Figure out what this maps to in what we have here

            Map<String, String> headers = new HashMap<>();
            for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements() ; ) {
                String name = headerNames.nextElement();
                headers.put(name, headers.get(name));
            }

            state.put("headers", headers);

            Enumeration<String> en = request.getHeaders(A2AHeaders.X_A2A_EXTENSIONS);
            List<String> extensionHeaderValues = new ArrayList<>();
            while (en.hasMoreElements()) {
                extensionHeaderValues.add(en.nextElement());
            }
            Set<String> requestedExtensions = A2AExtensions.getRequestedExtensions(extensionHeaderValues);
            return new ServerCallContext(user, state, requestedExtensions);
        } else {
            CallContextFactory builder = callContextFactory.get();
            return builder.build(request);
        }
    }

    @Provider
    public static class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

        public JsonParseExceptionMapper() {
        }

        @Override
        public Response toResponse(JsonParseException exception) {
            // parse error, not possible to determine the request id
            return Response.ok(new JSONRPCErrorResponse(new JSONParseError())).type(MediaType.APPLICATION_JSON).build();
        }

    }
}