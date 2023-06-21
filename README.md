# Sunbird-notification-service

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b963e5ed122f47b5a27b19a87d9fa6de)](https://app.codacy.com/app/sunbird-bot/sunbird-notification-service?utm_source=github.com&utm_medium=referral&utm_content=project-sunbird/sunbird-groups-service&utm_campaign=Badge_Grade_Settings)

This is the repository for notification service micro-service. It provides the APIs for notification service functionality of Sunbird.

The code in this repository is licensed under MIT License unless otherwise noted. Please see the [LICENSE](https://github.com/Sunbird-Lern/sunbird-notification-service/blob/master/LICENSE) file for details.
This readme file describes how to install and start groups-service in your development environment.
## Sunbird-notification-service development environment setup:
This readme file contains the instruction to set up and run the Sunbird-notification-service in your development environment.

### System Requirements

### Prerequisites

- Java 11
- Latest Docker
- Latest Maven (Only For Mac m1 users use 3.8.8 Maven version)

### Prepare folders for database data and logs

To prepare folders for database data and logs, run the following command:

```shell
mkdir -p ~/sunbird-dbs/cassandra ~/sunbird-dbs/es 
export sunbird_dbs_path=~/sunbird-dbs
```

To verify the creation of folders, run:

```shell
echo $sunbird_dbs_path
```

### Cassandra database setup in Docker

1. To get the Cassandra image, use the following command:

```shell
docker pull cassandra:3.11.6 
```
For Mac M1 users follow the bellow command:
```shell
docker pull --platform=linux/amd64 cassandra:3.11.6 
```

For the network, you can either use an existing network or create a new one by executing the following command:
```shell
docker network create sunbird_db_network
```

2. To create the Cassandra instance, run the following command:
```shell
docker run -p 9042:9042 --name sunbird_cassandra \
 -v $sunbird_dbs_path/cassandra/data:/var/lib/cassandra \
 -v $sunbird_dbs_path/cassandra/logs:/opt/cassandra/logs \
 -v $sunbird_dbs_path/cassandra/backups:/mnt/backups \
 --network sunbird_db_network -d cassandra:3.11.6 
```


For Mac M1 users follow the below command:
```shell
docker run --platform=linux/amd64 -p 9042:9042 --name sunbird_cassandra \
 -v $sunbird_dbs_path/cassandra/data:/var/lib/cassandra \
 -v $sunbird_dbs_path/cassandra/logs:/opt/cassandra/logs \
 -v $sunbird_dbs_path/cassandra/backups:/mnt/backups \
 --network sunbird_db_network -d cassandra:3.11.6 
```

3. To verify the setup, run the following command, which will show the status of Cassandra as up and running:

```shell
docker ps -a | grep cassandra
```

## To create/load keyspaces and tables to Cassandra

Click the link [sunbird-utils-cassandra-setup](https://github.com/Sunbird-Lern/sunbird-utils/tree/release-5.3.0#readme)
and follow the steps for creating/loading the Cassandra keyspaces and tables to your development environment.

Note: It is mandatory to follow the instructions provided in the link.

4. To verify the creation of keyspaces and tables, connect to the Cassandra Docker container using SSH and run the following command:

```shell
docker exec -it sunbird_cassandra /bin/bash
```

## Sunbird-notification-service Setup

To set up the notification service, follow the steps below:

1. Clone the latest branch of the notification service using the following command:
```shell
git clone https://github.com/Sunbird-Lern/sunbird-notification-service
```

2. Set up the necessary environment variables by running the following script in the path `<project-base-path>/sunbird-notification-service`:
```shell
./scripts/notification-service-config.sh
```

3. Build the application using the following maven command in the path `<project-base-path>/sunbird-notification-service`:
```shell
mvn clean install -DskipTests
```
Make sure the build is successful before proceeding to the next step. If the build is not successful,
fix any configuration issues and rebuild the application.

4. Run the netty server using the following maven command in the path `<project-base-path>/sunbird-notification-service/service`:
```shell
mvn play2:run
```

5. Verify the database connections by running the following command:
```shell
curl --location --request GET 'http://localhost:9000/health’
```
If all connections are established successfully, the health status will be shown as 'true', otherwise it will be 'false'.

Currently, the sunbird-notification-service service is dependent on User org service for getting user and organisation information.
We are planning to implement a mock service soon for these dependencies.

To make the notification service completely working, some pre-required configuration setup is mandatory.
Follow the steps given in the link [pre-required configuration setup](https://github.com/Sunbird-Lern/sunbird-notification-service/blob/release-5.4.0/notificationsetup.md) to complete the setup.