#!/usr/bin/env bash

lein uberjar
docker build -t klijs/website .
docker run -d -p 8080:80 --name website_instance klijs/website