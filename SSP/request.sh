#!/bin/sh

curl http://localhost:8082/v1/ad -X POST -H "Content-Type:application/json" -d @test/AdReqBodyForSSP.json -i
