#!/bin/bash

##########################################
# Deployment script using Docker Compose #
##########################################

# Update git, fetch updates
cd /root/Cogmaster
git pull

cd /root/Cogmaster/api
docker compose down
docker image rm cogmaster-api

cd /root/Cogmaster/discord/App
docker compose down
docker image rm cogmaster-app

# Ensure network is running
cd /root/Cogmaster
docker network inspect cogmaster-net >/dev/null 2>&1 || docker network create cogmaster-net

# Run API Back-End Docker container
cd /root/Cogmaster/api
docker compose up -d

# Run Discord Front-End Docker Container
cd /root/Cogmaster/discord/App
docker compose up -d
