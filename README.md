# MiniTrello - Task Management System

## Project Overview

MiniTrello is a REST API for a task management system similar to Trello, built with Spring Boot. It allows users to organize work by creating projects, task lists within those projects, and tasks within the lists. The application features JWT-based authentication, role-based access control, and comprehensive API documentation.

## Features

- **User Management**:
    - User registration and authentication
    - JWT-based security
    - Role-based access control (User/Admin roles)
    - Profile management

- **Project Management**:
    - Create, read, update, and delete projects
    - Add/remove project members
    - View projects by different criteria (owned, member of, all accessible)

- **Task Organization**:
    - Create and manage task lists within projects
    - Create, assign, and track tasks
    - Move tasks between lists
    - Position-based ordering for both tasks and lists

- **Admin Features**:
    - User role management
    - Enable/disable user accounts
    - View detailed user information

## Technical Implementation

### Architecture

The application follows a layered architecture with:
- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic
- **Repository Layer**: Manages data access
- **Model Layer**: Defines entity relationships
- **DTO Layer**: Transfers data between layers with validation
- **Mapper Layer**: Converts between entities and DTOs
- **Exception Layer**: Provides custom exception handling
- **Security Layer**: Implements JWT authentication and access control

### Technologies Used

- **Spring Boot**: Framework for the application
- **Spring Security**: For authentication and authorization
- **Spring Data JPA/Hibernate**: ORM for database operations
- **PostgreSQL**: Relational database
- **JWT**: For stateless authentication
- **Lombok**: Reduces boilerplate code
- **Flyway**: Database migration management
- **OpenAPI/Swagger**: API documentation
- **Docker**: Containerization

### Security

The application implements JWT-based authentication:
- Token-based authentication with expiration
- Role-based access control
- Method-level security using `@PreAuthorize`
- Custom security expressions
- Password encryption

### Database Design

The core entities in the system are:
- **User**: Account information and authentication
- **Project**: Contains task lists and has owner and members
- **TaskList**: Organizes tasks within a project
- **Task**: The actual work items

Relationships:
- One-to-many: User to Projects (ownership)
- Many-to-many: User to Projects (membership)
- One-to-many: Project to TaskLists
- One-to-many: TaskList to Tasks
- Many-to-one: Task to User (assignment)

## Code Quality

- **Clean Code**: Follows best practices for maintainable code
- **Validation**: Input validation at DTO level
- **Exception Handling**: Global exception handling with appropriate error responses
- **Logging**: Comprehensive logging for monitoring and debugging
- **Documentation**: Javadoc comments and OpenAPI/Swagger for API docs
- **Testing**: Service layer unit tests

## Running the Application

### Prerequisites
- Java 17+
- Maven
- Docker (optional, for containerization)

### Setup
1. Clone the repository
2. Configure database settings in `application.properties`
3. Run using Maven: `mvn spring-boot:run`
4. Access the API at `http://localhost:8080`
5. Access Swagger UI at `http://localhost:8080/swagger-ui.html`

Alternatively, use Docker Compose:
```
docker-compose up
```

## Future Improvements

1. **Enhanced Authentication**:
    - Implement refresh tokens
    - Add OAuth2 support for social login
    - Asymmetric JWT signing (RS256 instead of HS256)

2. **Feature Enhancements**:
    - Task comments and attachments
    - User notifications
    - Task due dates and reminders
    - Task labels/tags
    - Activity logs

3. **Technical Improvements**:
    - Caching for improved performance
    - Implement event-driven architecture using Spring Events
    - Add WebSocket support for real-time updates
    - Implement full-text search
    - Pagination improvements
    - More comprehensive testing (integration and E2E tests)

4. **Deployment and DevOps**:
    - Enhance CI/CD pipeline
    - Kubernetes deployment
    - Monitoring and metrics
    - Automated performance testing

5. **Documentation**:
    - Enhanced API documentation
    - User guide
    - Contributing guide

## Areas of Focus

The implementation placed special emphasis on:

1. **Concise Spring Boot API Design**: RESTful endpoints with consistent error handling
2. **Clean JPA/Hibernate Implementation**: Efficient queries and entity relationships
3. **Robust Security**: JWT implementation with proper authorization rules
4. **Code Quality**: Readable, maintainable, and well-documented code
5. **Comprehensive Testing**: Service layer unit tests for critical functionality

## Conclusion

This MiniTrello implementation demonstrates a solid foundation for a task management system, with a focus on proper REST API design, clean architecture, and security best practices. The modular design makes it extensible for future enhancements.