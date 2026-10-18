1) Login 

Method: POST
Endpoint: /api/auth/login
Purpose: HR login

Request:

{
  "email": "hr@company.com",
  "password": "your-password"
}

Response: authenticated session or token, depending on the authentication approach.


2) Fetch countries

Method: GET
Endpoint: /api/countries
Purpose: Fetch all countries for the UI dropdown

Response:

[
  { "id": 1, "name": "India", "isoCode": "IN" },
  { "id": 2, "name": "United States", "isoCode": "US" }
]

The UI displays the country names. When HR selects one, the frontend sends its id to fetch employees.


3) Fetch employees

Method: GET
Endpoint: /api/employees?countryId=1&page=0&size=20&sortBy=lastName&direction=asc
Purpose: Fetch employees for the selected country

Response:

{
  "content": [
    {
      "id": 101,
      "employeeCode": "EMP001",
      "firstName": "Rahul",
      "lastName": "Sharma",
      "countryId": 1,
      "annualSalary": 1200000,
      "salaryCurrency": "INR",
      "status": "ACTIVE"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 450,
  "totalPages": 23
}

4) Create employee

Method: POST
Endpoint: /api/employees
Purpose: Create employee

Request:
{
  "employeeCode": "EMP001",
  "firstName": "Rahul",
  "lastName": "Sharma",
  "email": "rahul@company.com",
  "countryId": 1,
  "annualSalary": 1200000,
  "salaryCurrency": "INR"
}

Response: 201 Created with the created employee.

5) Update employee details

Method: PUT
Endpoint: /api/employees/{id}
Purpose: Update employee details or salary


6) Deactivate employee

Method: PATCH
Endpoint: /api/employees/{id}/deactivate
Purpose: Deactivate employee

7) Median salary

Method: GET
Endpoint: /api/salaries/median?countryId=1
Purpose: Country median salary; omit countryId for global median

Response:
{
  "scope": "COUNTRY",
  "countryId": 1,
  "medianSalaryUsd": 25000.00,
  "employeeCount": 450
}

For the global median, omit countryId and return "scope": "GLOBAL"