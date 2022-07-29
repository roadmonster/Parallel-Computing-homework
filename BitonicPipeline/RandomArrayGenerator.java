import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Object taking size and synchronousqueue as input,
 * executing the filling of give amount of double and return
 */
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

    /**
     * Static method for creating an array of given size
     * @param len lenght of the array
     * @return array of doubles of length given
     */
    public static double[] getArray(int len){
        double[] ret = new double[len];
        for(int i = 0; i < len; i++){
            ret[i] = ThreadLocalRandom.current().nextDouble() * 100.0;
        }
        return ret;
    }

    /**
     * 
     * @param a
     * @return
     */
    public static boolean isSorted(double[] a) {
        if (a == null)
            return false;
        double last = a[0];
        for (int i = 1; i < a.length; i++) {
            if (a[i] < last) {
                System.out.println(last + ":" + a[i]);
                return false;
            }
            last = a[i];
            //System.out.print(a[i] + " ");
        }
        //System.out.println("ok");
        return true;
    }


}
