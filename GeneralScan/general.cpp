#include <iostream>
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

int main(){
    using namespace std;
    if (!test_max_scan())
        cout << "test_max_scan failed" << endl;
}