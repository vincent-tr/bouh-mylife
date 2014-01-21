LIBS  := -lircclient -lmpdclient -ljansson -lmicrohttpd -lconfig
CFLAGS := -I"./include" -O2 -g -Wall -std=gnu99
TARGET := mylife-home-components
SRCS := $(wildcard src/*.c)
OBJS := $(addprefix obj/,$(notdir $(SRCS:.c=.o)))

all: objdir $(TARGET)

objdir:
	mkdir -p obj

$(TARGET): $(OBJS)
	gcc $(CFLAGS) $(LIBS) -o $@ $^

obj/%.o: src/%.c
	gcc $(CFLAGS) -c -o $@ $<
    
clean:
	rm -rf obj $(TARGET) 

install:
	ln -s $(realpath $(shell pwd))/$(TARGET) /etc/init.d/$(TARGET)
	@echo service $(TARGET) created
	/etc/init.d/$(TARGET) start

uninstall:
	-/etc/init.d/$(TARGET) stop
	rm -f /etc/init.d/$(TARGET)
	@echo service $(TARGET) deleted