#!/bin/bash

####################################################
# Script for stopping a running Docker Compose app #
####################################################

# Stop running containers
cd "$(dirname "$0")/api"
docker compose down
docker image rm cogmaster-api

cd "$(dirname "$0")/discord/App"
docker compose down
docker image rm cogmaster-app
