package HeatMap;
/**
 * Hao Li
 * This is a free software released to public domain
 */
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
/**
 * Implementation of the GeneralScan concept of schwatz tight threads
 */
public class GeneralScan<ElemType, TallyType>{
    public static final int DEFAULT_THREAD_THRESHOLD = 10_000;
    public GeneralScan(List<ElemType> raw){
        this(raw, DEFAULT_THREAD_THRESHOLD);
    }

    /**
     * Ctor did the following
     *  1. compute the height of the heap
     *  2. compute the first data which is the first item in the leaf lv
     *  3. compute the last interior by recursively going to left and compute the data count starting
     *     from that node to the end, judging if the amount is equal to the threshold for each thread
     *     once reached that threshold, go further down one lv and retract to the previous node, which 
     *     is the last interior, right before the first data.
     *  4. Last interior is an integer indicating the amount of the interior nodes
     *  5. we create a list of tally type with the size of the interior nodes, and init each tally node item
     *  6. Lastly, we create a default pool
     * @param raw
     * @param thread_threshold
     */
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

    /**
     * Initialize the TallyType object, to be overriden by subclasses
     * @return
     */
    protected TallyType init(){
        throw new IllegalArgumentException("must implement init()");
    }

    /**
     * Convert the ElemType into TallyType
     * @param dataum
     * @return TallyType 
     */
    protected TallyType prepare(ElemType dataum){
        throw new IllegalArgumentException("must implement prepare()");
    }

    /**
     * Compute the sum of left and right node
     * @param left TallyType of the left subtree
     * @param right TallyType of the right subtree
     * @return TallyType for the combination
     */
    protected TallyType combine(TallyType left, TallyType right){
        throw new IllegalArgumentException("must implement combine()");
    }

    /**
     * Compute the scan for each thread by accumulating up from the left
     * @param tally Sum object to get all the data item
     * @param dataum data item from which to start accumulation
     * @return TallyType of the accumulation
     */
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

    /**
     * Recursively going left of the heap to find the first data for this given interior node
     * By saying first data it is not the first data of the leaf level.
     * It is the first data for the thread assigned job.
     * @param i node num from which we calculate the first
     * @return first data of the thread which is responsible for this given interior node
     */
    protected int firstData(int i){
        if (isLeaf(i)){
            return i < first_data ? -1: i;
        }
        return firstData(left(i));
    }

    /**
     * compute the last data for the thread assigned job
     * @param i
     * @return
     */
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

    /**
     * Compute the amount of data for this thread that has interior starting from i
     * @param i
     * @return
     */
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

    /**
     * compute the tally type value of of the current node
     * @param i
     * @return
     */
    protected TallyType value(int i){
        if (i < first_data)
            return interior.get(i);
        else
            return prepare(data.get(i - first_data));
    }

    /**
     * Given the index of the node and compute its data, throw exception in case of given interior node
     * @param i
     * @return
     */
    protected ElemType leafValue(int i){
        if (i < first_data || i >= size()){
            throw new IllegalArgumentException("i has exceeded the threshold");
        }
        return data.get(i - first_data);
    }

    /**
     * Compute the first data and last data, then accumulate from the first to last
     * finally set the interior the value of the accumulation
     * @param i node from which we need to do reduction
     */
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

    /**
     * We do the scan which is the summation of the prior tally to current interior node's value
     * Then we set the value for each element in the output list.
     * In short this shall compute the prefix sum for each element of the data
     * @param i
     * @param tallyPrior
     * @param output
     */
    protected void scan(int i, TallyType tallyPrior, List<TallyType> output){
        int first = firstData(i), last = lastData(i);
        if(first != -1){
            for (int j = first; j <= last; j++){
                tallyPrior = combine(tallyPrior, value(j));
                output.set(j - first_data, tallyPrior);
            }
        }
    }

    /**
     * Wrapper method to invoke the starting thread of ComputeReduction, which shall spawn sub threads
     * to do the scan. We check if the reduction has been done, if no start the thread to reduction.
     * We create an empty output list and start the scan thread.
     * @return
     */
    protected List<TallyType> getScan(){
        if (!reduced){
            getReduction();
        }
        List<TallyType>output = new ArrayList<>();
        for(int i = 0; i < data.size(); i++){
            output.add(init());
        }
        pool.invoke(new ComputeScan(ROOT, init(), output));
        return output;
    }

    /**
     * Wrapper method to init the reduction thread and update the reduced boolean value
     * @return the tally type value of the ROOT
     */
    public TallyType getReduction(){
        if(!reduced){
            pool.invoke(new ComputeReduction(ROOT));
            reduced = true;
        }
        return value(ROOT);
    }

    /**
     * Tool class to be invoked by invoked by the main thread, and this thread shall
     * spawn child thred to do the recursive scan, main thread shall pass ROOT as value of i
     * then we recurse down to left and right until the data count is less than threshold
     * then we do the reduce (accumalation) logic,
     * then we set the current node's value to the combination of the left tally and right tally
     * and assign the value to the node for upper level to use
     */
    class ComputeReduction extends RecursiveAction{
        private int i;

        public ComputeReduction(int i){
            this.i = i;
        }

        @Override
        protected void compute() {
            if(dataCount(i) <= threshold){
                reduce(i);
                return;
            }
            invokeAll(new ComputeReduction(left(i)), 
                      new ComputeReduction(right(i)));
            interior.set(i, combine(value(left(i)), value(right(i))));
        }
    }

    /**
     * Intermediate class to be invoked by the main thread
     * will spawn child thread to do the reduce and add the value to the prvious sum
     * and then assign the the value to the previous sum (tally prior) for the upper level 
     * to compute.
     * The catch is the the right subtree, will need the combination of the previous sum
     * and the value of the left node(we already computed within the scan of lower level)
     */
    class ComputeScan extends RecursiveAction{
        private int i;
        private TallyType tallyPrior;
        private List<TallyType> output;

        public ComputeScan(int i, TallyType tallyPrior, List<TallyType> output){
            this.i = i;
            this.tallyPrior = tallyPrior;
            this.output = output;
        }

        @Override
        protected void compute() {
            if (dataCount(i) <= threshold){
                scan(i, tallyPrior, output);
                return;
            }
            invokeAll(new ComputeScan(left(i), tallyPrior, output), 
                      new ComputeScan(right(i), combine( tallyPrior, value(left(i))), output));
        }
    }
}