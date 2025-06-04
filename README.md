# AI Q&A Project - Java Backend and Angular Frontend

## 1. Project Overview

This project is a full-stack application designed for asking questions based on the content of a predefined document. It features a Java Spring Boot backend that serves a REST API and an Angular frontend that provides a user interface for interaction. The backend includes a placeholder for integrating an AI service (like Eden AI) to provide answers, but currently uses a mock implementation.

The core functionality allows a user to submit a question through the Angular frontend. The frontend sends this question to the Java backend. The backend reads a specific document (`document.txt`) and (in a complete implementation) would use an AI service to find or generate an answer based on the document's content and the user's question.

## 2. Prerequisites

To build, run, and develop this project, you will need the following software installed:

*   **Java JDK**: Version 11 or newer (the project is configured for Java 11, but later versions should work).
*   **Maven**: Apache Maven, for building the Java backend.
*   **Node.js and npm**: Node.js (which includes npm) for managing frontend dependencies and running Angular scripts.
*   **Angular CLI**: The Angular command-line interface. Install globally using `npm install -g @angular/cli`.

## 3. Backend Setup (`java-backend`)

The backend is a Spring Boot application that handles API requests and contains the core logic.

### Navigation
First, navigate to the backend directory:
```bash
cd java-backend
```

### Building the Project
Build the project using Maven. This command will compile the code, run tests, and package the application into a JAR file.
```bash
mvn clean install
```

### Running the Project
Once the build is successful, you can run the application using:
```bash
java -jar target/aiqa-0.0.1-SNAPSHOT.jar
```
(Note: The JAR filename might vary based on the version in `pom.xml`).

The backend server will start and listen on `http://localhost:8080`.

### Integrating Eden AI API (or other AI services)

The current version of the application uses a **mock AI service**. To integrate a real AI service like Eden AI, you will need to modify the backend code.

1.  **Locate the Service Class**: The relevant code is in `java-backend/src/main/java/com/example/aiqa/service/AiService.java`.
2.  **Identify the Placeholder**: Inside the `AiService` class, find the `getAnswerFromAi` method. You will see a comment:
    ```java
    // TODO: Replace this with actual Eden AI API call
    ```
3.  **Implement the API Call**:
    *   You would typically use an HTTP client library available in Java (e.g., Spring's `RestTemplate`, `WebClient`, Apache HttpClient, or OkHttp) to make a POST request to the Eden AI API endpoint.
    *   The request would include your API key in the headers and the document content and question in the request body, formatted as required by Eden AI.
    *   The response from Eden AI would then be processed and returned by this method.

4.  **API Key Management**:
    *   **Security**: It is crucial to handle your API key securely. **Do not hardcode it directly into the source code.**
    *   **Configuration**: Refer to the comments in `java-backend/src/main/resources/application.properties`:
        ```properties
        # AI_API_KEY=YOUR_EDEN_AI_API_KEY
        # Note: It's recommended to use environment variables or a secure vault for API keys in production.
        # Example: spring.ai.eden.api-key=${EDEN_AI_API_KEY_ENV_VAR}
        ```
    *   You can load the API key from an environment variable or a Spring Boot configuration property, which can then be accessed in your `AiService`.

    **Conceptual Example (using Spring's RestTemplate - this is illustrative):**
    ```java
    // In AiService.java
    // @Value("${spring.ai.eden.api-key}") // Assuming you configure this in application.properties
    // private String edenApiKey;

    // ... in getAnswerFromAi method ...
    // RestTemplate restTemplate = new RestTemplate();
    // String edenApiUrl = "https://api.edenai.run/v2/text/question_answer"; // Example Eden AI endpoint

    // HttpHeaders headers = new HttpHeaders();
    // headers.set("Authorization", "Bearer " + edenApiKey);
    // headers.setContentType(MediaType.APPLICATION_JSON);

    // Map<String, Object> requestBody = new HashMap<>();
    // requestBody.put("providers", "openai"); // Or your chosen provider
    // requestBody.put("texts", List.of(documentContent));
    // requestBody.put("question", question);
    // requestBody.put("fallback_providers", "");

    // HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    // try {
    //   ResponseEntity<String> response = restTemplate.postForEntity(edenApiUrl, entity, String.class);
    //   // TODO: Parse the response JSON to extract the answer
    //   return "Extracted answer from Eden AI: " + response.getBody();
    // } catch (HttpClientErrorException e) {
    //   // Handle API errors
    //   return "Error calling AI API: " + e.getMessage();
    // }
    ```
    This is a simplified example. You'll need to handle JSON parsing (e.g., with Jackson) and robust error management.

## 4. Frontend Setup (`angular-frontend`)

The frontend is an Angular application that provides the user interface.

### Navigation
Navigate to the frontend directory:
```bash
cd angular-frontend
```
(If you are in `java-backend`, you'd use `cd ../angular-frontend`)

### Install Dependencies
Install the necessary Node.js packages:
```bash
npm install
```

### Run the Development Server
Start the Angular development server:
```bash
ng serve
```
or
```bash
npm start
```
This will compile the Angular application and serve it. The application will typically be available at `http://localhost:4200`. The frontend is configured to send API requests to the backend at `http://localhost:8080`.

## 5. Document Location

The document that the AI will use to answer questions is located within the backend project at:
`java-backend/src/main/resources/document.txt`

You can modify the content of this file to change the knowledge base for the Q&A system. If the backend is running, you might need to restart it for changes to `document.txt` to be reliably picked up, depending on how resource caching is handled.
