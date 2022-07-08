#include <iostream>

int encode(int v){
    for(int i = 0; i < 500; i++){
        v = ((v * v) + v) % 10; 
    }
    return v;
}

int decode(int v){
    return encode(v);
}

void prefixSum(int *data, int length){
    int encodedSum = 0;
    for(int i = 0; i < length; i++){
        encodedSum += encode(data[i]);
        data[i] = decode(encodedSum);
    }
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