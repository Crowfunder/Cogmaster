#!/bin/bash

##########################################
# Deployment script using Docker Compose #
##########################################

# Update git, fetch updates
cd "$(dirname "$0")"
git pull

cd "$(dirname "$0")/api"
docker compose down
docker image rm cogmaster-api

cd "$(dirname "$0")/discord/App"
docker compose down
docker image rm cogmaster-app

# Ensure network is running
cd "$(dirname "$0")"
docker network inspect cogmaster-net >/dev/null 2>&1 || docker network create cogmaster-net

# Run API Back-End Docker container
cd "$(dirname "$0")/api"
docker compose up -d

# Run Discord Front-End Docker Container
cd "$(dirname "$0")/discord/App"
docker compose up -d
