# Notes Application

A simple Spring Boot + MongoDB REST API that allows users to create, update, delete, and list notes with filtering, sorting, and word statistics.  

## Requirements
It is necessary to have docker and docker compose installed, otherwise it would not be possible to run tests.

## Run the Application (via Docker)

### Build and Run
Execute in the project's root directory:
```bash

docker compose up --build -d
```

Check that both containers are running successfully:
```bash

docker ps
```

### Access API
The API will be available at:
```
http://localhost:8080/api/notes
```

### Stop Containers
```bash

docker-compose down
```
To remove data volumes:
```bash

docker-compose down -v
```

## Run the Tests
Execute in the project's root directory:
```bash

docker compose up --build mongo -d

mvn test
```


## API Endpoints

| Method | Endpoint | Description |
|--------|-----------|-------------|
| **POST** | `/api/notes` | Create new note |
| **GET** | `/api/notes` | List notes (Title + Created Date only) |
| **GET** | `/api/notes?tag=BUSINESS&page=0&size=5` | Filter by tag + pagination |
| **GET** | `/api/notes/{id}` | Get full note (with text & tags) |
| **PUT** | `/api/notes/{id}` | Update existing note |
| **DELETE** | `/api/notes/{id}` | Delete note |
| **GET** | `/api/notes/{id}/stats` | Get word usage statistics |


