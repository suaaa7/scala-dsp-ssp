#!/bin/sh

curl http://localhost:8081/v1/ad/g -X POST -H "Content-Type:application/json" -d @test/AdReqBodyForDSP.json -i
