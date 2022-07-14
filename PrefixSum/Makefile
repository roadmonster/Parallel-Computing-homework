OPTIMIZATION = -O2
CPPFLAGS = -std=c++11 -Wall -Werror -pedantic -ggdb  -pthread $(OPTIMIZATION)
PROGRAMS = prefix

all : $(PROGRAMS)
% : %.cpp
	g++ $(CPPFLAGS) $< -o $@

clean :
	rm -f $(PROGRAMS)