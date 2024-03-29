#include <iostream>
#include <random>
#include "GeneralScan.h"

template<typename NumType>
class MaxScan : public GeneralScan<NumType>{
public:
    MaxScan(const typename GeneralScan<NumType>::RawData *data) : GeneralScan<NumType>(data, 1){
    }
protected:
    virtual NumType init() const{
        return std::numeric_limits<NumType>::min();
    }

    virtual NumType prepare(const NumType &dataum) const{
        return dataum;
    }

    virtual NumType combine(const NumType &left, const NumType &right) const{
        if (left > right)
            return left;
        return right;
    }

    virtual NumType gen(const NumType &tally) const{
        return tally;
    }
};

struct Ten{
    int ten[10];
};

std::ostream &operator<<(std::ostream &out, const Ten &ten){
    out << "[";
    for (int i = 0; i < 9; i++){
        out << ten.ten[i] << ", ";
    }
    out << ten.ten[9] << "]";
    return out;
}

class LowTen: public GeneralScan<int, Ten>{
    public:
        LowTen(const std::vector<int>*data) : GeneralScan<int, Ten>(data){
        }
    protected:
        virtual Ten init() const{
            Ten t;
            for (int i = 0; i < 10; i++){
                t.ten[i] = std::numeric_limits<int>::max();
            }
            return t;
        }

        virtual Ten prepare(const int &datum) const{
            Ten t;
            for (int i = 1; i < 10; i++){
                t.ten[i] = std::numeric_limits<int>::max();
            }
            t.ten[0] = datum;
            return t;
        }

        virtual Ten combine(const Ten&left, const Ten&right) const{
            Ten t;
            int r = 0, l = 0;
            for (int i = 0; i < 10; i++){
                if (left.ten[l] < right.ten[r]){
                    t.ten[i] = left.ten[l++];
                }else
                    t.ten[i] = right.ten[r++];
            }
            return t;
        }

        virtual Ten gen(const Ten &tally) const{
            return tally;
        }
};

class AvgLowTen: public GeneralScan<int, Ten, double>{
public:
    AvgLowTen(const std::vector<int> *data):GeneralScan<int, Ten, double>(data){
    }
protected:
    virtual double gen(const Ten &tally) const{
        double total = 0.0;
        int count = 0;
        for (int elem: tally.ten){
            if (elem != std::numeric_limits<int>::max()){
                total += elem;
                count++;
            }
        }
        return total / count;
    }

    virtual Ten init() const{
        Ten t;
        for (int i = 0; i < 10; i++){
            t.ten[i] = std::numeric_limits<int>::max();
        }
        return t;
    }

    virtual Ten prepare(const int& datum) const{
        Ten t;
        for (int i = 1; i < 10; i++){
            t.ten[i] = std::numeric_limits<int>::max();
        }
        t.ten[0] = datum;
        return t;
    }

    virtual Ten combine(const Ten& left, const Ten&right) const{
        Ten t;
        int r = 0, l = 0;
        for (int i = 0; i < 10; i++){
            if (left.ten[l] < right.ten[r]){
                t.ten[i] = left.ten[l++];
            }else{
                t.ten[i] = right.ten[r++];
            }
        }
        return t;
    }
};

struct Histo{
    static const int N = 10;
    int bucket[N + 2];
    int hi, lo;

    Histo() : hi(100), lo(0){
        for (int i = 0; i < N + 2; i++){
            bucket[i] = 0;
        }
    }
};

std::ostream &operator<<(std::ostream &out, const Histo &histo){
    out << "|";
    for (int count: histo.bucket){
        out << count << "|";
    }
    return out;
}

class HistoScan: public GeneralScan<int, Histo>{
public:
    HistoScan(const std::vector<int> *data): GeneralScan<int, Histo>(data){}
    
protected:
    virtual Histo init() const{
        Histo h;
        return h;
    }

    virtual Histo prepare(const int &dataum) const{
        Histo h;
        int bucket_size = (h.hi - h.lo) / h.N;
        if (dataum < h.lo)
            h.bucket[0]++;
        else if (dataum >= h.hi)
            h.bucket[h.N + 1]++;
        else
            h.bucket[1 + (dataum - h.lo)/ bucket_size]++;
        return h;
    }

