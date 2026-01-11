# Rainbow - Backend Take-Home Assignment

## Requirements

- Java 25
- Gradle

How to check Java version:
```bash
java -version
```

## How to run the project
```bash
./gradlew bootRun
```
It will start the application on port 8080.

## API endpoints v1
## API endpoints v2
### Get latest version of a record
```http
GET /api/v2/records/{id}
```
Description
Returns the latest version of the record with the given id.
Responses
200 OK — record found
404 Not Found — record does not exist

### Get a specific version of a record
```http
GET /api/v2/records/{id}?version={version}
```
Parameters
id — record identifier
version — record version number
Responses
200 OK — version found
404 Not Found — record or version does not exist

### Get record version history
```http
GET /api/v2/records/{id}/history
```
#### Description
Returns all versions of the record, sorted by version in ascending order.
#### Responses
* 200 OK — list of record versions
* 404 Not Found — record does not exist

### Create a new version of a record (JSON Merge Patch)
```http
POST /api/v2/records/{id}
```
#### Description
If the record does not exist, creates version 1.
If the record exists, creates a new version (latest + 1).
Updates are applied using JSON Merge Patch semantics:
  * null → remove field
  * object → deep merge
  * scalar / array → replace value
#### Request Body (JSON)
```json
{
  "field": "value",
  "nested": {
    "a": 1
  }
}
```
#### Responses
* 200 OK — returns the newly created record version