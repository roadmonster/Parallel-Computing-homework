import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;

public class BitonicThread implements Runnable{

    public static void main(String[] args) throws InterruptedException {
        final int TIME_ALLOWED = 10;
        final int N = 1 << 22;
        final int P = 16;
        final int GRANULARITY = 4;

        long start = System.currentTimeMillis();
        int work = 0;
        while(System.currentTimeMillis() < start + TIME_ALLOWED * 1000){
            double[] data = randomArray(N);
            process(data, P, GRANULARITY);
            if(!isSorted(data)){
                System.out.println("failed");
                break;
            }
            work++;
        }
        System.out.println("T = " + TIME_ALLOWED + " seconds");
        System.out.println("N = "+ N);
        System.out.println("P = " + P);
        System.out.println("Granularity " + GRANULARITY);
        System.out.println("Total " + work + "Arrays sorted");

    }
    @Override
    public void run() {
        for(int k = 2; k <= array.length; k *= 2){
            for(int j = k / 2; j > 0; j /= 2){
                awaitBarrier(j);
                // once every node within this barrier reached
                for( int i = start; i < end; i++){
                    int ixj = i ^ j;
                    // only care about those greater than current index
                    if (i < ixj){
                        // k is alway even, hence & operation will get 0 or 1111...
                        // hence if result is 0, we regard this as going up
                        if ((i & k) == 0)
                            compareAndSwapUp(i, ixj);
                        else   
                            compareAndSwapDown(i, ixj);
                    }
                }
            }
        }
    }

    private void compareAndSwapDown(int a, int b){
        if(array[a] < array[b]){
            double temp = array[a];
            array[a] = array[b];
            array[b] = temp;
        }
    }
    private void compareAndSwapUp(int a, int b){
        if(array[a] > array[b]){
            double temp = array[a];
            array[a] = array[b];
            array[b] = temp;
        }
    }


    /**
     * Figure out which barrier to use and wait for it
     * @param j 
     */
    public void awaitBarrier(int j){
        int jsweep = j * 2;
        int size = end - start;
        int n = array.length;
        if(jsweep < size)
            return;
        
        // start i's value from 1, i works the first node of each level
        // because the very fist barrier start at index 1 in the barrier array
        int width = n/2, i = 1;
        // loop until either width is less or equal current span or i is greater or equal number of levels
        // this is to find the immediate next width that covers current jweep size
        while(width > jsweep && i < barrierHeap.length){
            width /= 2;
            i = i * 2 + 1;
        }
        // then go back a level which is the last lv
        width *= 2;
        i /= 2;

        // decide which barrier to use by compute the offset and 
        int offset = start / width;
        // find the node
        int node = i + offset;

        try {
            barrierHeap[node].await();
        } catch (Exception e) {
            System.out.println( threadIdx + " broken due to: " + e);
        }


    }

    public static void process(double[]data, int p, int g) throws InterruptedException{
        // structure to hold cyclicbarrier
        // number of cyclic barriers is equal to the level of this heap, 
        // each level share have two times barriers than prev lv
        // in which case we need to choose barrier to use
        CyclicBarrier[] barriers = new CyclicBarrier[(1 << g) - 1];
        
        // We start with 
        int threadPerBarrier = p;
        // we use i to hold the number of first node for each level
        int i = 1;
        // start from node 0 and start creating barriers from lv 1, since on lv 0, each thread shall have no overlap
        for(int node = 0; node < barriers.length; node++){
            // when we reach the start of the next level, we make i the head of next level
            // we divide the thread by 2, because on each level of sweep, we mutiply the sweep by 2
            // hence the number of threads are actually merged
            if(node == i){
                i = i * 2 + 1;
                threadPerBarrier /= 2;
            }
            // The first barrier start at index 1
            // we put the cyclic barrier with current thread per barrier number of latches
            barriers[node] = new CyclicBarrier(threadPerBarrier);
        }
        // create a list of threads and initialze each thread with BitonicThread with barrier as parameter
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < p; t++){
            Thread thread = new Thread(new BitonicThread(data, t, p, barriers));
            threads.add(thread);
            thread.start();
        }

        for (Thread t: threads){
            t.join();
        }
    }

    public BitonicThread(double[]array, int currThreadIndex, int threadNum, CyclicBarrier[]barriers) {
        this.array = array;
        int size = array.length / threadNum;
        this.start = currThreadIndex * size;
        this.end = this.start + size;
        this.barrierHeap = barriers;
        this.threadIdx = currThreadIndex;
    }

    private int threadIdx, start, end;
    private double[]array;
    private CyclicBarrier[]barrierHeap;
    private static Random rand = new Random();

    private static double[] randomArray(int n){
        double ret[] = new double[n];
        for(int i = 0; i < n; i++){
            ret[i] = rand.nextDouble() * 100.0;
        }
        return ret;
    }

    private static boolean isSorted(double[]a){
        if (a == null) return false;
        double prev = a[0];
        for(int i = 1; i < a.length; i++){
            if(a[i] < prev)
                return false;
            prev = a[i];
        }
        return true;
    }
    
}