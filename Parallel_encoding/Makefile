CPPFLAGS = -std=c++11 -Wall -Werror -pedantic -ggdb -pthread

example: example.cpp ThreadGroup.h
	g++ $(CPPFLAGS) $< -o $@

parallel_seq: parallel_seq.cpp
	g++ $(CPPFLAGS) $< -o $@