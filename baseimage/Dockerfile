FROM anapsix/alpine-java:8_server-jre
MAINTAINER Gerard Klijs <gerard@openweb.nl>

COPY nginx-clojure /opt/nginx-clojure/

WORKDIR "/opt/nginx-clojure"

RUN addgroup -S nginx &&\
    adduser -S -D -H -G nginx nginx &&\
    chmod o+x /opt &&\
    chmod o+x /opt/nginx-clojure &&\
    chmod o+rx $(find libs -type d) &&\
    chmod o+rx $(find jars -type d) &&\
    chmod o+r $(find jars -type f) &&\
    chown -R nginx logs temp &&\
    chmod -R u+rwx logs temp

EXPOSE 80 443
CMD ["./nginx", "-g", "daemon off;"]
