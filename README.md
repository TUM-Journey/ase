# Kleo [![Build Status](https://travis-ci.com/wingsofovnia/kleo.svg?token=ouVhGzR1YKc4zojXsBsf&branch=master)](https://travis-ci.com/wingsofovnia/kleo)
Kleo is a Bluetooth-enabled electronic attendance system that is designed to cope with the problems of managing attendance lists of students.

## API
Kleo provides **REST** API for Android and Web applications. The REST API is defined with OpenAPI2 (Swagger) and availale on [Swaggerhub](https://app.swaggerhub.com/apis/wingsofovnia/kleo-api/). The Postman collection is also [provided](https://www.getpostman.com/collections/f168f5deb322113dc89a).

## Getting Started
### Configuration
In order to run the app, the following environment variables must be set:
```
ethereum.infura - an Infura endpoint to Ethereum net (e.g https://mainnet.infura.io/your_token)
ethereum.wallet.password - a password for your wallet
ethereum.wallet.file - a path to your wallet file
```

### Development
The backend default profile is DEV, populated with test data: groups and user accounts. To run the backend, execute:
```
$ ./gradlew bootRun
```
This will run the backend on `http://localhost:8080/api` with DEV profile activated.

The DEV profile is populated with the following user you may use for development:

| Username         | Password     | Roles                     | Configurable In    |
|------------------|--------------|---------------------------|--------------------|
| superuser@tum.de | password     | SUPERUSER, STUDENT, TUTOR | data.sql           |
| student@tum.de   | password     | STUDENT                   | data.sql           |
| tutor@tum.de     | password     | TUTOR                     | data.sql           |

You still can use any valid student or staff TUM Shibboleth account.

To run the android client application, make sure you have a valid IP address of the backend in `configuration.xml` config (`backend.baseUrl`) of the build variant used.

## Bugs and Feedback
For bugs, questions and discussions please use the [Github Issues](https://github.com/TUM-Journey/kleo/issues).

## License
Except as otherwise noted this software is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
