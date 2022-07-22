#include <iostream>
#include <vector>
#include <random>

using namespace std;

const int ORDER = 8;
const int N = 1 << ORDER;
typedef vector<int> Data;

template<typename Container>
class Bitonic{
public:
    Bitonic(Container *data): data(data), n(data->size()){}

    void sort(){
        cout << "k\tj\ti\ti^t\ti&k" << endl;

        for(int k = 2; k <= n; k *=2){
            cout << fourbits(k) << "\t";

            for(int j = k / 2; j > 0; j /=2){
                if(j != k / 2)
                    cout << " \t";
                cout << fourbits(j) << "\t";

                for(int i = 0; i < n; i++){
                    if(i != 0)
                        cout << "  \t  \t";
                    cout << fourbits(i) << "\t";

                    int ixj = i ^ j;
                    cout << fourbits(ixj) << "\t" << fourbits(i&k) << endl;

                    if (ixj > i){
                        if ((i & k) == 0 && (*data)[i] > (*data)[ixj])
                            std::swap((*data)[i], (*data)[ixj]);
                        if ((i & k) != 0 && ((*data)[i] < (*data)[ixj]))
                            std::swap((*data)[i], (*data)[ixj]);
                    }
                }
            }
        }
    }

    static string fourbits(int n){
        string ret = /*to_string(n) + */(n > 15 ? "/1" : "/");
        for( int bit = 3; bit >= 0; bit--)
            ret += (n & 1 << bit) ? "1":"0";
        return ret;
    }

private:
    Container *data;
    int n;
};

void fillRandom( Data &v, int lo, int hi){
    uniform_int_distribution<int> dist(lo, hi);
    random_device rd;
    mt19937 source(rd());
    for(int i = v.size()-1; i >= 0; i--){
        v[i] = dist(source);
    }
}

int main(){
    Data data(N, 0);
    fillRandom(data, 0, N);
    auto start1 = chrono::steady_clock::now();
    sort(data.begin(), data.end());
    auto end1 = chrono::steady_clock::now();
    auto elapse1 = chrono::duration<double, milli>(end1- start1).count();
    cout << "default sort in " << elapse1 << "ms" << endl;

    fillRandom(data, 0, N);
    start1 = chrono::steady_clock::now();

    Bitonic<Data> bitonic(&data);
    bitonic.sort();

    end1 = chrono::steady_clock::now();
    auto elapse2 = chrono::duration<double, milli>(end1-start1).count();

    int check = -1;
    for (int elem: data){
        if(elem < check){
            cout << "Failed result at "<< check << endl;
            break;
        }
        check = elem;
    }
    cout << "default compute in " << elapse1 << " ms" << endl;
    cout << "parallel compute in " << elapse2 << " ms" << endl;

    return 0;
}