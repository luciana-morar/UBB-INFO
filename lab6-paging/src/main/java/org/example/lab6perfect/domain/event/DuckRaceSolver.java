package org.example.lab6perfect.domain.event;


import org.example.lab6perfect.domain.duck.Duck;

import java.util.Arrays;

public class DuckRaceSolver {

    public static class Result{
        public static Result rezultat;
        public final double time;
        public final Duck[] aranjare;

        public Result(double time, Duck[] aranjare) {
            this.time = time;
            this.aranjare = aranjare;
        }


    }
    public static Result solve (Duck[] ducks, double[] distante, int M){

        Duck[] sortedDucks = ducks.clone();
        Arrays.sort(sortedDucks,(d1,d2)-> Double.compare(d2.getRezistenta(),d1.getRezistenta()));


        double low=0,high=10000;
        Result best=null;
        for(int i=0;i<10000;i++){
            double mid=(low+high)/2;
            Duck[] aranjare =canFinish(sortedDucks,distante,M,mid);
            if(aranjare!=null){
                best=new Result(mid, aranjare);
                high=mid;
            }
            else low=mid;
        }
        return best;
    }

    public static Duck[] canFinish(Duck[] ducks, double[] distante, int M, double maxTime){
        boolean[] used = new boolean[ducks.length]; //pt rate
        Duck[] aranjare =new Duck[ducks.length];
        int count = 0; //pt culoare
        for (int culoar = 0; culoar < M; culoar++) {
            boolean gasit = false;
            for (int i = 0; i < ducks.length; i++) {
                if (!used[i] && ducks[i].getTime(distante[culoar]) <= maxTime) {
                    aranjare[culoar]=ducks[i];
                    used[i] = true;
                    count++;
                    gasit = true;
                    break;
                }
            }
            if (!gasit) { return null; } //n-am gasit rata pt culoar
        }
        return count == M ? aranjare: null;
    }
}
