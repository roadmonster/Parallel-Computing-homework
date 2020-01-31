/*
 *@author Hao Li
 *Bitonic class for each thread to use 
 *Each thread only does their allocated share of work and use the CyclicBarrier to sychronize between threads.
 */

public class Bitonic implements Runnable{
     private int[] data;
     private int size;
     private int start;
     private int end;
     private CylicBarrier cb;
     private int id;
     
     
     public Bitonic(int[]data, int size, int thread_ID, int start, int end, CylicBarrier cb){
         this.data = data;
         this.size = size;
         this.id = thread_ID;
         this.start = start;
         this.end = end;
         this.cb = cb;
     }
     
     public void sort(){
         
         for (int j = k / 2; j > 0; j /= 2){
             for (int i = 0; i < size; i++){
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
         }
     }
     
     public void run(){
        try{
            for(int k =2; k < n; k*=2){
            sort();
            cb.await();
        }    
            
        }catch(InterruptedException e){
            System.out.println("thread" + this.id + "offline");
        }
            
     }
     
     
 }
