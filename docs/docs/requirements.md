# Requirements

## Software Requirements

- **JDK 21.0 or later** – [Download from Adoptium](https://adoptium.net/){target="_blank"}
- **OpenAI API key** – provided by the workshop organizer
- **Podman or Docker** – see [Podman installation](https://podman.io/getting-started/installation){target="_blank"} or
  [Docker installation](https://docs.docker.com/get-docker/){target="_blank"}
    - If you use Podman, we recommend [Podman Desktop](https://podman-desktop.io/docs/installation){target="_blank"} for
      easier container management.
- **IDE with Java support** – IntelliJ, Eclipse, VSCode (with Java extension), etc.
- **Terminal** – to run commands
- _(Optional)_ **Git** – [Installation guide](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git){target="_blank"}

---

## AI Model Requirements

### Using OpenAI

All of the examples in this workshop use OpenAI by default to serve the LLM that is used to build our application. If
you want to use them "as-is", you will need an OpenAI API key to complete this workshop. 

If you do not already have one,
[create an API key](https://developers.openai.com/api/docs/quickstart#create-and-export-an-api-key){target="_blank"}.

??? info "No instructor-provided key?"
    New OpenAI developer accounts receive $5 in free trial credits.  
    If you already used your credits, you’ll need to fund your account.
    
    !!! tip
        Don’t worry — this workshop is inexpensive. The total cost should not exceed **$0.50 (~€0.43)**.  
        See the [OpenAI pricing calculator](https://openai.com/api/pricing/){target="_blank"}.

Once you have a key, set it as an environment variable:

=== "Linux / macOS"
    ```bash
    export OPENAI_API_KEY=<your-key>
    ```

=== "Windows PowerShell"
    ```powershell
    $Env:OPENAI_API_KEY = <your-key>
    ```

### Using other models

If you do not want to use OpenAI to serve the LLM, LangChain4j and LangChain4j CDI makes it straightforward to integrate
any other service providers. For instance we could serve our model on our local machine using an 
[Ollama](https://ollama.com/) server.

The applications that you will build are configured using the 
`src/main/resources/META-INF/microprofile-config.properties` file. Each one will include an example `base-url` property
that can be used to configure the agent to connect to a specific LLM:

```properties title="application.properties"
# If you want to use a different provider or run an LLM on your local machine,
# uncomment this line and update the url/port accordingly.
# dev.langchain4j.cdi.plugin.customer-support-agent.config.base-url=http://localhost:11434/v1
```

Simply uncomment this line and modify the value of the `base-url` property to point at your own LLM. You may also need
to specify an API key for your model and the model that is being served. In order to do this, modify the `api-key` and
`model-name` properties defined in the `src/main/resources/META-INF/microprofile-config.properties` file. For example:

```properties title="application.properties"
dev.langchain4j.cdi.plugin.customer-support-agent.config.api-key=${MY_API_KEY}
dev.langchain4j.cdi.plugin.customer-support-agent.config.model-name=gpt-oss:20b
```

---

## Good to Know

### Liberty Dev Mode

All of the examples in this workshop use [Open Liberty](https://openliberty.io/) to run the agent applications. You can
run the applications in [dev mode](https://openliberty.io/docs/latest/development-mode.html) from the project directory:

```bash
./mvnw liberty:dev
```

Dev mode automatically recompiles your code on every change.
Your app will be available at http://localhost:9080/.

!!! warning "Switching steps"
    Stop the running application (Ctrl+C) before starting the next step.

### Debugging

To debug an app in dev mode, put breakpoints in your code and attach your IDE debugger.
In VSCode, use the
[Liberty Tools](https://marketplace.visualstudio.com/items?itemName=Open-Liberty.liberty-dev-vscode-ext) extension.
With the application running in Open Liberty, right click on the application in the `LIBERTY DASHBOARD` view in the
explorer and select `Attach debugger`.
Other IDEs (Eclipse, IntelliJ) support similar remote debugging.

---

## Getting the Workshop Material

Either clone the repository with Git or download a ZIP archive.

### With Git

```shell
git clone https://github.com/msmiths/langchain4j-workshop.git
cd langchain4j-workshop
```

### Direct Download

```shell
curl -L -o workshop.zip https://github.com/msmiths/langchain4j-workshop/archive/refs/heads/main.zip
unzip workshop.zip
cd langchain4j-workshop-main
```

---

## Pre-Warming Caches

This workshop requires downloading Maven dependencies and Docker images.
To avoid bandwidth issues during the session, we recommend pre-downloading them.

### Warm up Maven

```shell
./mvnw verify
```

!!! tip 
    This command not only downloads dependencies but also verifies your setup before the workshop.

### Warm up Docker Images

* Podman:
    - `podman pull pgvector/pgvector:pg17`
    - `podman pull grafana/otel-lgtm`
* Docker:
    - `docker pull pgvector/pgvector:pg17`
    - `docker pull grafana/otel-lgtm`

---

## Importing the Project in Your IDE

!!! tip 
    Open the project from `section-1/step-01` in your IDE and use that directory throughout the workshop.

If you get stuck, simply switch to the `step-xx` directory of the last completed step.

---

## Next Step

Once ready, you can pick one of these entries points to start the workshop:

- If you are new to LangChain4j and LangChain4j CDI, start with [Section 1 - AI Apps](./section-1/step-01.md).
- If you want to learn more advanced AI-Infused features, such as MCP, Guardrails, Observability, and Fault Tolerance,
  start with [Section 1 - Step 08](./section-1/step-08.md).
- If you want to jump directly into agentic systems, start with [Section 2 - Agentic Workflows](./section-2/step-01.md).
