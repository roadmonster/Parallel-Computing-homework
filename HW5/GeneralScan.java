/**
 * Abstract class GeneralScan
 * Author: Hao Li
 * This class provide basic interface for other classes to implement
 */

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.lang.Math;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

/**
 * Generalized reducing/scanning class with methods for preparing the data elements into
 * TallyType objects, combining two TallyType objects, and initializing the beginning TallyType
 * object. These methods can be overridden by subclasses to do any kind of operation for the
 * reduce/scan.
 * @param <ElemType> This is the data type of the read-only data elements.
 * @param <TallyType> This is the combination-result data type. This type must have a 0-arg ctor. Defaults to ElemType.
 * @param <ResultType> This is the final result data type. Any final tally will be converted to this
 *                      data type (using the gen(tally) method). Defaults to TallyType.
 */
abstract class GeneralScan<ElemType, TallyType, ResultType>
{

    static int ROOT = 0;
    final static int N_THREADS = 16;

    private boolean reduced; //flag showing if reduce has been done
    private int n; //size of the input arraylist
    private ArrayList<ElemType> data;
    private ArrayList<TallyType> reduceData; //interior tallies
    private int height; //height of the tree
    private int n_threads;
    private ForkJoinPool pool; //forkjoinpool
    private ArrayList<ResultType>scanData; //output tallies


    /**
     * Ctor
     * @param raw
     */
    GeneralScan(ArrayList<ElemType> raw) {
        scanData = new ArrayList<>();
        n = raw.size();
        reduced = false;
        this.n_threads = N_THREADS;
        data = raw;
        height = (int) Math.ceil(Math.log(n) / Math.log(2));

        if (1 << height != n)
            throw new IllegalArgumentException("data size must be power of 2 for now!");
        if (n_threads >= n)
            throw new IllegalArgumentException("must be more data than threads!");

        reduceData = new ArrayList<>(n_threads * 2);
        for (int i = 0; i < n_threads * 2; i++) {
            reduceData.add(init());
        }


        for (int i = 0; i < n; i++) {
            scanData.add(null);
        }

        pool = new ForkJoinPool(n_threads);
    }

    /**
     * Return the value of node of current index
     * @param i index
     * @return TallyType object
     */
    TallyType value(int i) {
        if (i < n - 1)
            return reduceData.get(i);
        else
            return prepare(data.get(i - (n-1)));
    }

    /**
     * Wrapper class find the reduce result of given index
     * @param i index
     * @return ResultType object
     */
    ResultType getReduction(int i){
        //System.out.println(size());
        if(i >= size())
            throw new IllegalArgumentException("non-existent node");

        reduced = reduced || reduce(ROOT);
        return gen(value(i));
    }

    /**
     * Wraper class
     * @param output ArrayList to hold the output of Scan calculation
     */
    void getScan(ArrayList<ResultType> output){
        if(!reduced)
            reduce(ROOT);
        scan(ROOT, init(), output);

    }

    /**
     * initialize for each new TallyType to be overriden by child classes
     * @return a blank TallyType object for later use
     */
    abstract TallyType init();

    /**
     * Prepare the leaf node before incorporating into the tree
     * @param dataum
     * @return a new TallyType object with data item in it.
     */
    abstract TallyType prepare(ElemType dataum);

    /**
     *Combine two tallies.
     * @param left left tally
     * @param right right tally
     * @return a new tally which is the combination of left and right
     */
    abstract TallyType combine(TallyType left, TallyType right);

    abstract ResultType gen(TallyType tally);

    /**
     * Accumulate the value of the right node into the accumulator and return it.
     * @param accumulator
     * @param right
     * @return the accumulated tally
     */
    TallyType accum(TallyType accumulator, TallyType right){
        return combine(accumulator, right);
    }

    /**
     * Helper method for getReducation, doing the job of calling threads
     * @param i index of the node to compute the reduce
     * @return true if done
     */
    private boolean reduce(int i){


        pool.invoke(new Reduce(i));


        return true;
    }

    /**
     * Helper method for getScan, calls thread to do the job
     * @param i index of the node to getScan result
     * @param tallyPrior previous values
     * @param output output ArrayList
     */
    private void scan(int i, TallyType tallyPrior, ArrayList<ResultType> output){
        pool.invoke(new Scan(i, tallyPrior));
    }

    /**
     * Compute the size of interior plus leaf
     * @return total size
     */
    int size() {
        return (n - 1) + n;
    }

    /**
     * Get the parent index
     * @param i index
     * @return index of the parent of given index
     */
    int parent(int i) {
        return (i - 1) / 2;
    }

    /**
     * Get the left node index
     * @param i current index
     * @return current index's leftnode index
     */
    int left(int i) {
        return i * 2 + 1;
    }

    /**
     * Get the right node index
     * @param i current index
     * @return current index's rightnode index
     */
    int right(int i) {
        return left(i) + 1;
    }

    /**
     * check if current node is leaf
     * @param i current idnex
     * @return true if it is a leaf
     */
    boolean isLeaf(int i) {
        return left(i) >= size();
    }

    /**
     * Get the leftmost child of current index
     * @param i current index
     * @return leftmost child index
     */
    int leftmost(int i) {
        while (!isLeaf(i))
            i = left(i);
        return i;
    }

    /**
     * Get the leftmost child of current index
     * @param i current index
     * @return rightmost child index
     */
    int rightmost(int i) {
        while (!isLeaf(i))
            i = right(i);
        return i;
    }


    /**
     * Inner class extends recursive action could be invoked by forkjointask doing the reduce job
     */
    class Reduce extends RecursiveAction{

        private int i;

        /**
         * Ctor
         * @param index
         */
        public Reduce(int index) { System.out.println("reached ctor");this.i = index;}

        @Override
        protected void compute() {
            System.out.println("come to reduce compute");
            TallyType retTally;

            if (i < n_threads - 1) {
                System.out.println("going to invoke");
                ForkJoinTask.invokeAll(new Reduce(right(i)), new Reduce(left(i)));
                //reduce(left(i));
                retTally = combine(value(right(i)), value(left(i)));
                reduceData.set(i,retTally);

            } else {
                TallyType tally = init();
                int rm = rightmost(i);
                for (int j = leftmost(i); j <= rm; j++)
                    tally = accum(tally, value(j));
                reduceData.set(i, tally);
            }
        }
    }

    /**
     * Inner class extends recursive action could be invoked by forkjointask doing the scan job
     */
    class Scan extends RecursiveAction{
        private int i;
        private TallyType tallyPrior;

        /**
         * Ctor
         * @param index
         * @param tallyPrior
         */
        public Scan(int index, TallyType tallyPrior){
            this.i = index;
            this.tallyPrior = tallyPrior;
        }
        @Override
        protected void compute() {
            if (i < n_threads - 1) {
                ForkJoinTask.invokeAll(new Scan(left(i), tallyPrior),
                        new Scan(right(i), combine(tallyPrior, value(left(i)) ) ) );

            }
            else
            {
                TallyType tally = tallyPrior;
                int rm = rightmost(i);
                for (int j = leftmost(i); j <= rm; j++) {
                    tally = accum(tally, value(j));
                    scanData.set((j - (n - 1)), gen(tally));
                }
            }
        }
    }


}










