
public class HW4{
    
    final int NUM_THREADS = 4;
    final int ARR_SIZE = 1 <<22;
    
    public static void main(String[] args){
    
        int[]arr = getRandomArr(ARR_SIZE);
        CylicBarrier cb = new CylicBarrier(NUM_THREADS);
        Thread[] threads = new Thread[NUM_THREADS];
        for(int i =0; i < NUM_THREADS; i++){
            threads[i] = new Thread(new Bitonic())
        }
        
    }
    
    public int[]getRandomArr(int size){
        Randdom rand = new Random();
        int[]arr = new int[size];
        
        for(int i =0; i < size; i++){
            arr[i] = rand.nextInt();
        }
        return arr;
    }
    
    

}
