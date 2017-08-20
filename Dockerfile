FROM klijs/nginx-clojure:0.4.5
MAINTAINER Gerard Klijs <gerard@openweb.nl>

COPY target/uberjar/m-venue-default.jar libs
COPY conf/logback.xml libs/res
COPY conf/nginx.conf conf/nginx.conf
COPY resources/public public

RUN chmod o+r libs/m-venue-default.jar &&\
    chmod -R -w public &&\
    chown -R nginx public &&\
    chmod u+rx $(find public -type d) &&\
    chmod u+r $(find public -type f)

EXPOSE 80 443
CMD ["./nginx", "-g", "daemon off;"]
