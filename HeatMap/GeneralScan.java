package HeatMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class GeneralScan<ElemType, TallyType>{
    public static final int DEFAULT_THREAD_THRESHOLD = 10_000;
    public GeneralScan(List<ElemType> raw){
        this(raw, DEFAULT_THREAD_THRESHOLD);
    }

    public GeneralScan(List<ElemType>raw, int thread_threshold){
        reduced = false;
        n = raw.size();
        data = raw;
        height = 0;
        while((1 <<height) < n )
            height++;
        first_data = (1 << height) - 1;
        threshold = thread_threshold;
        last_interior = ROOT;
        while(dataCount(last_interior) > threshold)
            last_interior = left(last_interior);
        last_interior= left(last_interior) - 1;

        interior = new ArrayList<TallyType>(last_interior + 1);
        for (int i = 0; i < last_interior; i++){
            interior.add(init());
        }
        pool = new ForkJoinPool();
    }

    protected TallyType init(){
        throw new IllegalArgumentException("must implement init()");
    }

    protected TallyType prepare(ElemType dataum){
        throw new IllegalArgumentException("must implement prepare()");
    }

    protected TallyType combine(TallyType left, TallyType right){
        throw new IllegalArgumentException("must implement combine()");
    }

    protected TallyType accum(TallyType tally, ElemType dataum){
        throw new IllegalArgumentException("must implement accum");
    }


    protected static final int ROOT = 0;
    protected boolean reduced;
    protected int n;
    protected List<ElemType>data;
    protected List<TallyType>interior;
    protected int height;
    protected int last_interior;
    protected int first_data;
    protected int threshold;
    protected ForkJoinPool pool;

    protected int firstData(int i){
        if (isLeaf(i)){
            return i < first_data ? -1: i;
        }
        return firstData(left(i));
    }

    protected boolean isLeaf(int i){
        return left(i) >= size();
    }

    protected int left(int i){
        return i * 2 + 1;
    }

    protected int right(int i){
        return left(i) + 1;
    }

    protected int size(){
        return first_data + n;
    }

    protected int lastData(int i){
        if (isLeaf(i)){
            return i < first_data ? -1:i;
        }
        if (hasRight(i)){
            int r = lastData(right(i));
            if (r != -1)
                return r;
        }
        return lastData(left(i));
    }

    protected boolean hasRight(int i){
        return right(i) < size();
    }

    protected int dataCount(int i){
        int first = firstData(i);
        if (first == -1){
            return 0;
        }
        int last = lastData(i);
        if(last == -1)
            last = size();
        return last - first;
    }

    protected TallyType value(int i){
        if (i < first_data)
            return interior.get(i);
        else
            return prepare(data.get(i - first_data));
    }

    protected ElemType leafValue(int i){
        if (i < first_data || i >= size()){
            throw new IllegalArgumentException("i has exceeded the threshold");
        }
        return data.get(i - first_data);
    }

    protected void reduce(int i){
        int first = firstData(i), last = lastData(i);
        System.out.println("reduce(" + i + ") from " + first + " to " + last );
        TallyType tally = init();
        if (first != -1){
            for (int j = first; j <= last; j++){
                accum(tally, leafValue(j));
            }
        }
        interior.set(i, tally);
    }

    protected void scan(int i, TallyType tallyPrior, List<TallyType> output){
        int first = firstData(i), last = lastData(i);
        if(first != -1){
            for (int j = first; j <= last; j++){
                tallyPrior = combine(tallyPrior, value(j));
                output.set(j - first_data, tallyPrior);
            }
        }
    }



}