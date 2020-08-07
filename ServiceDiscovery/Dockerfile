FROM busybox:uclibc
WORKDIR /api
RUN mkdir resources
COPY serviceDiscovery .
COPY servicediscovery.yaml resources
CMD /api/serviceDiscovery -v 3 -configfile servicediscovery.yaml
