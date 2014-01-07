#!/bin/bash

wsimport -extension -b bindings.xml -s source -d classes http://ephortetest/ephorteweb/Services/ObjectModel/V3/No/ObjectModelService.svc?wsdl
