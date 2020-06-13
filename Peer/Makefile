#----------PARAMETERS-------------

cinfigFile = ./config.ini

#-----------------------------
all : compile

compile :
	javac -d build -cp build/ utils/*.java
	javac -d build -cp build/ src/*.java
	

peer: compile
	java -classpath build src.Main $(cinfigFile)

clean :
	@rm -rf build
