Set projectLocation=C:\Users\5159473\eclipse-workspace\FedEx_Automation
cd %projectLocation%
set classpath=%projectLocation%\bin;%projectLocation%\lib\*
java org.testng.TestNG %projectLocation%\Data\XML\testng.xml