import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class RandomArrayGenerator implements Runnable{
    private static final int timeout = 10;
    private int size;
    private SynchronousQueue<double[]>output;
    public RandomArrayGenerator(int size, SynchronousQueue<double[]>output) {
        this.size = size;
        this.output = output;
    }

    @Override
    public void run() {
        while(true){
            try {
                output.offer(getArray(this.size), timeout * 1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                return;
            }
        }
        
    }

    public static double[] getArray(int len){
        double[] ret = new double[len];
        for(int i = 0; i < len; i++){
            ret[i] = ThreadLocalRandom.current().nextDouble() * 100.0;
        }
        return ret;
    }

    public static boolean isSorted(double[] a){
        if (a == null)
            return false;
        double last = a[0];
        for(int i = 1; i < a.length; i++){
            if (a[i] < last)
                return false;
            last = a[i];
        }
        return true;
    }


}
