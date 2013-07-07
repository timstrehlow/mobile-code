cd MobileCodeServer
javac -sourcepath src -d bin src/server/Server.java
echo "Server compiled"
cd ../MobileCodeClient
javac -sourcepath src -d bin src/SocketClient.java
echo "Client compiled"
echo "Starting server"
cd ../MobileCodeServer
java -cp bin server.Server &
serverPID=`ps | grep "server.Server" | grep -v "grep" | awk '{print $1}'`
echo "Starting client"
cd ../MobileCodeClient
javac -sourcepath src -d bin src/SocketClient.java
java -cp bin SocketClient
echo "Stopping server..."
kill $serverPID
