# Step 08 - Agentic AI - Model Context Protocol

Building on top of the Function Calling concept of the previous step, let's explore how we can make this idea more
distributed with the [Model Context Protocol](https://docs.langchain4j.dev/tutorials/mcp) pattern.

Basically, we will allow the LLM to act as a true agent, calling a predefined set of tools using the Model Context
Protocol to further enhance its knowledge and/or functionality. 

## Model Context Protocol

The [Model Context Protocol](https://modelcontextprotocol.io/introduction) serves as an open standard, facilitating the
creation of secure, bidirectional links between data repositories and AI-driven tools. The design is uncomplicated;
developers can either make their data accessible via MCP servers or construct AI applications (MCP clients) that
interface with these servers.

![MCP](../images/mcp.png)

In this step, we are going to see how to implement both MCP servers and clients in our application. The MCP client will
be integrated with our existing code, while the MCP server will be a standalone application that the MCP client's agent
will call to retrieve additional context.

The final code for the client application, the AI service, is available in the `step-08` folder. As before, we recommend
you follow the step-by-step guide to understand how it works, and the different steps to implement this pattern.

## The MCP Weather Server

Our MCP server will provide a wrapper around [Open-Meteo](https://open-meteo.com/), a free weather forecast and
historical weather service. It will act as a REST client to the Open-Meteo REST API, exposing an MCP interface to it so
that it can be invoked by AI agents. Our AI service will use it to retrieve weather information that is relevant to a
customer's booking.

You will find the MCP server application in the `step-08-mcp-server` folder.

The `dev.langchain4j.workshop.WeatherClient` class defines a template interface that maps to the Open-Meteo weather
service REST API. The `@RegisterRestClient` annotation automatically generates a client instance based on what is
defined and annotated in the template interface. As a result, you don’t have to worry about all the boilerplate code,
such as setting up a client class, connecting to the remote server, or invoking the correct URI with the correct
parameters. You can simply inject an instance of the client bean using CDI, wherever you want to consume it.

```java hl_lines="10" title="WeatherClient.java"
--8<-- "../../section-1/step-08-mcp-server/src/main/java/dev/langchain4j/workshop/WeatherClient.java"
```

The `dev.langchain4j.workshop.WeatherTools` class provides the MCP server implementation. It defines the methods that
are annotated with `@Tool`, just like in the previous step for our local function calling. The only difference is that
in this case, the MCP Tools we define will be available over the wire using the MCP protocol and a given transport type.

```java hl_lines="21-23 35-39 41-49 56-61" title="WeatherTools.java"
--8<-- "../../section-1/step-08-mcp-server/src/main/java/dev/langchain4j/workshop/WeatherTools.java"
```

You can see that the `WeatherTools` class injects an instance of our `WeatherClient` interface. It also defines the
`getForecast` method that is annotated with the `@Tool` annotation. The arguments for the `getForecast` annotation,
`latitude` and `longitude` are annotated with the `@ToolArg` annotation. The `@Tool` and `@ToolArg` annotations are used
when generating a description of the tool for MCP clients. Finally, you can see the that `getForecast` method simply
invokes the `getForecast` method on the `WeatherClient` interface, which will invoke the Open-Meteo weather
service REST API.

The relevant dependencies deined in the `pom.xml` file are as follows:

```xml title="pom.xml"
--8<-- "../../section-1/step-08-mcp-server/pom.xml:mcp-server"
```

Easy right? With just a few lines of code, we were able to build a full-blown MCP server that would require much more
work with any other stack or language out there! LangChain4J FTW!

==Go ahead and start the server from the `step-08-mcp-server` folder in a separate terminal window/tab:==

```shell
 ./mvnw liberty:dev
```

Now, let's configure our client app to use the newly built MCP server.

## A new MCP client dependency

LangChain4j supports MCP with equally minimal work. To use it, we need to add a new MCP client dependency. ==Open the
`pom.xml` file in your **main project** (i.e., NOT the one containing the MCP Server) and add the following 
dependency:==

```xml title="pom.xml"
--8<-- "../../section-1/step-08/pom.xml:mcp"
```

The LangChain4j MCP dependency will allow us to call remote MCP servers. Remember, MCP servers can be written in Java,
like the one we created above, but in fact they can be any kind of technology that supports the MCP protocol.

## Configuring the MCP client

Now that we have the dependency, we just need to configure our AI service to call our MCP server using the http
transport-type. For simplicity, we will use LangChain4j CDI to declaratively create the MCP client. ==In the
`src/main/resources/META-INF/microprofile-config.properties` file, add the following configuration:==

```properties title="microprofile-config.properties"
--8<-- "../../section-1/step-08/src/main/resources/META-INF/microprofile-config.properties:mcp-client"
```

Let's look at the configuration:

- `mcptransport`: The `mcptransport` properties create and configure an instance of the `StreamableHttpMcpTransport`
  class. You can see that `config.url` property is pointing to the endpoint for the MCP weather server and that it is
  configured to log requests and responses.
- `mcpclient`: The `mcpclient` properties create and configure the MCP client itself, here using the `DefaultMcpClient`
  class and referring to the `mcptransport` bean defined above.
- `mcp`: The `mcp` properties create and configure an instance of the `McpToolProvider` class. This can reference a list
  MCP clients in the `config.mcpClients` property. The bean defined here will be referenced by the AI service.

??? Note "Equivalent code"
    The properties defined in the `microprofile-config.properties` file are equivalent to the following code:

    ```java
    // Create the MCP transport for the weather service
    McpTransport weatherServiceTransport = StreamableHttpMcpTransport.builder()
        .url("http://localhost:9081/mcp")
        .logRequests(true)
        .logResponses(true)
        .build();

    // Now create the McpClient for the weather service
    McpClient mcpClient = DefaultMcpClient.builder()
        .transport(weatherServiceTransport)
        .build();

    McpToolProvider toolProvider = McpToolProvider.builder()
        .mcpClients(mcpClient)
        .build();
    ```

We'll add a `toolProviderName` attribute to the  `@RegisterAIService` annotation on our AI Service to reference the
MCP tool provider configured above. We will also add some instructions to the prompt to make the model calls retrieve
the current weather for a car rental location, and provide suggestions on what special equipment the driver might need.

==Update the `CustomerSupportAgent.java` file, as follows:==

```java hl_lines="16 29-30" title="CustomerSupportAgent.java"
--8<-- "../../section-1/step-08/src/main/java/dev/langchain4j/workshop/CustomerSupportAgent.java"
```

## Testing the function calling

Let's test the function calling. ==Make sure the application is running (`./mvnw liberty:dev`).==

Open your browser and go to [http://localhost:9080](http://localhost:9080). In the chatbot, let's try the following
interactions:

```text title="You"
Hello, I would like to retrieve information about my booking.
```

**AI**: Sure, I can help with that. I'll need a bit more information to proceed: 1. Your first and last name to look up
the booking. 2. The booking ID associated with your reservation. Please provide these details so I can assist you
further.

```text title="You"
My name is Speedy McWheels and my booking id is 2.
```

**AI**: Hello Speedy, Here are the details for your booking (ID: 2): - **Pick-up Date:** June 30, 2026 - 
**Drop-off Date:** July 2, 2026 - **Location:** Sao Paulo, Brazil ==I checked the weather forecast for Sao Paulo during
your stay, and it looks like you'll experience mild temperatures ranging from around 10°C to 25°C, with some light rain
expected on July 1st. Given the possibility of rain, would you like to consider upgrading to a vehicle with better
traction or additional comfort features for a more pleasant drive? I'd be happy to help you with that!==

```text title="You"
I need to know if need to pack snow chains. Can you check the weather forecast for that trip and advise?
```

**AI**: Based on the weather forecast for your rental period in São Paulo, you definitely **won't need snow chains**.
The temperatures are expected to stay between 8°C and 25°C with **zero snowfall** recorded. You might just want to pack
a light jacket or raincoat for the cooler evenings and the chance of light rain. If you'd prefer extra comfort during
the cooler nights, I'd be happy to help you upgrade to a vehicle with heated seats or a more robust suspension. Just let
me know if you'd like to see those options!

## Conclusion

In this step, we explored how to work with MCP servers and clients within our application, enabling us to create
versatile _agents_ that can not only reason but also interact dynamically with remote systems that can provide
additional functionality and data to our application.

An MCP server in this context is very similar to the concept of local function calling we explored previously, except
it's running in a remote application. This allows us to interface with (and build) reusable components.

As you could see, the actual implementation of the MCP server is also entirely customizable.

However, introducing tools and function calling also comes with new risks, such as LLM misbehavior (e.g., calling
functions excessively or with incorrect parameters) or vulnerabilities to prompt injection. In the
[next step](./step-09.md), we’ll explore a straightforward approach to mitigate prompt injection using guardrails,
ensuring safer and more reliable interactions.
