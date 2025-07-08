# Deployment
There are two (three) supported ways to launch the api. There are two scripts (`deploy.sh` and `shutdown.sh`) for deployment of the entire app that use docker compose method.
## Standard, through Maven
1. Clone the repository
```sh
$ git clone https://github.com/Crowfunder/Cogmaster.git
```
3. If you don't have a JDK v17 already, install one, for example [Zulu](https://www.azul.com/downloads/?version=java-17-lts&package=jdk#zulu)
4. Get the game configs as xmls, from now on they will be called "parseable", to get the xml configs you may need to use a tool like [Datdec](https://github.com/lucasluqui/datdec), put them into folder named "parseable"
5. Get the game translations .properties files, open "code/projectx-config.jar" in something like 7zip or WinRar, go into i18n folder, put them into a folder named "properties" 
6. Put the extracted resources into `Cogmaster/api/src/main/resources`, so that "parseable" and "properties" are in the same folder as routers
7. Open terminal in the `Cogmaster/api` folder, run the following command
```sh
$ ./mvnw spring-boot:run
```

## Docker Run
1. Clone the repository
```sh
$ git clone https://github.com/Crowfunder/Cogmaster.git
```
3. Enter the directory with the api
```sh
$ cd Cogmaster/api
```
3. For docker deployment with any frontend, initalize a Docker Network so that the containers share a network.
```sh
$ docker network create (NETWORK NAME)
```
4. Build the container
```sh
$ docker build --tag "cogmaster-api"
```
5. Run the container, note, map the port to any port you like, assure they match with the front-end
```sh
docker run --detach --name 'cogmaster-api' -p 2137:2137 'cogmaster-api'
```

## Docker Compose
1. Clone the repository
```sh
$ git clone https://github.com/Crowfunder/Cogmaster.git
```
3. Enter the directory with the api
```sh
$ cd Cogmaster/api
```
3. For docker deployment with any frontend, initalize a Docker Network so that the containers share a network.
```sh
$ docker network create (NETWORK NAME)
```
4. Run docker compose
```sh
$ docker compose up -d
```
