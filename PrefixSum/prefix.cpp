#include <vector>
#include <future>
#include <iostream>
#include <chrono>

using namespace std;
const int N = 1 << 26;
const int PARALLEL_LV = 4;
typedef vector<int> Data;

class Heaper{
public:
    Heaper(const Data* data):data(data), n(data->size()){
        interior = new Data(n-1, 0);
    }
    ~Heaper(){
        delete interior;
    }
protected:
    const Data* data;
    int n;
    Data* interior;

    virtual int size(){
        return n-1 + n;
    }

    virtual int left(int i){
        return 2 * i + 1;
    }

    virtual int right(int i){
        return left(i) + 1;
    }

    virtual bool isLeaf(int i){
        return right(i) >= size();
    }

    virtual int value(int i){
        if ( i < n -1 )
            return interior->at(i);
        else{
            return data->at(i - (n - 1));
        }    
    }

};

class SumHeap: public Heaper{
public:
    SumHeap(const Data* data):Heaper(data), thread_cnt(1){
        calcSum(0, 0);
    }

    void getPrefix(Data* output){
        thread_cnt = 1;
        calcPrefix(0, 0, 0, output);
    }

    int threadUsed(){
        return thread_cnt;
    }
private:
    int thread_cnt;
    void calcSum(int i, int lv){
        if(!isLeaf(i)){
            if(lv < PARALLEL_LV){
                auto handle = async(launch::async, &SumHeap::calcSum, this, left(i), lv + 1);
                thread_cnt++;
                calcSum(right(i), lv + 1);
                handle.wait();
            }else{
                calcSum(left(i), lv + 1);
                calcSum(right(i), lv + 1);
            }
            interior->at(i) = value(left(i)) + value(right(i));
        }
    }

    void calcPrefix(int i, int sumPrior, int lv, Data* output){
        if(isLeaf(i)){
            output->at(i - (n -1)) = sumPrior + value(i);
        }else{
            if(lv < PARALLEL_LV){
                auto handle = async(launch::async, &SumHeap::calcPrefix, this, left(i), sumPrior, lv + 1, output);
                thread_cnt++;
                calcPrefix(right(i), sumPrior + value(left(i)), lv + 1, output);
            }else{
                calcPrefix(left(i), sumPrior, lv + 1, output);
                calcPrefix(right(i), sumPrior + value(left(i)), lv + 1, output);
            }
        }
    }
};

int main(){
    Data data(N, 1);
    Data output(N, 1);

    auto start = chrono::steady_clock::now();
    SumHeap heap(&data);
    heap.getPrefix(&output);
    auto end = chrono::steady_clock::now();
    auto elapsed = chrono::duration<double, milli>(end - start).count();

    int check = 1;
    for(int elem: output){
        if(elem != check++){
            cout << "Failed at " << check - 1 << endl;
            break;
        }
    }

    cout << "Computed " << N << " prefix sum in " << elapsed 
    << " ms using " << heap.threadUsed() << "threads. " << endl;

    return 0;


}

