#!/bin/bash

#java -Dfile.encoding=UTF-8 "-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2019.3\lib\idea_rt.jar=51627:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2019.3\bin" -classpath "${JAVA_HOME}\jre\lib\charsets.jar;${JAVA_HOME}\jre\lib\deploy.jar;
#${JAVA_HOME}\jre\lib\ext\access-bridge-64.jar;${JAVA_HOME}\jre\lib\ext\cldrdata.jar;
#${JAVA_HOME}\jre\lib\ext\dnsns.jar;${JAVA_HOME}\jre\lib\ext\jaccess.jar;${JAVA_HOME}\jre\lib\ext\jfxrt.jar;
#${JAVA_HOME}\jre\lib\ext\localedata.jar;${JAVA_HOME}\jre\lib\ext\nashorn.jar;${JAVA_HOME}\jre\lib\ext\sunec.jar;
#${JAVA_HOME}\jre\lib\ext\sunjce_provider.jar;${JAVA_HOME}\jre\lib\ext\sunmscapi.jar;${JAVA_HOME}\jre\lib\ext\sunpkcs11.jar;
#${JAVA_HOME}\jre\lib\ext\zipfs.jar;${JAVA_HOME}\jre\lib\javaws.jar;${JAVA_HOME}\jre\lib\jce.jar;
#${JAVA_HOME}\jre\lib\jfr.jar;${JAVA_HOME}\jre\lib\jfxswt.jar;${JAVA_HOME}\jre\lib\jsse.jar;
#${JAVA_HOME}\jre\lib\management-agent.jar;${JAVA_HOME}\jre\lib\plugin.jar;${JAVA_HOME}\jre\lib\resources.jar;
#${JAVA_HOME}\jre\lib\rt.jar;C:\Users\cathe\Documents\Workspace\VRPPD\out\production\VRPPD;
#C:\Program Files\IBM\ILOG\CPLEX_Studio1210\opl\lib\oplall.jar" src.Main 1 100 1 1.0 1.0

java -Dfile.encoding=UTF-8 -classpath "${JAVA_HOME}\jre\lib\*;C:\Users\cathe\Documents\Workspace\VRPPD\out\production\VRPPD;${CPLEX_STUDIO_DIR1210}\opl\lib\oplall.jar" src.Main 1 100 1 1.0 1.0