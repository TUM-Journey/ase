# Kleo [![Build Status](https://travis-ci.com/wingsofovnia/kleo.svg?token=ouVhGzR1YKc4zojXsBsf&branch=master)](https://travis-ci.com/wingsofovnia/kleo)
Kleo is a Bluetooth-enabled electronic attendance system that is designed to cope with the problems of managing attendance lists of students.

## API
Kleo provides **REST** API for Android and Web applications. The REST API is defined with OpenAPI2 (Swagger) and availale on [Swaggerhub](https://app.swaggerhub.com/apis/wingsofovnia/kleo-api/). The Postman collection is also [provided](https://www.getpostman.com/collections/f168f5deb322113dc89a).

## Getting Started
### Configuration
In order to run the app, the following environment variables must be set:
```
# Common
ETHEREUM_INFURA - an Infura endpoint to Ethereum net (e.g https://mainnet.infura.io/your_token)
ETHEREUM_ATTENDANCE_TRACKER_ADDRESS - an address of the deployed attendance_tracker.sol smart contract

# Backend specific
ETHEREUM_WALLET_PASSWORD - a password for your wallet used to post attendances to the blockchain
ETHEREUM_WALLET_FILE  - a path to your wallet file used to post attendances to the blockchain

# Android specific
ETHEREUM_ATTENDANCE_TRACKER_URL - a url of the contract on the public blockchain explorer web
```

An example .bash_profile may look like:
export ETHEREUM_INFURA="https://ropsten.infura.io/NoXjb7h7L0YPzNSbroLJ"
export ETHEREUM_WALLET_PASSWORD="extremepassword"
export ETHEREUM_WALLET_FILE="~/UTC--2018-01-11T18-58-12.059Z--0121a28a3a04a71bd11f4749ca23f2585b4844d5"
export ETHEREUM_ATTENDANCE_TRACKER_ADDRESS = "0x324f85e86b1c42f24894c31aef1d74360ef8607e"
export ETHEREUM_ATTENDANCE_TRACKER_URL="https://ropsten.etherscan.io/address/0x324f85e86b1c42f24894c31aef1d74360ef8607e"

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

To run the android client application run:
```
$ ./gradlew :android:installDevDebug
```
This will build the APK and immediately install it on a running emulator or connected device. The backend API client in the app will point to local IP of the machine used to build the apk.

## Bugs and Feedback
For bugs, questions and discussions please use the [Github Issues](https://github.com/TUM-Journey/kleo/issues).

## License
Except as otherwise noted this software is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