    virtual Histo combine(const Histo &left, const Histo &right) const {
        Histo h;
        for (int i = 0; i < h.N + 2; i++){
            h.bucket[i] = left.bucket[i] + right.bucket[i];
        }
        return h;
    }

    virtual Histo gen(const Histo &tally) const {
        return tally;
    }
};

class ExamHeap : public GeneralScan<double>{
public:
    ExamHeap(const std::vector<double> *data) : GeneralScan<double>(data){

    }
protected:
    virtual double init() const{
        return 1.0;
    }

    virtual double prepare(const double &probability_of_miss) const{
        return 1.0 - probability_of_miss;
    }

    virtual double combine(const double &left, const double &right) const{
        return left * right;
    }
    
    virtual double gen(const double &tally) const{
        return tally;
    }
};

class SumHeap : public GeneralScan<int>{
public:
    SumHeap(const std::vector<int> *data) : GeneralScan<int>(data){}
protected:
    virtual int init() const {
        return 0;
    }

    virtual int prepare(int& dataum) const{
        return dataum;
    }

    virtual int combine(const int& left, const int& right) const{
        return left + right;
    }

    virtual int gen(const int& tally){
        return tally;
    }
};

bool testExamHeap(){
    using namespace std;
    const int N = 1 << 3;
    vector<double> data(N, 1e-3);
    vector<double> prefix(N, 1.0);

    auto start = chrono::steady_clock::now();

    ExamHeap heap(&data);
    cout << "exam1: " << heap.getReduction() << endl;
    heap.getScan(&prefix);

    auto end = chrono::steady_clock::now();
    auto elapsed = chrono::duration<double, milli>(end - start).count();

    double check = 1.0;
    for (double elem: prefix) {
        check *= 1.0 - 1e-3;
        if (abs(elem - check) > 1e-8) {
            cout << "FAILED RESULT at " << check << endl;
            return false;
        }
        // cout << elem << endl;
    }
    cout << "in " << elapsed << "ms" << endl;
    return true;

    
}


bool testSumHeap(){

}
bool test_max_scan(){
    using namespace std;
    const int N = 1 << 27;  // FIXME must be power of 2 for now
    vector<int> data(N, 1);  // put a 1 in each element of the data array
    vector<int> prefix(N, 1);
    data[3000] = 12345;
    data[9000] = 67890;

    auto start = chrono::steady_clock::now();

    MaxScan<int> max_scan(&data);
    cout << "max scan: " << max_scan.getReduction() << endl;
    max_scan.getScan(&prefix);

    // stop timer
    auto end = chrono::steady_clock::now();
    auto elpased = chrono::duration<double, milli>(end - start).count();

    int check = 1;
    for (int i = 0; i < N; i++) {
        int elem = prefix[i];
        if (elem != check) {
            cout << "FAILED RESULT at " << i << ": " << elem << endl << data[i + 1] << endl;
            return false;
        } else if (i == 2999) {
            check = 12345;
        } else if (i == 8999) {
            check = 67890;
        }
    }
    cout << "in " << elpased << "ms" << endl;
    return true;
}

bool test_avg_low_ten(){
    using namespace std;
    const int N = 1 << 10;
    vector<int> data(N);
    for (int i = 0; i < N; i++){
        data[i] = rand() % 100;
    }
    vector<Ten>prefix(N);

    auto start = chrono::steady_clock::now();
    LowTen low_ten(&data);
    cout << "low ten: " << low_ten.getReduction() << endl;
    low_ten.getScan(&prefix);

    auto end = chrono::steady_clock::now();
    auto elapsed = chrono::duration<double, milli>(end - start).count();

    for (int i = 0; i < 100; i++){
        Ten elem = prefix[i];
        cout << "adding in " << data[i] << ": " << elem << endl;
    }
    cout << "in " << elapsed << "ms" << endl;
    return true;
}



int main(){
    using namespace std;
    if (!test_max_scan())
        cout << "test_max_scan failed" << endl;
    if (!test_avg_low_ten())
        cout << "test_low_ten failed" << endl;
}