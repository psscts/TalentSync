@echo off
setlocal

set "MVN_HOME=C:\Users\2494447\.m2\wrapper\dists\apache-maven-3.9.10-bin\53h08a94dg6djh6umvruv7q564\apache-maven-3.9.10"
set "PATH=%MVN_HOME%\bin;%PATH%"

call "%MVN_HOME%\bin\mvn.cmd" %*
