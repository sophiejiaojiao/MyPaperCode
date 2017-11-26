package bugAssignment;

public class CommunityMembersRankingResult {
    //	0-public int assigneeRank1_random2;
//	1-public int assigneeRank1_weightedRandom2;
//	2-public int assigneeRank1_zeroR2;
//
//	3-public int assigneeRank1_weightedRandom2_myMetric1; //weightedRandom mixed with recency
//	4-public int assigneeRank1_weightedRandom2_myMetric2; //weightedRandom mixed with recency and z-score
    public int differentRankings[];
    public CommunityMembersRankingResult(){
        differentRankings = new int[Constants.TOTAL_NUMBER_OF_METRICS];
    }

}
