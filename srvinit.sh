#!/usr/bin/env sh
DIR=/var/run/secrets/nais.io/srvfamilie-ef-mot/

if test -d $DIR;
       export CREDENTIAL_USERNAME=familie-ef-mot
       export CREDENTIAL_PASSWORD=`cat $DIR/password`
fi