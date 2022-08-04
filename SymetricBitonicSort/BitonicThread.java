import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class BitonicThread implements Runnable{

    public static void main(String[] args) {
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
        // TODO Auto-generated method stub
        
    }

    public static void process(double[]data, int p, int g) throws InterruptedException{
        // structure to hold cyclicbarrier
        CyclicBarrier[] barriers = new CyclicBarrier[p];
        int n = data.length;
        int threadPerBarrier = p;
        int i = 1;
        int width = n;
        for(int node = 0; node < barriers.length; node++){
            if(node == i){
                width /= 2;
                i = i * 2 + 1;
                threadPerBarrier /= 2;
            }
            barriers[node] = new CyclicBarrier(threadPerBarrier);
        }

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

    public BitonicThread(double[]array, int t, int threadNum, CyclicBarrier[]barriers) {
        
    }
    
}