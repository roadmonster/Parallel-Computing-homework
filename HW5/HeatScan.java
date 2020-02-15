/**
 * HeatScan class
 * Author: Hao Li
 * This is a class inherits from the GeneralScan class
 */

import java.util.ArrayList;


public class HeatScan extends GeneralScan<Observation, Integer[][], Integer[][]> {

    static final int DIM = 150;

    HeatScan(ArrayList<Observation> raw) {
        super(raw);
    }

    @Override
    Integer[][] init() {
        Integer[][]ret = new Integer[DIM][DIM];
        for(int i = 0; i < DIM; i++){
            for(int j = 0; j < DIM; j++){
                ret[i][j] = 0;
            }
        }
        return ret;
    }

    @Override
    Integer[][] prepare(Observation dataum) {
        Integer[][] ret = new Integer[DIM][DIM];
        for(int i = 0; i < DIM; i++){
            for(int j = 0; j < DIM; j++){
                ret[i][j] = 0;
            }
        }
        int x = (int)Math.ceil(dataum.x);
        int y = (int)Math.ceil(dataum.y);

        ret[x][y] += 1;
        return ret;
    }


    @Override
    Integer[][] combine(Integer[][] left, Integer[][] right) {
        Integer[][]ret = new Integer[DIM][DIM];

        for(int i = 0; i < DIM; i++){
            for(int j = 0; j < DIM; j++){
                ret[i][j] = left[i][j] + right[i][j];
            }
        }

        return ret;
    }

    @Override
    Integer[][] gen(Integer[][] tally) {
        return tally;
    }




}
