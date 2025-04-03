# FusionAuth - Senior Java Engineer - Take Home Exercise

## Exercise

### Summary

Build a functional REST API to CRUD on a Joke of the Day. (JOTD)

### Requirements

1. Use GitHub
2. Build the application using Java 8 or newer
3. A JOTD has required fields of “joke” and “date” and an optional field called “description”
4. API request and response need to be JSON
5. API needs to be tested
6. Must be compilable and/or runnable from the command-line.
   - For example: gradlew app:run, start.sh or something like that.
   - Maven or gradle are good options, but please use whatever you feel most comfortable with.
7. Add a README.md file with instructions for running the application.

### Review

We’ll schedule a code review roughly a week out, during which we’ll have you discuss your submission with members of our engineering team and justify any decisions you’ve made.

Your code will be evaluated based on these areas, so be sure to address them:

- Functional correctness
- Readability
- Extensibility
- Testability
- Performance
- Scalability
- Security
- Production readiness

In the interest of brevity, while you may not account for each of these items completely in your application, be prepared to explain how you would modify your code to address each of them.

## Prerequisites

* Java 21 or later
* Maven

## Building and Running the Application

1.  **Navigate to the project directory:**

    ```bash
    cd <project_directory>
    ```

2.  **Build the application:**

    ```bash
    ./mvnw clean install
    ```

3.  **Run the application:**

    ```bash
    ./mvnw spring-boot:run
    ```

