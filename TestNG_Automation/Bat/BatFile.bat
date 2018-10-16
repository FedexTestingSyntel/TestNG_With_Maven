Set projectLocation=%cd%
cd %projectLocation%
mvn clean test -DsuiteXmlFile=WFCLtestng.xml
pause