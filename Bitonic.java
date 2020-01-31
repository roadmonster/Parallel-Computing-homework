/*
 *@author Hao Li
 *Bitonic class for each thread to use 
 *Each thread only does their allocated share of work and use the CyclicBarrier to sychronize between threads.
 */

public class Bitonic implements Runnable{
     private int[] data;
     private int start;
     private int end;
     private CylicBarrier cb;
     private int id;
     
     /**
      * Constructor
      */
     public Bitonic(int[]data, int id, int num_threads, CylicBarrier cb){
         this.data = data;
         this.id = id;
         this.start = id*(data.length / num_threads);
         this.end = id != N_THREADS-1 ? (id + 1) * piece : data.length;
         this.cb = cb;
     }
     
     /*
      * 
      */
     public void sort(){
        for(int k =2; k < data.length; k*=2){

            for (int j = k / 2; j > 0; j /= 2){

                for (int i = 0; i < this.data.length; i++){
                     
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
                this.cb.await();
            }
        }
     }
     
     public void run(){
        try{
            sort();
        }catch(InterruptedException e){
            e.printStackTrace();
            System.out.println("thread" + this.id + "offline");
        }
            
     }
     
     
 }
