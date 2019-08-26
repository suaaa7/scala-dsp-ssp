#!/bin/sh

curl http://172.18.0.3:8081/v1/ad -X POST -H "Content-Type:application/json" -d @DSP/AdReqBody.json -i
