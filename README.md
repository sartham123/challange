# Chanllenge Application


## Requirements

For building and running the application you need:

- [JDK 17]
- [Gradle]

## Tech Specification in this application

- [JDK 17]
- [Spring Boot 3.2.2]

## Running the application locally

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the `com.dws.challenge.ChallengeApplication` class from your IDE.

## About this application
This application allows end users to perform the following actions:

- Create an account with a balance.
- Retrieve account details by account ID.
- Transfer funds between accounts.

## Version and Features implemented
######Version: 0.0.1-SNAPSHOT
Implemented Features:
- Create accounts with balances.
- Implemented Validation and transfer amount logic (Added API Endpoint in Controller class)
- Implemented swagger integration with Application. Can see on swagger and API information in the URL `http://localhost:18080/swagger-ui/index.html` after up application.
- Retrieve account details by account ID.
- Transfer funds between accounts with the following validations:
    - Minimum transfer amount should be 0.01.
    - Zero amount cannot be transferred.
    - Negative amount cannot be transferred.
    - The balance of the 'from' account must be greater than or equal         to the transfer amount.
    - 'From' account and 'To' account cannot be the same.
Transfer amount must be specified.
    - Both accounts involved in the transfer must exist.
 
     


