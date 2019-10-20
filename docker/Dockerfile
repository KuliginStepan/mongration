FROM mongo:4.2

RUN echo "rs.initiate();" > /docker-entrypoint-initdb.d/replica-init.js

HEALTHCHECK --interval=5s --retries=10 CMD OUT=$(mongo --eval "db.isMaster().ismaster" | tail -n 1); if [ $OUT != "true" ]; then exit 1; fi

CMD ["--replSet", "rs0"]