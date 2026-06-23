# Step 01 - Introduction to LangChain4j and LangChain4j CDI

To get started, make sure you use the `step-01` directory.

This step is the starting point for the workshop. It's a simple application that uses
[LangChain4j](https://docs.langchain4j.dev/intro/) and [LangChain4j CDI](https://github.com/langchain4j/langchain4j-cdi)
to interact with OpenAI's gpt-4o model. It's a simple chatbot that we will extend in the subsequent steps.

## Running the application

Run the application with the following command:

```shell
./mvnw liberty:dev
```

??? note "mvnw permission issue"
    If you run into an error about the `mvnw` maven wrapper, you can give execution permission for the file by
    navigating to the project folder and executing `chmod +x mvnw`.

??? note "Could not expand value OPENAI_API_KEY"
    If you run into an error similar to one shown below, make sure you have set the environment variable
    `OPENAI_API_KEY` with your OpenAI API key.

    ```shell
    dev.langchain4j.exception.AuthenticationException: {"error":"Unauthorized"}
    ```

When you see the following, your application is runniing and ready to serve requests: 

```bash
[INFO] ************************************************************************
[INFO] *    Liberty is running in dev mode.
[INFO] *        Automatic generation of features: [ Off ]
[INFO] *        h - see the help menu for available actions, type 'h' and press Enter.
[INFO] *        q - stop the server and quit dev mode, press Ctrl-C or type 'q' and press Enter.
[INFO] *    Liberty server port information:
[INFO] *        Liberty server HTTP port: [ 9080 ]
[INFO] *        Liberty debug port: [ 7777 ]
[INFO] ************************************************************************
```

Bring up the page for application at [http://localhost:9080](http://localhost:9080){target="_blank"} and click the red
robot icon in the bottom right corner to start chatting with the chatbot.

![Miles of Smiles UI](../images/ui-no-chatbot.png)

## Chatting with the chatbot

The chatbot is calling gpt-4o (from OpenAI) via the backend. You can test it out and observe that it has memory.
Example:

```
User: My name is Clement.
AI: Hi Clement, nice to meet you.
User: What is my name?
AI: Your name is Clement.
```

![An example of discussion with the chatbot](../images/ui.png)

This is how memory is built up for LLMs.
==In the terminal, you can observe the calls that are made to OpenAI behind the scenes. Notice the roles 'user'
(`UserMessage`) and 'assistant' (`AiMessage`).==

```bash
# The request -> Sending a message to the LLM
[INFO] 10:12:55.238 [Default Executor-thread-436] INFO dev.langchain4j.http.client.log.LoggingHttpClient -- HTTP request:
[INFO] - method: POST
[INFO] - url: http://watsonx-orders-gpu-node-17.dev.fyre.ibm.com:8000/v1/chat/completions
[INFO] - headers: [Authorization: Beare...Wg], [User-Agent: langchain4j-openai], [Content-Type: application/json]
[INFO] - body: {
[INFO]   "model" : "openai/gpt-oss-120b",
[INFO]   "messages" : [ {
[INFO]     "role" : "user",
[INFO]     "content" : "My name is Clement."
[INFO]   }, {
[INFO]     "role" : "assistant",
[INFO]     "content" : "Nice to meet you, Clement! How can I help you today?"
[INFO]   }, {
[INFO]     "role" : "user",
[INFO]     "content" : "What is my name?"
[INFO]   } ],
[INFO]   "stream" : false
[INFO] }

# The response from the LLM
[INFO] 10:12:55.949 [Default Executor-thread-436] INFO dev.langchain4j.http.client.log.LoggingHttpClient -- HTTP response:
[INFO] - status code: 200
[INFO] - headers: [content-length: 944], [content-type: application/json], [date: Thu, 11 Jun 2026 09:12:54 GMT], [server: uvicorn]
[INFO] - body: {
  "id": "chatcmpl-b94b45929e97f7c5",
  "object": "chat.completion",
  "created": 1781169175,
  "model": "openai/gpt-oss-120b",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "Your name is Clement. Is there anything else you’d like to talk about?",
        "refusal": null,
        "annotations": null,
        "audio": null,
        "function_call": null,
        "tool_calls": [],
        "reasoning": "The user asks: \"What is my name?\" We have context: they introduced themselves as Clement. So answer: Clement. Also maybe ask if correct.",
        "reasoning_content": "The user asks: \"What is my name?\" We have context: they introduced themselves as Clement. So answer: Clement. Also maybe ask if correct."
      },
      "logprobs": null,
      "finish_reason": "stop",
      "stop_reason": null,
      "token_ids": null
    }
  ],
  "service_tier": null,
  "system_fingerprint": null,
  "usage": {
    "prompt_tokens": 99,
    "total_tokens": 156,
    "completion_tokens": 57,
    "prompt_tokens_details": null
  },
  "prompt_logprobs": null,
  "prompt_token_ids": null,
  "kv_transfer_params": null
}
```

A very important aspect of the interaction with LLMs is their statelessness. To build a conversation, you need to
_resend_ the full list of messages exchanged so far. That list includes both the user and the assistant messages. This
is how the memory is built up and how the LLM can provide contextually relevant responses. We will see how to manage
this in the subsequent steps.

## Anatomy of the application

Before going further, let's take a look at the code.

If you open the `pom.xml` file, you will see that the project is a LangChain4j application. It defines a number of
dependencies on both LangChain4j and LangChain4j CDI libraries.

```xml title="pom.xml"
--8<-- "../../section-1/step-01/pom.xml:langchain4j"
```

[LangChain4j OpenAI](https://docs.langchain4j.dev/integrations/image-models/dall-e){target="_blank"} is a LangChain4j
that provides a simple way to interact with language models (LLMs), like 
[gpt-4o from OpenAI](https://platform.openai.com/docs/models/gpt-4o){target="_blank"}. It can actually interact with any
model serving the OpenAI API (like [vLLM](https://docs.vllm.ai/en/latest/){target="_blank"} or
[Podman AI Lab](https://podman-desktop.io/docs/ai-lab){target="_blank"}). LangChain4j OpenAI library abstracts the
complexity of calling the model and provides a simple API to interact with it.

In our case, the application is a simple chatbot. It uses a _WebSocket_, which is why you can also see the `websocket`
dependency in the list of features configured in the `server.xml` file:

```xml title="server.xml features"
--8<-- "../../section-1/step-01/src/main/liberty/config/server.xml:features"
```

If you now open the `src/main/java/dev/langchain4j/workshop/CustomerSupportAgentWebSocket.java`  file, you can see how the web socket is implemented:

```java title="CustomerSupportAgentWebSocket.java"
--8<-- "../../section-1/step-01/src/main/java/dev/langchain4j/workshop/CustomerSupportAgentWebSocket.java"
```

Basically, it:

1. Welcomes the user when the connection is opened
2. Calls the `chat` method of the `CustomerSupportAgent` class when a message is received and sends the result back to the user (via the web socket).

Let's now look at the cornerstone of the application, the `CustomerSupportAgent` interface.

```java title="CustomerSupportAgent.java"
--8<-- "../../section-1/step-01/src/main/java/dev/langchain4j/workshop/CustomerSupportAgent.java"
```

This interface is annotated with the `@RegisterAIService` LangChain4j CDI annotation to indicate that it is an AI
service. An [_AI service_](https://docs.langchain4j.dev/tutorials/ai-services){target="_blank"} is a concept introduced
by LangChain4j that hides the complexities of interacting with LLMs and other components behind a simple API. As you can
see it's an interface, not a concrete class, so you don't need to implement anything (thanks LangChain4j!).
LangChain4j CDI will provide an implementation for you at runtime. Thus, your application only interacts with the
methods defined in the interface.

There is a single method in this interface, `chat`, but you could name the method whatever you wanted. It takes two
parameters:

- A `sessionId` that is used to retrieve the _memory_ for the chatbot session.
- A `userMessage` that is passed to the AI model.

The response from the AI model is used as the return value for the `chat` method. How this is done is abstracted away by
LangChain4j.

[//]: # (!!! note "`SessionScoped`?")
[//]: # (    Attentive readers might have noticed the `@SessionScoped` annotation. This is a)
[//]: # (    [CDI](https://jakarta.ee/specifications/cdi/) annotation which scopes the object to the session. In our case the)
[//]: # (    session is the web socket. The session starts when the user connects to the web socket and ends when the user)
[//]: # (    disconnects. This annotation indicates that the `CustomerSupportAgent` object is created when the session starts and)
[//]: # (    destroyed when the session ends. It influences the _memory_ of our chatbot, as it remembers the conversation that)
[//]: # (    happened so far in this session.)

So far, so good! Let's move on to the [next step](./step-02.md).
