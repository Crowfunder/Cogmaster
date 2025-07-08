# Getting the app ready
1. Clone the repository
```sh
$ git clone https://github.com/Crowfunder/Cogmaster.git
```
2. If you wish for the bot to display icons, extract them from the game directory `Spiral Knights/rsrc/ui` and put into `Cogmaster/discord/App/Src/Assets` dir.

# Deployment
There are scripts that automatically deploy the entire app, below are manual steps.
## Through Docker Compose
1. Enter the discord dir
```
$ cd Cogmaster/discord/App
```
2. For docker deployment with any frontend, initalize a Docker Network so that the containers share a network.
```sh
$ docker network create (NETWORK NAME)
```
3. Run docker compose
```sh
$ docker compose up -d
```
