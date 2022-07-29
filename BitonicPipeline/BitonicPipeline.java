/**
 * Free software released to public domain
 * Seattle University
 * Hao Li
 * 2022/07/29
 */
import java.util.ArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Driver class or main thread to start the thread pipeline,
 * when the children thread completes, main thread shall check if result correct.
 */
class BitonicPipeline{
    
    public static final int N = 1 << 22; // length of each array
    public static int TIME_ALLOWED = 10; // total amount of seconds allowed for the program to run
    public static int timeout = 10; // time out for the synchronous queue

    public static void main(String[] args) {
        final int 
            N_ARRAY_GENS = 4, // number of threads to do array generation
            N_STAGE_ONES = N_ARRAY_GENS, // number of stage one thread to sort the array to stage one
            N_INTERIOR = N_STAGE_ONES - 1, // interior-stage threads for sorting
            N_THREADS = N_STAGE_ONES + N_INTERIOR + N_ARRAY_GENS, // total number of threads
            N_QUEUES = N_THREADS, // total number of synchronous queue needed
            ROOT_QUEUE = 0; // root queue to store the final result
        
            /*
             * create thread array and queue array, and initialize queue array
             */
            Thread[] threads = new Thread[N_THREADS]; 
            ArrayList<SynchronousQueue<double[]>>queues = new ArrayList<>();
            for (int i = 0; i < N_QUEUES; i++){
                queues.add(new SynchronousQueue<double[]>());
            }

            /*
             * assign thread #10, #9, #8, #7, each thread 1/4 of the data length to generate
             * give each thread queue number same as their thread num
             * Each thread will fill their result into their queue for higher level threads to pick up
             * Could be regarded as a leaf
             */
            for (int i = 1; i <= N_ARRAY_GENS; i++){
                threads[N_THREADS - i] = 
                    new Thread(new RandomArrayGenerator(N / 4, queues.get(N_QUEUES - i)));
            }

            /**
             * intialize StageOne threads
             * each array generating thread is hooked up to the StageOne thread
             * Still we are at the leaf level of the tree since we have not started the combing/merging process
             * hence #10 -> #6, #9 -> #5, #8 -> #4, #7 -> #3, left as input, right as output
             */
            for (int i = 1; i <= N_STAGE_ONES; i++){
                threads[N_THREADS - N_ARRAY_GENS - i] = new Thread(new StageOne(queues.get(N_QUEUES - i), 
                    queues.get(N_QUEUES - N_ARRAY_GENS - i), "one" + i));
            }
            /**
             * initialize the bitonic-stage threads
             * This can be seen as a merging thread result and execute the bitonic sort logic
             * We start from thread 0 (root thread), to 1 (left child), 2(right child)
             * thread 1 is parenting 2 child thread (stage one thread), 2 * 1 + 1 = 3, and 4
             * thread 2 is parenting 5 and 6
             * after thread 1 and 2 finished, thread 0 and finally execute the bitonic logic
             */
            for (int i = 0; i < N_INTERIOR; i++){
                threads[i] = new Thread(
                                new BitonicStage(queues.get(2 * i + 1), queues.get(2 * i + 2), 
                                                queues.get(i), "bit " + i));
            }
            // initialize the threads
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
                if( result == null) // print error message
                    System.out.println("Pipeline produced null array");
                else if (!RandomArrayGenerator.isSorted(result)) // check if result is sorted
                    System.out.println("Pipeline result not correct");
                else // increment the work number
                    work++;
            }
            for (Thread thread: threads){
                thread.interrupt();
            }
            System.out.println("sorted " + work + " arrays (each: " + result.length 
                                + " doubles in " + TIME_ALLOWED + " seconds");


    }

}