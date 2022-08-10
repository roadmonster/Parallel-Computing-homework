CPPFLAGS = -std=c++11 -Wall -Werror -pedantic -ggdb -O0 -pthread
PROGRAMS = general20 general

all : $(PROGRAMS)

% : %.cpp GeneralScan.h
	g++ $(CPPFLAGS) $< -o $@

clean :
	rm -f $(PROGRAMS)
