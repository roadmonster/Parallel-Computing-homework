#include <iostream>
#include <vector>
#include "ThreadGroup.h"
using namespace std;

int* data;
const int N_THREADS = 4;
class ParaCoder{
public:
    void operator()(int id, void* data, int length){
        int* myData = (int*)data;
        int piece = length / N_THREADS;
        int start = id * piece;
        int end = id != N_THREADS-1 ? (id + 1) * piece : length;

        for(int i = start; i < end; i++){
            myData[i] = encode(myData[i]);
        }
    }
};

class ParaDecoder{
public:
    void operator()(int id, void* data, int length){
        int *myData = (int*)data;
        int piece = length / N_THREADS;
        int start = id * piece;
        int end = id != N_THREADS-1 ? (id + 1) * piece : length;

        for(int i = start; i < end; i++){
            myData[i] = encode(myData[i]);
        }
    }
};
int encode(int v){
    for(int i = 0; i < 500; i++){
        v = ((v * v) + v) % 10; 
    }
    return v;
}


void prefixSum(int *data, int length){
    ThreadGroup<ParaCoder>coderThreads;
    for(int i = 0; i < N_THREADS; i++){
        coderThreads.createThread(i, data);
    }
    coderThreads.waitForAll();

    int encodedSum = 0;
    for(int i = 0; i < length; i++){
        encodedSum += data[i];
        data[i] = encodedSum;
    }
    ThreadGroup<ParaDecoder>decoderThreads;
    for(int i = 0; i < N_THREADS; i++){
        decoderThreads.createThread(i, data);
    }
    coderThreads.waitForAll();
}


int main(){
    int length = 1000 * 1000;

    int *data = new int[length];
    for(int i = 1; i < length; i++){
        data[i] = 1;
    }
    data[0] = 6;

    prefixSum(data, length);
    std::cout << data[0] << std::endl;
    std::cout << data[length/2] << std::endl;
    std::cout << data[length-1] << std::endl;

    delete[] data;
    return 0;
}