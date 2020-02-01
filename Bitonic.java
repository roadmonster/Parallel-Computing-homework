/*
 *@author Hao Li
 *Bitonic class for each thread to use 
 *Each thread only does their allocated share of work and use the CyclicBarrier to sychronize between threads.
 */

import java.lang.*;
import java.io.*; 
import java.util.Random; 
import java.util.*;
import java.util.concurrent.*;


public class Bitonic implements Runnable{
     int[] data;
     int start;
     int end;
     CyclicBarrier cb = null;
     int id;
     
     /**
      * Constructor
      */
     public Bitonic(int[]data, int id, int num_threads, CyclicBarrier cb){
         this.data = data;
         this.id = id;
         this.start = id*(data.length / num_threads);
         this.end = id != num_threads - 1 ? (id + 1) * (data.length / num_threads) : data.length;
         this.cb = cb;
     }
     
     /*
      * sort class doing the bitonic sort on the given chunk of data
      * Each column will have a barrier to synchronize the threads
      */

     
     public void run(){
        try{
            for(int k =2; k <= data.length; k*=2){

                for (int j = k / 2; j > 0; j /= 2){

                    for (int i = start; i < end; i++){
                         
                         int ixj = i ^j;

                            if (ixj > i) {
                                if ((i & k) == 0 && (data[i] > data[ixj])){
                                    int temp = data[i];
                                    data[i] = data[ixj];
                                    data[ixj] = temp;
                                }

                                if ((i & k) != 0 && (data[i] < data[ixj])){
                                    int temp = data[i];
                                    data[i] = data[ixj];
                                    data[ixj] = temp;
                                }
                            }
                    }
                    cb.await();    
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("thread" + this.id + "offline");
        }
            
     }
     
     
 }
