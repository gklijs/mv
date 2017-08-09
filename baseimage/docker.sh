#!/usr/bin/env bash

curl -o nginx-clojure.tar.gz https://netcologne.dl.sourceforge.net/project/nginx-clojure/nginx-clojure-0.4.5.tar.gz
tar -xzvf nginx-clojure.tar.gz
mv nginx-clojure-0.4.5 nginx-clojure
mv nginx-clojure/nginx-linux-x64 nginx-clojure/nginx
rm nginx-clojure/nginx-linux-i586
rm nginx-clojure/nginx-macosx
rm nginx-clojure/nginx-win32.exe
rm nginx-clojure/nginx-win64.exe
rm nginx-clojure/jars/nginx-clojure-embed-0.4.5.jar
rm nginx-clojure/jars/nginx-jersey-0.1.4.jar
rm nginx-clojure/jars/nginx-tomcat8-0.2.4.jar

docker build -t klijs/nginx-clojure:0.4.5 .
rm -rf nginx-clojure
rm nginx-clojure.tar.gz