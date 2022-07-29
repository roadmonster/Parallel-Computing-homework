import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class BitonicStage implements Runnable {
    private static final int timeout = 10;

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

    public enum Direction {UP, DOWN}

    @Override
    public void run() {
        double[] array1 = new double[1], array2 = new double[1];
        while(this.output != null && array1 != null && array2 != null){
                try {
                    array1 = input1.poll(timeout * 1000, TimeUnit.MILLISECONDS);
                    array2 = input2.poll(timeout * 1000, TimeUnit.MILLISECONDS);

                    if (array1 != null && array2 != null){
                        double[] a = process(array1, array2);
                        output.offer(a, timeout*1000, TimeUnit.MILLISECONDS);
                    }else{
                        System.out.println(getClass().getName() + " " + name + " got null array");
                    }
                } catch (InterruptedException e) {
                    return;
                }
        }
    }
    public double[] process(double[] half1, double[] half2){
        if (this.data == null || this.data.length != 2 * half1.length){
            this.data = new double[half1.length * 2];
        }
        int i = 0; 
        for (int j = 0; j < half1.length; j++){
            data[i++] = half1[j];
        }

        for(int j = half1.length - 1; j >= 0; j--){
            data[i++] = half2[j];
        }
        bitonic_sort(0, data.length, Direction.UP);
        return data;
    }

    private void bitonic_sort(int start, int len, Direction d){
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

    private void bitonic_merge(int start, int len, Direction d){
        if (len > 1){
            int half = len / 2;
            if (d == Direction.UP){
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
