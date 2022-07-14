CPPFLAGS = -std=c++11 -Wall -Werror -pedantic -ggdb -pthread

example: example.cpp ThreadGroup.h
	g++ $(CPPFLAGS) $< -o $@

parallel_seq: parallel_seq.cpp
	g++ $(CPPFLAGS) $< -o $@

parallel: parallel_simple.cpp ThreadGroup.h
	g++ $(CPPFLAGS) $< -o $@

hw1: hw1.cpp ThreadGroup.h
	g++ $(CPPFLAGS) $< -o $@

clear:
	rm -r *.dSYM
	rm hw1
	rm parallel
	rm parallel_seq
	rm example