The application will start on [`http://localhost:8080`](http://localhost:8080).

## Running with the Demo Profile

The application includes a "demo" profile that exposes a simple Thymeleaf templated "jotd" page and an endpoint to add multiple jokes via CSV upload. The CSV file should have the following columns: `date` (YYYY-MM-DD), `joke`, and `description`. A sample CSV file (`jokes.csv`) is located in `src/main/test/resources`.

**To run the application with the demo profile:**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

The "Joke of the Day" can be viewed at [`http://localhost:8080/jotd`](http://localhost:8080/jotd).

## Database Configuration

The application uses an embedded H2 database for persistence. The database is saved as a file in the `target/data` folder. No external database configuration is required. The database schema is automatically created on startup using Spring Data JPA.

## REST API Endpoints

This application provides basic CRUD operations for Jokes. All endpoints return HAL+JSON, adhering to RESTful principles.

* **Get List of Jokes:** `GET /api/v1/jokes`
    * Query Parameters:
        * `date` (optional): Filter jokes by date (e.g., `2024-01-15`).
        * `page` (optional, default: 0): The page number to retrieve.
        * `size` (optional, default: 20): The number of jokes per page.
        * `sort` (optional, default: date,asc): Sort results by property and direction (e.g., `date,desc`). Multiple sort parameters can be provided (e.g., `sort=date,asc&sort=id,desc`).
    * Response: `200 OK`

        ```json
        {
          "_embedded": {
            "jokes": [
              {
                "id": "string",
                "date": "2024-01-15",
                "joke": "string",
                "description": "string",
                "_links": {
                  "self": {
                    "href": "http://localhost:8080/api/v1/jokes/string"
                  }
                }
              },
              ...
            ]
          },
          "_links": {
            "self": {
              "href": "http://localhost:8080/api/v1/jokes?page=0&size=20"
            },
            "next": {
              "href": "http://localhost:8080/api/v1/jokes?page=1&size=20"
            },
            "prev": {
              "href": "http://localhost:8080/api/v1/jokes?page=0&size=20"
            },
            "first": {
              "href": "http://localhost:8080/api/v1/jokes?page=0&size=20"
            },
            "last": {
              "href": "http://localhost:8080/api/v1/jokes?page=4&size=20"
            }
          },
          "page": {
            "size": 20,
            "totalElements": 100,
            "totalPages": 5,
            "number": 0
          }
        }
        ```

    * Error Responses:
        * `400 Bad Request`: Invalid query parameters.

* **Add Joke:** `POST /api/v1/jokes`
    * Request Body:

        ```json
        {
          "date": "2025-04-01",
          "joke": "joke text",
          "description": "some description"
        }
        ```

    * Response `201 Created` - Joke created successfully. The `Location` header contains the URL of the created resource.
    * Example Success Response Header:

        ```
        Location: http://localhost:8080/api/v1/jokes/123e4567-e89b-12d3-a456-426614174000
        ```

    * Example Success Response Body:

        ```json
        {
          "id": "123e4567-e89b-12d3-a456-426614174000",
          "date": "2025-04-01",
          "joke": "joke text",
          "description": "some description",
          "_links": {
            "self": {
              "href": "http://localhost:8080/api/v1/jokes/123e4567-e89b-12d3-a456-426614174000"
            }
          }
        }
        ```
    * Error Responses:
        * `400 Bad Request`: Invalid request body (e.g., missing required fields, invalid date).
        * `409 Conflict`: A joke for the specified date already exists.

* **Get Joke by ID:** `GET /api/v1/jokes/{id}`
    * Response: `200 OK`

        ```json
        {
          "id": "string",
          "date": "2025-04-01",
          "joke": "string",
          "description": "string",
          "_links": {
            "self": {
              "href": "http://localhost:8080/api/v1/jokes/string"
            }
          }
        }
        ```

    * Error Responses:
        * `404 Not Found`: Joke with the specified ID does not exist.

* **Update Joke by ID:** `PUT /api/v1/jokes/{id}`
    * Request Body:

        ```json
        {
          "id": "123e4567-e89b-12d3-a456-426614174000",
          "date": "2025-04-01",
          "joke": "updated joke text",
          "description": "updated description"
        }
        ```

    * Response: `200 OK` - returns the updated joke.

        ```json
        {
          "id": "string",
          "date": "2025-04-01",
          "joke": "updated joke text",
          "description": "updated description",
          "_links": {
            "self": {
              "href": "http://localhost:8080/api/v1/jokes/string"
            }
          }
        }
        ```

    * Error Responses:
        * `400 Bad Request`: Invalid request body.
        * `404 Not Found`: Joke with the specified ID does not exist.
        * `409 Conflict`: A joke for the specified date already exists.
        * `422 Unprocessable Entity`: The "id" in the request body does not match the ID in the path.

* **Delete Joke by ID:** `DELETE /api/v1/jokes/{id}`
    * Response: `204 No Content` - Joke deleted successfully.

    * Error Responses:
        * `404 Not Found`: Joke with the specified ID does not exist.

* **Get Joke of the Day:** `GET /api/v1/jokes/today`
    * Response: `200 OK`

        ```json
        {
          "id": "string",
          "date": "2025-04-01",
          "joke": "string",
          "description": "string",
          "_links": {
            "self": {
              "href": "http://localhost:8080/api/v1/jokes/string"
            }
          }
        }
        ```

    * Error Responses:
        * `404 Not Found`: There is no joke for the current date.

The API also provides a Swagger UI for interactive exploration and documentation of the available endpoints.  You can access it at `http://localhost:8080/swagger-ui.html` after running the application.

## Demo REST API Endpoints

These endpoints are only available when the application is run with the demo profile active.

* **Upload Jokes via CSV:** `POST /jotd`
    * Request Body:

        * `csv`: A CSV file with the following columns:

            ```
            date,joke,description
            YYYY-MM-DD,joke text,some description
            ```

    * Response: `200 OK` - Jokes uploaded successfully.
  
        ```
        Jokes uploaded successfully.
        ```
    * Error Responses:
      * `500 Internal Server Error`: An error occurred during the upload, parsing, or saving of the jokes.  The response will contain an error message.

## Design Decisions and Class Descriptions

For more detailed information about design decisions and class descriptions, please refer to the `DESIGN.md` file.
