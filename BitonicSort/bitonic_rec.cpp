#include <iostream>
#include <vector>
#include <chrono>
#include <random>

using namespace std;

typedef vector<int>Data;
const int ORDER = 26;
const int N = 1 << ORDER;

template<typename Container>
class Bitonic{
public:
    Bitonic(Container *data): data(data), n(data->size()){}

    void sort(){
        sort(0, n, UP);
    }

    void dump(ostream &o, int start = 0, int end = -1, string label = ""){
        if (end == -1)
            end = n;
        o << "[" << start << ":" << end << "]";
        for(int i = start; i < end; i++){
            o << (*data)[i] << " ";
        }
        o << label << endl;
    }
private:
    Container *data;
    int n;
    enum Direction{
        UP, DOWN
    };

    void bitonic_merge(int m, int half, Direction way){
        if (way ==  UP){
            for(int i = 0; i < half; i++){
                if ((*data)[m + i] > (*data)[m + half + i])
                    std::swap((*data)[m + i], (*data)[m + half + i]);
            }
        }else{
            for (int i = 0; i < half; i++)
                if((*data)[m+ i] < (*data)[m + half + i])
                    std::swap((*data)[m + i], (*data)[m + half + i]);
        }
    }

    void bitonic_sort(int m, int k, Direction way){
        if (k > 1){
            int half = k / 2;
            bitonic_merge(m, half, way);
            bitonic_sort(m, half, way);
            bitonic_sort(m + half, half, way);
        }
    }

    void bitonic_sequence(int m, int k){
        if(k > 1){
            int half = k / 2;
            sort(m, half, UP);
            sort(m + half, half, DOWN);
        }
    }

    void sort(int m, int k, Direction way){
        bitonic_sequence(m, k);
        bitonic_sort(m, k, way);
    }
};

void fillRandom(Data &v, int lo, int hi){
    uniform_int_distribution<int> dist(lo, hi);
    random_device rd;
    mt19937 source(rd());
    for(int i = v.size() -1; i >= 0; i--){
        v[i] = dist(source);
    }
}

int main(){
    Data data(N, 0);

    fillRandom(data, 0, N);
    auto start1 = chrono::steady_clock::now();
    sort(data.begin(), data.end());
    auto end1 = chrono::steady_clock::now();
    auto elapse1 = chrono::duration<double, milli>(end1 - start1).count();

    fillRandom(data, 0, N);

    start1 = chrono::steady_clock::now();
    Bitonic<Data>bitonic(&data);
    bitonic.sort();
    end1 = chrono::steady_clock::now();
    auto elapse2 = chrono::duration<double, milli>(end1 - start1).count();

    int check = -1;
    for(int elem: data){
        if(elem < check){
            cout << "Failed result at " << check << endl;
            break;
        }
        elem = check;
    }
    cout << "Default compute in " << elapse1 << " ms" << endl;
    cout << "Parallel compute in " << elapse2 << " ms" << endl;
    return 0; 
}