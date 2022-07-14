#include "ThreadGroup.h"
#include <iostream>
class Example{
public:
    void operator()(int id, void*sharedData){
        std::cout << "Hello from thread " << id << std::endl;
        std::string *ourData = (std::string*)sharedData;
        ourData[id] = "done " + std::to_string(id);
    }
};

const int N_THREAD = 4;

int main(){
    std::string ourData[N_THREAD];
    for(int i = 0; i < N_THREAD; i++){
        ourData[i] = "";
    }

    std::cout << "Starting " << N_THREAD << " threads." << std::endl;
    ThreadGroup<Example>example;
    for(int i = 0; i < N_THREAD; i++){
        example.createThread(i, ourData);
    }
    example.waitForAll();

    std::cout << "All threads are done! "<< std::endl;
    for(int i = 0; i < N_THREAD; i++){
        std::cout << "thread " << i << "wrote "<< ourData[i] << std::endl;
    }
    return 0; 
}