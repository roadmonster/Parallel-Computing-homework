CPPFLAGS = -std=c++11 -Wall -Werror -pedantic -ggdb -pthread
PROGRAMS = bitonic bitonic_loop bitonic_rec 
FOLDERS = *.dSYM
bitontic_loop : 
	g++ $(CPPFLAGS) $< -o $@

clean :
	rm -f $(PROGRAMS)
	rm -r $(FOLDERS)
