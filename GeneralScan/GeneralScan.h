template<typename ElemType, typename TallyType=ElemType, typename ResultType=TallyType>
class GeneralScan{
public:
    /**
     * @class RawData - vector of ElemType, how the raw data package for the ctor
     */
    typedef std::vector<ElemType> RawData;

    /**
     * @class TallyData - a vector of TallyType, used for the interior nodes of the reduction
     */
    typedef std::vector<TallyType> TallyData;

    /**
     * @class ScanData - a vector of ResultType, used for scan output
     */
    typedef std::vector<ResultType> ScanData; 

    static const int ROOT = 0;

    static const int N_THREADS = 16;

    GeneralScan(const RawData *raw, int n_threads = N_THREADS): reduced(false), n(raw->size()), data(raw),
                                                                height(ceil(log2(n))), n_threads(n_threads){
        if (1 << height != n)
            throw std::invalid_argument("data size must be power of 2 for now");
        interior = new TallyData(n-1);
    }

    virtual ~GeneralScan(){
        delete interior;
    }

    ResultType getReduction(int i = ROOT){
        //TODO
    }

    void getScan(ScanData *output){
        //TODO
    }

protected:
    virtual TallyType init() const = 0;

    virtual TallyType prepare(const ElemType &datum) const = 0;

    virtual TallyType combine(const TallyType &left, const TallyType &right) const = 0;

    virtual ResultType gen(const TallyType &tally) const = 0;

private:
    bool reduced;
    int n;
    const RawData *data;
    TallyData *interior;
    int height;
    int n_threads;

    TallyType value(int i){
        if (i < n - 1){
            return interior->at(i);
        }else{
            return prepare(data->at(i - (n -1)));
        }
    }

    bool reduce(int i){
        if (!isLeaf(i)){
            if ( i < n_threads - 1){
                auto handle = std::async(std::launch::async, &GeneralScan::reduce, this, left(i));
                reduce(right(i));
                handle.wait();
            }else{
                reduce(left(i));
                reduce(right(i));
            }
            interior->at(i) = combine(value(left(i)), value(right(i));
        }
        return true;
    }

    void scan(int i, TallyType tallyPrior, ScanData *output){
        if(isLeaf(i)){
            output->at(i - (n-1)) = gen(combine(tallyPrior, value(i)));
        }else{
            if (i < n_thread -1){
                auto handle = std::async(stdd::async, &GeneralScan::scan, this, left(i), tallyPrior, output);
                scan(right(i), combine(tallyPrior, value(left(i)), output));
                handle.wait();
            }
            else{
                scan(left(i), tallyPrior, output);
                scan(right(i), combine(tallyPrior, value(left(i))), output);
            }
        }
    }

    int size(){
        return n - 1 + n;
    }
        
    int parent(int i){
        return (i - 1) / 2;
    } 

    int left(int i){
        return i * 2 + 1;
    }

    int right(int i){
        return left(i) + 1;
    }
    bool isLeaf(int i){
        return left(i) >= size();
    }

};

