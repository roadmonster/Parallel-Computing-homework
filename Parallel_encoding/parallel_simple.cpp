#include <iostream>
#include "ThreadGroup.h"
using namespace std;

int encode(int v){
    for(int i = 0; i < 500; i++){
        v = ((v * v) + v) % 10; 
    }
    return v;
}

int decode(int v) {
	return encode(v);
}

struct SharedData{
    static const int N_THREADS = 2;
    int start[N_THREADS];
    int end[N_THREADS];
    int* data;
};

class ParaCoder{
public:
    void operator()(int id, void* data){
        SharedData* myData = (SharedData*)data;

        int start = myData->start[id];
        int end = myData->end[id];

        for(int i = start; i < end; i++){
            myData->data[i] = encode(myData->data[i]);
        }
    }
};

class ParaDecoder{
public:
    void operator()(int id, void* data){
        SharedData* myData = (SharedData*)data;

        int start = myData->start[id];
        int end = myData->end[id];

        for(int i = start; i < end; i++){
            myData->data[i] = decode(myData->data[i]);
        }
    }
};

void prefixSum(int *data, int length){
    SharedData ourData;
	ourData.start[0] = 0;
	int pieceLength = length/SharedData::N_THREADS;
	for (int t = 1; t < SharedData::N_THREADS; t++)
		ourData.end[t-1] = ourData.start[t] = ourData.start[t-1] + pieceLength;
	ourData.end[SharedData::N_THREADS-1] = length;
	ourData.data = data;

    ThreadGroup<ParaCoder> encoders;
	for (int t = 0; t < SharedData::N_THREADS; t++)
		encoders.createThread(t, &ourData);
	encoders.waitForAll();

	int encodedSum = 0;
	for (int i = 0; i < length; i++) {
		encodedSum += data[i];
		data[i] = encodedSum;
	}
    ThreadGroup<ParaDecoder>decoderThreads;
    for(int i = 0; i < SharedData::N_THREADS; i++){
        decoderThreads.createThread(i, &ourData);
    }
    decoderThreads.waitForAll();

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