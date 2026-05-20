@echo off
setlocal

set "MVN_HOME=C:\Users\2494598\.m2\wrapper\dists\apache-maven-3.9.14\ed7edd442f634ac1c1ef5ba2b61b6d690b5221091f1a8e1123f5fadcc967520d"
set "PATH=%MVN_HOME%\bin;%PATH%"

call "%MVN_HOME%\bin\mvn.cmd" %*
