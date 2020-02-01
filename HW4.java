/**
 * HW4.java
 * CPSC5600 Seattle University
 * @author Hao Li
 * This is free and unencumbered software released into the public domain.
 *
 */
import java.util.Random;
import java.util.*;
import java.util.concurrent.*;

/**
 * HW4 class 
 * This is the driver class to create threads and calculate
 * the work done within the time slot allowed.
 */
public class HW4{
    
    static final int NUM_THREADS = 16; 
    static final int ARR_SIZE = 1 <<22; //size of the arr to feed
    static final int TIME_OUT = 10;//in seconds (max wait for an output)
    
    public static void main(String[] args){

        long start = System.currentTimeMillis();
        int work = 0;


        while (System.currentTimeMillis() < start + TIME_OUT * 1000) {

    
            int[]arr = HW4.getRandomArr(ARR_SIZE);
            CyclicBarrier cb = new CyclicBarrier(NUM_THREADS);
            Thread[] threads = new Thread[NUM_THREADS];
            for(int i =0; i < NUM_THREADS; i++){

                threads[i] = new Thread(new Bitonic(arr, i, NUM_THREADS, cb));
                threads[i].start();
            }

           
  				try{
                for(int j = 0; j < NUM_THREADS; j++){
                    threads[j].join();
                }
            }catch(InterruptedException e){
                e.printStackTrace();
                System.exit(1);
            }
	
			
            work++;
           	System.out.println(work);
        }

		  System.out.println("Sorted" + work +" arrays (each " + ARR_SIZE + "integers" 
				+ "in " + TIME_OUT + "seconds");

        
    }
    /**
     * Helper class to generate random array
     */
    public static int[]getRandomArr(int size){
        Random rand = new Random();
        int[]arr = new int[size];
        
        for(int i =0; i < size; i++){
            arr[i] = rand.nextInt(101);
        }
        return arr;
    }
    
    

}
