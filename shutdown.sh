#!/bin/bash

####################################################
# Script for stopping a running Docker Compose app #
####################################################

# Stop running containers
cd /root/Cogmaster/api
docker compose down
docker image rm cogmaster-api

cd /root/Cogmaster/discord/App
docker compose down
docker image rm cogmaster-app
