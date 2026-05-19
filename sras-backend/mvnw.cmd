@echo off
setlocal

set "MVN_HOME=C:\Users\2494399\.m2\wrapper\dists\apache-maven-3.9.15\0226a00282e400185496f3b60ec5a3f029cbdc6893912937d4876d57695224e1"
set "PATH=%MVN_HOME%\bin;%PATH%"

call "%MVN_HOME%\bin\mvn.cmd" %*
