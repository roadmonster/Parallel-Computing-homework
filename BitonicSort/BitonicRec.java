import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BitonicRec {
    
    public static final int N = 26;
    enum direction{
        UP, 
        DOWN
    }
    List<Integer>data;

    public BitonicRec(List<Integer>data){
        this.data = data;
    }

    public void sort(){
        sort(0, this.data.size(), direction.UP);
    }

    private void sort(int start, int len, direction d){
        bseq(start, len);
        bsort(start, len, d);
    }
    
    private void bseq(int start, int len){
        if(len < 2)
            return;
        sort(start, len / 2, direction.UP);
        sort(start + len / 2, len / 2, direction.DOWN);
    }
    
    private void bsort(int start, int len, direction d){
        if(len < 2) return;
        bmerge(start, len / 2, d);
        bsort(start, len /2, d);
        bsort(start, start + len / 2, d);
    }

    private void bmerge(int start, int half, direction d){
        if(d == direction.UP){
            for(int i = 0; i < half; i++){
                if(data.get(start + i) > data.get(start + half + i)){
                    int temp = data.get(start + i);
                    data.set(start + i, data.get(start + half + i));
                    data.set(start + half + i, temp);
                }
            }
        }else{
            for(int i = 0; i < half; i++){
                if(data.get(start + i) < data.get(start + half + i)){
                    int temp = data.get(start + i);
                    data.set(start + i, data.get(start + half + i));
                    data.set(start + half + i, temp);
                }
            }
        }
    }

    public static List<Integer> generateRandNum(int size){
        if(size == 0 || (size & (size -1)) != 0)
            throw new IllegalArgumentException("size must be power of 2");
        
        Random rand = new Random();
        Set<Integer>mySet = new HashSet<>();
        while(mySet.size() < size){
            mySet.add(rand.nextInt(size));
        }
        List<Integer>ret = new ArrayList<>(mySet);
        return ret;
    }
    public static void main(String[] args) {
        List<Integer>myRandList;
        try {
            myRandList = generateRandNum(1 << N);    
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        long start = System.currentTimeMillis();
        Collections.sort(myRandList);
        long end = System.currentTimeMillis();
        System.out.println("Default sort: " + (end - start) / 1000 + " ms");
        try {
            myRandList = generateRandNum(1 << N);    
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        start = System.currentTimeMillis();
        BitonicRec br = new BitonicRec(myRandList);
        br.sort();
        end = System.currentTimeMillis();
        System.out.println("Default sort: " + (end - start) / 1000 + " ms");

        

    }
}
