#!/usr/bin/env sh
DIR=/var/run/secrets/nais.io/srvfamilie-ef-mot/

if test -d $DIR;
then
       export CREDENTIAL_USERNAME=familie-ef-mot
       export CREDENTIAL_PASSWORD=`cat /var/run/secrets/nais.io/srvfamilie-ef-mot/password`
       echo "- exported CREDENTIAL_PASSWORD for familie-ef-mot "
fi