# nfllook-tools-data-export
Export nfllook data to MongoDB

## Prerequisites:
1. NFL Game data directory: $NFLGameData_Dir
2. Java
3. MongoDB. $MongoDBUri format: mongodb://user:password@host:port/database

## Build app
mvnw clean install

## Run app
java -jar target\data-export-0.0.1-SNAPSHOT.jar --path=$NFLGameData_Dir --season=$season --uri=$MongoDBUri