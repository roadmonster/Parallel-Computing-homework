import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

// /**
//  * Class doing the Bitonic sort
//  * Takes 2 input queue and 1 output queue
//  */
public class BitonicStage implements Runnable {
    private static final int timeout = 10;
        public enum UpDown {UP, DOWN}

    private SynchronousQueue<double[]>input1;
    private SynchronousQueue<double[]>input2;
    private SynchronousQueue<double[]>output;
    private double[]data;
    private String name;

    public BitonicStage(SynchronousQueue<double[]>input1, 
                        SynchronousQueue<double[]>input2, 
                        SynchronousQueue<double[]>output,
                        String name) {
        this.input1 = input1;
        this.input2 = input2;
        this.output = output;
        this.name = name;
    }

    public BitonicStage() {
    }
    @Override
    public void run() {
        double[] array1 = new double[1], array2 = new double[1];
        while(this.output != null && array1 != null && array2 != null){
                // try {
                //     // take the input array
                //     array1 = input1.poll(timeout * 1000, TimeUnit.MILLISECONDS);
                //     array2 = input2.poll(timeout * 1000, TimeUnit.MILLISECONDS);
                //     // if input array not null, safe to process and fill the result into the output array
                //     if (array1 != null && array2 != null){
                //         double[] a = process(array1, array2);
                //         output.offer(a, timeout*1000, TimeUnit.MILLISECONDS);
                //     }else{ // input array is null, display error message
                //         System.out.println(getClass().getName() + " " + name + " got null array");
                //     }
                // } catch (InterruptedException e) { // in case of interruption, return
                //     return;
                // }
                try {
                    array1 = input1.poll(timeout * 1000, TimeUnit.MILLISECONDS);
                    array2 = input1.poll(timeout * 1000, TimeUnit.MILLISECONDS);
                    if (array1 != null && array2 != null) {
                        double a[] = process(array1, array2);
                        output.offer(a, timeout * 1000, TimeUnit.MILLISECONDS);
                    } else {
                        System.out.println(getClass().getName() + " " + name + " got null array as input.");
                    }
                } catch (InterruptedException e) {
                    return;
                }
        }
    }

    /**
     * Entry point / Wrapper method for bitonic sort
     * This method resue data array if size is right, or re-create one if size of not correct. 
     * Doing this could save lots of memory since we are doing a stream of arrays, we want to avoid overhead
     * We fill the data array first half ascending order, then 2nd half decending order
     * Thenwe  call the sorting method, we start index, length of current array, and the target direction
     * @param half1
     * @param half2
     * @return
     */
    public double[] process(double[] half1, double[] half2){
        if (this.data == null || this.data.length != 2 * half1.length){
            this.data = new double[half1.length + half2.length];
        }
        int i = 0; 
        for (int j = 0; j < half1.length; j++){
            data[i++] = half1[j];
        }

        for(int j = half2.length - 1; j >= 0; j--){
            data[i++] = half2[j];
        }
        bitonic_sort(0, data.length, UpDown.UP);
        return data;
    }

    private void bitonic_sort(int start, int len, UpDown d){
        if (len > 1){
            int half = len / 2;
            bitonic_merge(start, len, d);
            bitonic_sort(start, half, d);
            bitonic_sort(start + half, half, d);
        }
    }

    private void swap(int i, int j){
        if (i != j){
            double temp = data[i];
            data[i] = data[j];
            data[j] = temp;
        }
    }

    private void bitonic_merge(int start, int len, UpDown d){
        if (len > 1){
            int half = len / 2;
            if (d == UpDown.UP){
                for (int i = start; i < start + half; i++){
                    if(data[i] > data[i + half])
                        swap(i, i + half);
                }
            }else{
                for (int i = start; i < start + half; i++){
                    if(data[i] < data[i + half])
                        swap(i, i + half);
                }
            }
        }
    }
}



