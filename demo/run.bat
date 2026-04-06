@echo off
set "JAVA_HOME=C:\Program Files\Java\latest\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d c:\Users\Administrator\Desktop\Caixin_work\algo-mind\demo
"C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.14\ed7edd442f634ac1c1ef5ba2b61b6d690b5221091f1a8e1123f5fadcc967520d\bin\mvn.cmd" spring-boot:run
