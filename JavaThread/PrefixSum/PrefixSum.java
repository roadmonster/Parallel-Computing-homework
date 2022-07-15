import java.util.Arrays;


public class PrefixSum{
    static final int N_THREADS = 8;
    static final int LENGTH = 1_000_000;
    private int[] data;

    public PrefixSum(int[]data){
        this.data = data;
    }
    int encode(int v){
        for(int i = 0; i < 500; i++){
            v = ((v * v) + v) % 10;
        }
        return v;
    }

    int decode(int v){
        return encode(v);
    }

    private class Encoder implements Runnable{
        protected int start, end;
        public Encoder(int start, int end){
            this.start = start;
            this.end = end;
        }
        public void run(){
            for(int i = start; i < end; i++){
                data[i] = encode(data[i]);
            }
        }
    }

    private class Decoder extends Encoder{

        public Decoder(int start, int end) {
            super(start, end);
        }
    }

    private void prefixSumSeq(){
        int encodedSum = 0;
        for(int i = 0; i < LENGTH; i++){
            encodedSum += encode(data[i]);
            data[i] = decode(encodedSum);
        }
    }

    private void prefixSumParallel(){
        Thread[]threads = new Thread[N_THREADS];
        int piece = LENGTH / N_THREADS;
        for(int i = 0; i < N_THREADS; i++){
            int start = i * piece;
            int end = i == N_THREADS-1 ? LENGTH:start + piece;
            threads[i] = new Thread(new Encoder(start, end));
            threads[i].start();
        }

        for(int i = 0; i < N_THREADS; i++){
            try{
                threads[i].join();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }

        int encodedSum = 0; 
        for(int i = 0; i < LENGTH; i++){
            encodedSum += data[i];
            data[i] = encodedSum;
        }

        for(int i = 0; i < N_THREADS; i++){
            int start = i * piece;
            int end = i == N_THREADS-1 ? LENGTH:start + piece;
            threads[i] = new Thread(new Decoder(start, end));
            threads[i].start();
        }

        for(int i = 0; i < N_THREADS; i++){
            try{
                threads[i].join();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            
        }
        
        
    }



    public static void main(String args[]){
        int[] data = new int[LENGTH];
        Arrays.fill(data, 1);
        data[0] = 6;

        long start = System.nanoTime();
        
        new PrefixSum(data).prefixSumSeq();
        
        long end = System.nanoTime();

        System.out.println((end -start) / 1_000_000 + " ms");

        System.out.println("[0]: " + data[0] );
        System.out.println("[Length / 2]: " + data[LENGTH / 2] );
        System.out.println("[0]: " + data[LENGTH-1] );

        System.out.println("----------------");
        System.out.println("Parallel");

        int[] data2 = new int[LENGTH];
        Arrays.fill(data2, 1);
        data2[0] = 6;

        start = System.nanoTime();

        new PrefixSum(data2).prefixSumParallel();

        end = System.nanoTime();

        System.out.println((end -start) / 1_000_000 + " ms");

        System.out.println("[0]: " + data[0] );
        System.out.println("[Length / 2]: " + data[LENGTH / 2] );
        System.out.println("[0]: " + data[LENGTH-1] );


    }
}