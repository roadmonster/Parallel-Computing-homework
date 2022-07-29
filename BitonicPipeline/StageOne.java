import java.util.Arrays;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/*
 * Stage one take input from random generator array, which filled in the synchrounous queue
 * Sorting the input in ascending order and fill the output queue
 */
public class StageOne implements Runnable{

    private static final int timeout = 10;

    public static void process(double[] data){
        Arrays.sort(data);
    }

    private SynchronousQueue<double[]> input;
    private SynchronousQueue<double[]> output;
    private String name;

    public StageOne(SynchronousQueue<double[]>input, SynchronousQueue<double[]>output, String name) {
        this.input = input;
        this.output = output;
        this.name = name;
    }

    @Override
    public void run() {
        double[] array = new double[1];
        while (array != null){
            try {
                // wait for the input to be filled
                array = input.poll( timeout* 1000, TimeUnit.MILLISECONDS);
                if(array != null){
                    process(array);
                    output.offer(array, timeout * 1000, TimeUnit.MILLISECONDS);
                }else{
                    System.out.println(getClass().getName() + " " + name + " got null array");
                }
            } catch (Exception e) {
                return;
            }
        }
        
    }
    
}
