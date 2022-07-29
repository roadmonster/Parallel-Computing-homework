import java.util.ArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

class BitonicPipeline{
    public static final int N = 1 << 22;
    public static int TIME_ALLOWED = 10;
    public static int timeout = 10;

    public static void main(String[] args) {
        final int 
            N_ARRAY_GENS = 4,
            N_STAGE_ONES = N_ARRAY_GENS,
            N_INTERIOR = N_STAGE_ONES - 1,
            N_THREADS = N_STAGE_ONES + N_INTERIOR + N_ARRAY_GENS,
            N_QUEUES = N_THREADS,
            ROOT_QUEUE = 0;
        
            Thread[] threads = new Thread[N_THREADS];
            ArrayList<SynchronousQueue<double[]>>queues = new ArrayList<>();
            for (int i = 0; i < N_QUEUES; i++){
                queues.add(new SynchronousQueue<double[]>());
            }

            for (int i = 1; i <= N_ARRAY_GENS; i++){
                threads[N_THREADS - i] = 
                    new Thread(new RandomArrayGenerator(N / 4, queues.get(N_QUEUES - i)));
            }

            for (int i = 1; i <= N_STAGE_ONES; i++){
                threads[N_THREADS - N_ARRAY_GENS - i] = new Thread(new StageOne(queues.get(N_QUEUES - i), 
                    queues.get(N_QUEUES - N_ARRAY_GENS - i), "one" + i));
            }

            for (int i = 0; i < N_INTERIOR; i++){
                threads[i] = new Thread(
                                new BitonicStage(queues.get(2 * i + 1), queues.get(2 * i + 2), 
                                                queues.get(i), "bit " + i));
            }

            for (int i = 0; i < N_THREADS; i++){
                threads[i].start();
            }

            long start = System.currentTimeMillis();
            int work = 0;
            double[]result = null;

            while (System.currentTimeMillis() < start + TIME_ALLOWED * 1000){
                try {
                    result = queues.get(ROOT_QUEUE).poll(timeout * 1000, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                if( result == null)
                    System.out.println("Pipeline produced null array");
                else if (!RandomArrayGenerator.isSorted(result))
                    System.out.println("Pipeline result not correct");
                else
                    work++;
            }
            for (Thread thread: threads){
                thread.interrupt();
            }
            System.out.println("sorted " + work + " arrays (each: " + result.length 
                                + " doubles in " + TIME_ALLOWED + " seconds");


    }

}