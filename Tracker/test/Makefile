CFLAGS=-Wall -Wextra -std=c99
EXECUTABLE_NAME=Client

C_FILES      := $(wildcard *.c)
O_FILES      := $(patsubst %.c, %.o, $(C_FILES))

all : clean compile

compile: $(O_FILES)
	@echo "Création de l'executable\e[1m\e[33m" $(EXECUTABLE_NAME) "\e[0m\e[39m"
	@gcc $(CFLAGS) *.o -o $(EXECUTABLE_NAME)
	@rm -f *.o

%.o: %.c
	@gcc $(CFLAGS) $< -c -o $@
	@printf "Compilation de \e[1m\e[34m%-12s \e[32mDONE\e[0m\e[39m\n" $<

clean:
		@rm -f $(EXECUTABLE_NAME) *.o
