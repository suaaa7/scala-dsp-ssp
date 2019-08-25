#!/bin/sh

curl http://172.18.0.2:8082/v1/ad -X POST -H "Content-Type:application/json" -d @SSP/AdReqBody.json -i
