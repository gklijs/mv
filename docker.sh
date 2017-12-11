#!/usr/bin/env bash

lein test :only m-venue.repo-test/set-prod-img-info
lein uberjar
docker build -t klijs/website .
docker run -d -p 8080:80 --name m_venue_instance klijs/website