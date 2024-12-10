# Credit Api
Credit Api project aims to manage credit operations for customers. The Credit Api project is containerized. There is docker compose file in the root directory which helps you to run the Credit Api project.
Unit tests are under the test folder.

## Note:
To be able to call the endpoint you need to be logged in first. 
If you use the postman collection which attached to the repo, you would not need to grab the token and add it to header. Test script handles it automatically. You just need to be logged in with one of these users below, then execute endpoints.

Admin email : omer@outlook.com passowrd: 12345

User email : ali@outlook.com password: 12345

User email : veli@outlook.com password: 12345


## Includes:
Java, Spring boot, Spring security, Spring Data, Docker, H2

## To run the project:
After downloading the project you can directly run the project any idea.
Or you can go to root directory of project and open the console and run the commands:
- mvn clean install
- docker compose up or docker-compose up (only if you have docker on your machine)

## Endpoints:
The project runs on port 8080 as default.
Admin can reach every endpoint.
Regular users can reach only Authenticate, Get Loan, Get Loan Installments and Pay Loan endpoint.

Authenticate - HTTP Request type : POST
Endpoint : localhost:8080/api/auth
Example payload:

{
"email" : "omer@outlook.com",
"password" : "12345"
}

Example response:
{
"token": "eyJhbGciOiJIUzI1...."
}

***********************************************************

Create Loan - HTTP Request type : POST
Endpoint : localhost:8080/api/loans
Example payload:

{
"customerId": 2,
"amount": 10,
"interestRate": 0.5,
"numberOfInstallments": 6
}

Example response:
{
Loan created successfully.
}

***********************************************************

Pay Loan - HTTP Request type : POST
Endpoint : localhost:8080/api/loans/1/pay
Example payload:

{
"amount":250
}

{
"installmentsPaid": 1,
"totalAmountSpent": 500,
"loanFullyPaid": false
}

***********************************************************

Get Loan - HTTP Request type : GET
Endpoint : localhost:8080/api/loans?customerId=1&numberOfInstallments=12&isPaid=false&page=1&size=5

numberOfInstallments, isPaid, page and size parameters are optional.
Example response:
{
"content": [
{
"loanId": 1,
"loanAmount": 15.0,
"numberOfInstallments": 6,
"isPaid": false
}
],
"page": {
"size": 1,
"number": 0,
"totalElements": 1,
"totalPages": 1
}
}

***********************************************************

Get Loan Installments - HTTP Request type : GET
Endpoint : localhost:8080/api/loans/1/installments

Example response:
[
{
"amount": 2.5,
"paidAmount": 0.0,
"dueDate": "2025-01-01",
"paymentDate": null,
"isPaid": false
},
{
"amount": 2.5,
"paidAmount": 0.0,
"dueDate": "2025-02-01",
"paymentDate": null,
"isPaid": false
},
{
"amount": 2.5,
"paidAmount": 0.0,
"dueDate": "2025-03-01",
"paymentDate": null,
"isPaid": false
},
{
"amount": 2.5,
"paidAmount": 0.0,
"dueDate": "2025-04-01",
"paymentDate": null,
"isPaid": false
},
{
"amount": 2.5,
"paidAmount": 0.0,
"dueDate": "2025-05-01",
"paymentDate": null,
"isPaid": false
},
{
"amount": 2.5,
"paidAmount": 0.0,
"dueDate": "2025-06-01",
"paymentDate": null,
"isPaid": false
}
]

***********************************************************
