# Student Management Application Documentation

This document provides a comprehensive overview and detailed explanation of the Student Management application, covering both its Kotlin/Spring Boot backend and Vue.js/TypeScript frontend. It aims to serve as a guide for developers to understand the architecture, implemented features, and how different components interact.

## Table of Contents
1.  [Project Overview](#1-project-overview)
2.  [Backend Documentation](#2-backend-documentation)
    *   [2.1 Project Structure](#21-project-structure)
    *   [2.2 Core Technologies](#22-core-technologies)
    *   [2.3 Authentication and Authorization (JWT)](#23-authentication-and-authorization-jwt)
    *   [2.4 Real-time Chat Feature (WebSockets with STOMP)](#24-real-time-chat-feature-websockets-with-stomp)
    *   [2.5 File Upload and Sharing](#25-file-upload-and-sharing)
    *   [2.6 Typing Indicator](#26-typing-indicator)
    *   [2.7 Online Presence Indicator](#27-online-presence-indicator)
    *   [2.8 Data Models](#28-data-models)
    *   [2.9 Services](#29-services)
    *   [2.10 Controllers](#210-controllers)
    *   [2.11 Repositories](#211-repositories)
    *   [2.12 Exception Handling](#212-exception-handling)
3.  [Frontend Documentation](#3-frontend-documentation)
    *   [3.1 Project Structure](#31-project-structure)
    *   [3.2 Core Technologies](#32-core-technologies)
    *   [3.3 Authentication](#33-authentication)
    *   [3.4 Real-time Chat UI](#34-real-time-chat-ui)
    *   [3.5 File Upload and Download UI](#35-file-upload-and-download-ui)
    *   [3.6 Typing Indicator UI](#36-typing-indicator-ui)
    *   [3.7 Online Presence Indicator UI](#37-online-presence-indicator-ui)
    *   [3.8 State Management (Pinia)](#38-state-management-pinia)
    *   [3.9 API Integration](#39-api-integration)
    *   [3.10 Routing (Vue Router)](#310-routing-vue-router)
4.  [Setup and Running the Application](#4-setup-and-running-the-application)
    *   [4.1 Backend Setup](#41-backend-setup)
    *   [4.2 Frontend Setup](#42-frontend-setup)
5.  [Key Concepts and Best Practices](#5-key-concepts-and-best-practices)

---

## 1. Project Overview

The Student Management application is a full-stack solution designed to manage student information and facilitate real-time communication. It consists of:

*   **Backend:** Built with Kotlin and the Spring Boot framework, providing a robust RESTful API, WebSocket endpoints for real-time features, and secure data handling.
*   **Frontend:** Developed using Vue.js and TypeScript, offering a dynamic and responsive user interface for interacting with the backend services.

The application supports user authentication via JWT, real-time private messaging, file sharing within chat, typing indicators, and online presence tracking.

---

## 2. Backend Documentation

The backend is a Kotlin-based Spring Boot application responsible for business logic, data persistence, security, and real-time communication via WebSockets.

### 2.1 Project Structure

The backend follows a standard Spring Boot project structure, organized by features and layers:

```
src/main/kotlin/com/example/student_api/
├── config/                  # Spring configurations (WebSocket, Security, Storage)
├── controller/              # REST API and WebSocket message handlers
├── dto/                     # Data Transfer Objects for API and WebSocket communication
├── exception/               # Custom exception classes
├── model/                   # JPA Entities (database models)
├── repository/              # Spring Data JPA repositories for data access
├── security/                # Spring Security configuration, JWT utilities, filters
├── service/                 # Business logic, orchestrates data access and other services
└── StudentApiApplication.kt # Main Spring Boot application entry point
```

### 2.2 Core Technologies

*   **Language:** Kotlin
*   **Framework:** Spring Boot
*   **Database:** PostgreSQL (via Spring Data JPA and Hibernate)
*   **Security:** Spring Security, JSON Web Tokens (JWT)
*   **Real-time Communication:** Spring WebSockets with STOMP
*   **File Storage:** Local file system

### 2.3 Authentication and Authorization (JWT)

The application uses JWT (JSON Web Tokens) for securing its REST APIs and WebSocket connections.

*   **`JWTUtil.kt`**: Handles the creation, validation, and extraction of information from JWTs.
*   **`JWTAuthFilter.kt`**: An `OncePerRequestFilter` that intercepts incoming HTTP requests, extracts the JWT, validates it, and sets the `Authentication` object in the Spring Security context.
*   **`SecurityConfig.kt`**: Configures Spring Security, defining URL access rules, setting up the JWT filter, and configuring the `AuthenticationManager`.
*   **`UserService.kt`**: Contains `register` and `login` methods to handle user creation and authentication, issuing JWTs upon successful login.
*   **`WebSocketAuthChannelInterceptor.kt`**: Intercepts WebSocket `CONNECT` and `SEND` commands. It validates the JWT provided in the `Authorization` header during `CONNECT` and associates the authenticated user with the WebSocket session. For `SEND` commands, it ensures the user is authenticated before allowing the message to proceed. This is crucial for securing WebSocket communication.

### 2.4 Real-time Chat Feature (WebSockets with STOMP)

Real-time private messaging is implemented using Spring WebSockets with the STOMP (Simple Text Oriented Messaging Protocol) sub-protocol.

*   **`WebSocketConfig.kt`**: Configures the WebSocket message broker, enabling STOMP endpoints and defining application and user-specific destinations.
*   **`PrivateChatController.kt`**:
    *   `@MessageMapping("/chat.privateMessage")`: Handles incoming private chat messages. It persists the message using `MessageService` and then uses `SimpMessagingTemplate` to send the message to both the sender's and receiver's private queues (`/user/{username}/queue/private`).
    *   `@MessageMapping("/chat.typing")`: Handles incoming typing status notifications. These messages are not persisted but are immediately forwarded to the recipient's private queue to update their typing indicator.
*   **`MessageService.kt`**: Manages the persistence of chat messages to the database.
*   **`Message.kt`**: JPA entity representing a chat message, now including a `fileUrl` field for shared files.
*   **`PrivateMessageDto.kt`**: DTO used for sending and receiving private messages over WebSocket, now includes `type` (CHAT, TYPING, STOP_TYPING) and `fileUrl` fields.

### 2.5 File Upload and Sharing

The application allows users to upload and share files within the chat, with security and easy download functionality.

*   **`StorageProperties.kt`**: A `@ConfigurationProperties` class to define configurable properties for file storage, such as the `location` (directory) for uploaded files.
*   **`StorageService.kt` (Interface)**: Defines the contract for file storage operations (store, load, delete, init).
*   **`FileSystemStorageService.kt`**: An implementation of `StorageService` that stores files on the local file system. It includes security checks to prevent directory traversal attacks.
*   **`StorageException.kt` / `StorageFileNotFoundException.kt`**: Custom exceptions for file storage operations. `StorageException` is marked `open` to allow inheritance.
*   **`FileController.kt`**:
    *   `POST /api/files/upload`: Endpoint for uploading files. It uses `StorageService` to save the file and returns a URL to access it.
    *   `GET /api/files/{filename}`: Endpoint for downloading files. It retrieves the file as a `Resource` from `StorageService` and sets the `Content-Disposition` header for download.
*   **`StudentApiApplication.kt`**: Configured with `@EnableConfigurationProperties(StorageProperties::class)` to enable the storage properties and includes a `CommandLineRunner` to initialize the storage directory on startup.
*   **`GlobalExceptions.kt`**: Includes an `@ExceptionHandler` for `MaxUploadSizeExceededException` to return a `413 Payload Too Large` status when a file exceeds the configured size limit (2MB in `application-dev.properties`).
*   **`Message.kt`**: The JPA entity now includes a `fileUrl: String?` field to link messages to uploaded files.
*   **`PrivateMessageDto.kt`**: The DTO now includes a `fileUrl: String?` field to carry the file URL in WebSocket messages.
*   **`application-dev.properties`**: Configures `spring.servlet.multipart.max-file-size` and `spring.servlet.multipart.max-request-size` to set the maximum allowed file size for uploads.

### 2.6 Typing Indicator

The typing indicator provides real-time feedback when another user is typing.

*   **`PrivateMessageDto.kt`**: The `MessageType` enum now includes `TYPING` and `STOP_TYPING` types.
*   **`PrivateChatController.kt`**: The `@MessageMapping("/chat.typing")` endpoint receives typing status messages and broadcasts them to the recipient without persisting them.
*   **`WebSocketAuthChannelInterceptor.kt`**: Ensures that `SEND` commands (including typing status messages) are only processed if the user is authenticated.

### 2.7 Online Presence Indicator

This feature tracks and displays whether users are currently online or offline.

*   **`UserPresenceService.kt`**: A `@Service` that maintains a `ConcurrentHashMap` of currently online usernames. It provides methods to add/remove users and check their online status.
*   **`UserPresenceDto.kt`**: A DTO used to broadcast user presence changes, containing the `username` and `PresenceStatus` (ONLINE/OFFLINE).
*   **`WebSocketEventListener.kt`**:
    *   `@EventListener` for `SessionConnectedEvent`: When a user connects via WebSocket, their username is added to `UserPresenceService`, and an `ONLINE` status is broadcast to `/topic/public.presence`.
    *   `@EventListener` for `SessionDisconnectEvent`: When a user disconnects, their username is removed from `UserPresenceService`, and an `OFFLINE` status is broadcast to `/topic/public.presence`.
*   **`UserDto.kt`**: Includes an `isOnline: Boolean` field to reflect the user's current online status.
*   **`UserService.kt`**: Injects `UserPresenceService` and uses it to populate the `isOnline` field when `UserDto` objects are created (e.g., in `getAllUsers()` and `getCurrentUser()`).

### 2.8 Data Models

JPA Entities representing the core data structures:

*   **`Users.kt`**: User entity (username, password, roles).
*   **`Message.kt`**: Chat message entity, now includes `fileUrl`.
*   **`Course.kt`**: Course entity.
*   **`Student.kt`**: Student entity.
*   **`AuditLog.kt`**: Audit log entity.

### 2.9 Services

Business logic layer:

*   **`UserService.kt`**: Handles user registration, login, and retrieval of user data, including online status.
*   **`MessageService.kt`**: Manages chat message persistence and retrieval of conversations.
*   **`StorageService.kt` / `FileSystemStorageService.kt`**: Manages file storage operations.
*   **`UserPresenceService.kt`**: Manages user online/offline status.
*   `AuditService.kt`, `StudentService.kt`, `StudentServiceImpl.kt`, `UserDetailsServiceImpl.kt`: Other application services.

### 2.10 Controllers

REST API and WebSocket message handlers:

*   **`AuthController.kt`**: Handles user authentication (login, register).
*   **`UserController.kt`**: Provides user-related REST endpoints.
*   **`ChatController.kt`**: Handles public chat messages (if implemented).
*   **`PrivateChatController.kt`**: Handles private chat messages and typing notifications via WebSockets.
*   **`FileController.kt`**: Handles file uploads and downloads.
*   `AuditController.kt`, `StudentController.kt`: Other application controllers.

### 2.11 Repositories

Spring Data JPA interfaces for database interaction:

*   `UserRepository.kt`
*   `MessageRepository.kt`
*   `CourseRepository.kt`
*   `StudentRepository.kt`
*   `AuditLogRepository.kt`
*   `StudentSpecification.kt`

### 2.12 Exception Handling

*   **`GlobalExceptions.kt`**: A `@ControllerAdvice` class that provides centralized exception handling for the REST API. It catches various exceptions (e.g., `StudentNotFoundException`, `MethodArgumentNotValidException`, `BadCredentialsException`, `DataIntegrityViolationException`, `MaxUploadSizeExceededException`) and returns appropriate HTTP status codes and error messages.

---

## 3. Frontend Documentation

The frontend is a Vue.js 3 application built with TypeScript, providing the user interface and interacting with the backend API and WebSocket services.

### 3.1 Project Structure

The frontend follows a standard Vue.js project structure:

```
student-management/
├── public/                  # Static assets
├── src/
│   ├── assets/              # CSS, images
│   ├── components/          # Reusable UI components
│   │   ├── Auth/
│   │   ├── Chat/            # Chat-specific components (MessageInput, ChatWindow, ChatList)
│   │   ├── Dashboard/
│   │   ├── Course/
│   │   └── Shared/          # Generic components (NavBar, Pagination, DownloadConfirmModal)
│   ├── composables/         # Reusable Vue composition functions
│   ├── router/              # Vue Router configuration
│   ├── services/            # API interaction logic (axios instances, specific API calls)
│   ├── stores/              # Pinia stores for state management
│   ├── types/               # TypeScript interfaces and enums for data structures
│   ├── views/               # Vue components representing different pages/views
│   ├── App.vue              # Main application component
│   ├── main.ts              # Application entry point, Pinia & Vue Router setup
│   └── shims-vue.d.ts       # TypeScript declaration file for Vue components
├── env.d.ts                 # Environment variable declarations
├── package.json             # Project dependencies and scripts
├── tsconfig.json            # TypeScript configuration
├── vite.config.ts           # Vite build tool configuration
└── ... (other config files)
```

### 3.2 Core Technologies

*   **Framework:** Vue.js 3
*   **Language:** TypeScript
*   **State Management:** Pinia
*   **Routing:** Vue Router
*   **HTTP Client:** Axios
*   **WebSockets:** `sockjs-client` and `stompjs`
*   **Build Tool:** Vite

### 3.3 Authentication

User authentication is handled via JWTs, obtained from the backend.

*   **`auth.store.ts`**: A Pinia store that manages the user's authentication state (JWT token, user object). It provides actions for `login`, `register`, and `logout`.
*   **`auth.service.ts`**: Contains functions for making API calls to the backend's authentication endpoints.
*   **`api.ts`**: Configures an Axios instance with a request interceptor to automatically attach the JWT to outgoing requests. It also includes a response interceptor to handle 401 (Unauthorized) errors by logging the user out.
*   **`AuthView.vue`**: The view containing login and registration forms.
*   **`LoginForm.vue` / `RegisterForm.vue`**: Components for user input.

### 3.4 Real-time Chat UI

The chat interface allows users to send and receive private messages in real-time.

*   **`chat.store.ts`**: A Pinia store that manages:
    *   `users`: List of all available chat users.
    *   `conversations`: Stores messages for each chat partner.
    *   `activeChat`: The currently selected chat partner.
    *   `unreadMessages`: Tracks unread message counts.
    *   `typingStatus`: Tracks which users are currently typing.
    *   Actions to `fetchUsers`, `fetchConversation`, `addMessage`, `setActiveChat`, `markAsRead`, `setTypingStatus`, and `setOnlineStatus`.
*   **`MessageInput.vue`**:
    *   Connects to the WebSocket endpoint (`ws`) using `sockjs-client` and `stompjs`.
    *   Subscribes to the user's private queue (`/user/queue/private`) for incoming messages and typing notifications.
    *   Sends chat messages to `/app/chat.privateMessage`.
    *   Sends typing status updates to `/app/chat.typing`.
*   **`ChatWindow.vue`**: Displays the messages for the `activeChat`.
*   **`ChatList.vue`**: Displays a list of users, allowing selection of a chat partner.

### 3.5 File Upload and Download UI

Integrated into the chat feature, allowing users to share files.

*   **`file.service.ts`**: Contains the `uploadFile` function, which makes an Axios `POST` request to `/api/files/upload` with `multipart/form-data`.
*   **`MessageInput.vue`**:
    *   Includes a file input (`<input type="file">`) and an attachment button.
    *   `handleFileUpload`: Triggers the file upload via `file.service.ts` immediately when a file is selected.
    *   **File Size Validation**: Checks if the selected file exceeds 2MB before uploading and displays an alert if it does.
    *   Stores the `fileUrl` and `fileName` in local refs (`uploadedFileUrl`, `uploadedFileName`) after a successful upload.
    *   The `sendMessage` function now includes `uploadedFileUrl` in the message payload if a file is pending.
    *   Displays a preview of the uploaded file name with a clear button before sending.
*   **`ChatWindow.vue`**:
    *   Checks the `fileUrl` of incoming messages.
    *   If the `fileUrl` points to an image (based on extension), it renders an `<img>` tag for direct preview.
    *   For all files, clicking on the image preview or file link triggers a custom `DownloadConfirmModal`.
*   **`DownloadConfirmModal.vue`**: A reusable Vue component (`src/components/Shared/`) that acts as a confirmation dialog for file downloads. It displays the file name and provides "Download" and "Cancel" buttons. Upon confirmation, it programmatically triggers the file download.

### 3.6 Typing Indicator UI

Provides visual feedback when a chat partner is typing.

*   **`types/chat.ts`**: Defines `MessageType` enum (`CHAT`, `TYPING`, `STOP_TYPING`).
*   **`MessageInput.vue`**:
    *   Uses a `watch` on the `message` input field to detect typing activity.
    *   Employs a debounce mechanism (1-second timeout) to send `TYPING` and `STOP_TYPING` messages to the backend via `/app/chat.typing`.
    *   Resets the typing status when a message is sent.
*   **`chat.store.ts`**: The `setTypingStatus` action updates the `typingStatus` reactive object, tracking which user is typing.
*   **`ChatWindow.vue`**:
    *   A computed property `isTypingInActiveChat` checks the `typingStatus` for the active chat partner.
    *   If `isTypingInActiveChat` is true, a text message "is typing" along with animated bouncing dots is displayed in the chat header.

### 3.7 Online Presence Indicator UI

Displays a visual cue (green/gray dot) indicating whether a user is online or offline.

*   **`types/chat.ts`**: Defines `PresenceStatus` enum (`ONLINE`, `OFFLINE`).
*   **`types/auth.ts`**: The `User` interface now includes an `isOnline: boolean` field.
*   **`chat.store.ts`**:
    *   The `fetchUsers` action populates the initial `isOnline` status for all users based on the backend API response.
    *   The `setOnlineStatus` action updates the `isOnline` property of a specific user in the `users` array.
*   **`MessageInput.vue`**: Subscribes to the public presence topic (`/topic/public.presence`) and dispatches incoming `UserPresenceDto` updates to `chatStore.setOnlineStatus`.
*   **`ChatWindow.vue`**:
    *   A computed property `activeChatPartnerOnlineStatus` retrieves the `isOnline` status of the currently active chat partner.
    *   A small green (`online`) or gray (`offline`) dot (`online-indicator-header`) is displayed next to the chat partner's name in the header, reflecting their online status.

### 3.8 State Management (Pinia)

Pinia is used for centralized state management in the frontend.

*   **`auth.store.ts`**: Manages authentication-related state.
*   **`chat.store.ts`**: Manages all chat-related state (users, conversations, typing status, online status).

### 3.9 API Integration

Axios is used for making HTTP requests to the backend REST API.

*   **`api.ts`**: Configures the base Axios instance, including `baseURL`, `Content-Type` header, and request/response interceptors for JWT handling.
*   **`services/`**: Contains individual service files (e.g., `auth.service.ts`, `user.service.ts`, `chat.service.ts`, `file.service.ts`) that encapsulate API calls related to specific domains.

### 3.10 Routing (Vue Router)

Vue Router manages navigation within the single-page application.

*   **`router/index.ts`**: Defines the application's routes, including protected routes that require authentication.

---

## 4. Setup and Running the Application

To get the application running, you need to set up both the backend and the frontend.

### 4.1 Backend Setup

1.  **Prerequisites:**
    *   Java Development Kit (JDK) 17 or higher
    *   Gradle (usually bundled with Spring Boot projects)
    *   PostgreSQL database server
    *   An IDE like IntelliJ IDEA (recommended for Kotlin/Spring Boot)

2.  **Database Configuration:**
    *   Create a PostgreSQL database (e.g., `student_database`).
    *   Update `src/main/resources/application-dev.properties` with your PostgreSQL credentials:
        ```properties
        spring.datasource.url=jdbc:postgresql://localhost:5432/student_database
        spring.datasource.username=your_db_username
        spring.datasource.password=your_db_password
        ```
    *   The `spring.jpa.hibernate.ddl-auto=update` property will automatically create/update tables on startup.

3.  **Run the Backend:**
    *   Open the backend project in IntelliJ IDEA.
    *   Build the project (Gradle sync).
    *   Run the `StudentApiApplication.kt` file (main method).
    *   The backend will start on `http://localhost:8080`.

### 4.2 Frontend Setup

1.  **Prerequisites:**
    *   Node.js (LTS version recommended)
    *   npm or Yarn package manager
    *   A code editor like VS Code

2.  **Install Dependencies:**
    *   Navigate to the `student-management` directory in your terminal.
    *   Run `npm install` or `yarn install`.

3.  **Run the Frontend:**
    *   In the `student-management` directory, run `npm run dev` or `yarn dev`.
    *   The frontend application will typically open in your browser at `http://localhost:5173` (or another port if 5173 is in use).

---

## 5. Key Concepts and Best Practices

*   **Separation of Concerns:** The application clearly separates backend and frontend responsibilities, and within each, layers are distinct (controllers, services, repositories; components, stores, services).
*   **RESTful API Design:** The backend exposes a RESTful API for standard CRUD operations.
*   **WebSocket for Real-time:** STOMP over WebSockets is effectively used for instant messaging, typing indicators, and presence updates, reducing latency compared to polling.
*   **JWT Security:** A stateless authentication mechanism that secures both HTTP and WebSocket communication.
*   **Pinia State Management:** Provides a centralized, reactive, and type-safe way to manage frontend application state.
*   **Modular Components:** Frontend UI is built with reusable Vue components.
*   **Error Handling:** Global exception handling on the backend and client-side error handling (e.g., file upload size validation) improve robustness.
*   **File System Storage:** Simple and effective for local file storage, though for production, cloud storage solutions (S3, Azure Blob) would be considered.
*   **TypeScript:** Used throughout the frontend for type safety, improving code quality and maintainability.
