# Design Decisions and Class Descriptions

This document provides detailed information about the design decisions and class descriptions for the "Joke Of The Day" application.

## Design Decisions

### Layered Architecture

The application follows a layered architecture to separate concerns and improve maintainability.  The layers are:

* **Data Layer:** Handles database interactions.
* **Service Layer:** Contains the core business logic.
* **Representation Layer:** Exposes the REST API.

Each layer is described in more detail below.

#### Data Layer

* The data layer consists of a simple `Joke` JPA entity.
* The `Joke` entity is designed with the following fields:
    * `id`: A UUID identifier for the joke.
    * `date`: A `LocalDate` representing the date for the joke.
    * `joke`: The text of the joke (`String`).
    * `description`: An optional description of the joke (`String`).
* **Joke Data Constraints:**
    * The current design enforces a uniqueness constraint on the `date` field. This means that only one joke can be associated with a given year-month-day.  The rationale for this constraint is to simplify the application logic and user experience by guaranteeing a single, unambiguous joke for any given date.
    * This design requires that new jokes be added for each year.
* `Joke` entities are accessed and manipulated using the `JokeRepository`, which extends Spring Data JPA's `JpaRepository`.

#### Service Layer

* The service layer (`JokeService`) encapsulates the core business logic for managing jokes.
* It interacts with the data layer using `JokeRepository`.
* It uses Java `record` classes as Data Transfer Objects (DTOs) to ensure clear data transfer boundaries between layers.
* Conversion between `Joke` entities and DTOs is handled by Spring's `ConversionService`, using the registered `JokeToJokeRecordConverter`.

#### Representation Layer

* The representation layer (REST API) is implemented using Spring `@RestController` classes.
* Controllers use Java `record` classes as DTOs for request and response payloads, promoting immutability and data clarity.
* Controllers delegate all business logic execution to the service layer.
* The `/api/v1/jokes` API uses Spring HATEOAS `RepresentationModel` (specifically `EntityModel<JokeRecord>`) to provide hypermedia links for API discoverability.
    * The `JokeModelAssembler` transforms DTOs into `EntityModel` instances, adding relevant links.

### Error Handling

* The application uses custom, application-specific exceptions to represent error conditions, avoiding the use of `null` values for error signaling.  Using custom exceptions promotes a cleaner, more domain-driven error handling strategy.  It allows the application to define a set of exceptions that are meaningful to the "Joke of the Day" domain, rather than exposing low-level persistence or framework-specific exceptions to the higher layers.  This improves modularity by preventing the representation layer from needing to have any knowledge of the underlying data access mechanisms.  It also allows the service layer to evolve its implementation (e.g., switching databases) without affecting how errors are handled in the representation layer.
* The `JokeController` uses `@ExceptionHandler` methods to map exceptions to appropriate HTTP status codes in the response.
* Error response bodies are formatted as `ProblemDetail` instances, adhering to RFC 9457.
* The current error handling strategy wraps generic `DataIntegrityViolationException` instances (from JPA or Hibernate) in a custom `JokeDataIntegrityException`. This provides a domain-specific exception and preserves the underlying database constraint details, but does not provide any more specific details.  Similarly, generic `JpaSystemException` instances are wrapped in a `JokeDataOperationException`. This approach simplifies initial development by avoiding detailed exception handling in the service layer. Error handling is primarily focused on database-level constraints.
* Request body validation is performed using Spring's validation framework (Jakarta Validation annotations). For example, validation ensures that required fields are present and that data types are correct.

## Future Enhancements

This section outlines potential future enhancements for the application. These enhancements were considered during the exercise, but simpler implementations were chosen to showcase the potential for more sophisticated designs within the given time constraints.

### Joke Data Model Enhancements

* **Relaxing Date Constraint:** The current design enforces a single joke per year-month-day. Future enhancements could relax this constraint:
    * Allow multiple jokes per day, enabling the selection of a random joke for the "Joke of the Day."
    * Store jokes with only month-day granularity, allowing reuse across years. This could involve a "last displayed year" field or a similar mechanism to track joke rotation.
* **Joke Versioning/History:** Implement a mechanism to track the history of jokes, allowing to see how a joke has been changed over time. This could include:
    * Using optimistic locking with eTags in the REST endpoints to prevent conflicting updates.

### Enhanced Joke Searching

* **Single-Day Retrieval:** Add an endpoint to retrieve a joke by its exact date.
* **Proximity Search:** Implement a "closest date" search, allowing users to find the nearest joke to a specified date.
* **Monthly Retrieval:** Add an endpoint to retrieve all jokes for a given month, potentially useful for calendar-style UI displays.
* **Textual Search:** Add functionality to search for jokes based on text content within the `joke` or `description` fields. This could include:
    * Simple substring matching.
    * Full-text search capabilities (e.g., using a database with full-text indexing).
    * Support for searching with stemming, stop word removal, and other advanced text search features.

### Improved HATEOAS Links

* **Navigational Links:** Enhance the HATEOAS links provided with joke resources:
    * `nextDay`: Link to the next day's joke (if it exists).
    * `previousDay`: Link to the previous day's joke (if it exists).
    * `currentMonth`: Link to an endpoint that retrieves all jokes for the current month.
    * Consider other relevant links based on potential UI use cases.

### Error Handling Improvements

* **Specific Exception Mapping:** Improve error handling by mapping specific `DataIntegrityViolationException` subtypes or parsing constraint violation messages to provide more informative, user-friendly error messages. This would provide more actionable feedback to API clients. For example, a constraint violation on the `date` field indicating a duplicate entry could be translated into a "JokeAlreadyExistsException" with a message like "A joke for this date already exists.".
* **Expanded Validation:** Enhance request body validation using Spring Validation to enforce more complex business rules beyond basic data type checks. This could include cross-field validation and semantic checks, such as ensuring that a date is not in the past.

### Caching
* Implement caching mechanisms, potentially using HTTP `Cache-Control` headers, to reduce server load.
    * For example, the "joke of the day" (especially the `/today` endpoint) could be cached to reduce the number of requests to the server. This assumes that immediate propagation of updates is not a critical requirement, especially if combined with a restriction that prevents editing jokes in the past.
