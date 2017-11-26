package data;

import bugAssignment.Constants;
/*
 * 问题：1.randomScore、weightedRandomScore、zeroRScore、myMetric1、myMetric2是什么意思？如何计算的？
 */
public class Scores {
//	0-public double randomScore;随机分数
//	1-public double weightedRandomScore;加权的随机分数
//	2-public double zeroRScore;
//
//	3-public double myMetric1; //weightedRandom mixed with recency
//	4-public double myMetric2; //weightedRandom mixed with recency and z-score

    public double differentScores[];
    public Scores(){
        differentScores = new double[Constants.TOTAL_NUMBER_OF_METRICS];//18
        for (int i=0; i<Constants.TOTAL_NUMBER_OF_METRICS; i++)
            differentScores[i] = 0;//给存放分数的数组differentScores赋初值0
    }
}
