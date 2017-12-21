package bugAssignment;

import java.io.FileWriter;
//import java.security.AllPermission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
//import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import data.CommunityMember;
import data.Issue;
import data.IssueTextualInformation;
import data.Project;
import data.ProvideData;
import data.Scores;
import data.SOPost;
import bugAssignment.Constants.ConditionType;
import bugAssignment.Constants.SortOrder;

public class OriginalCodeTestAlpha {
    //---------------------------------------------------------------------------------------------------------------------------------------
    private static double calculateHarmonicMeanOfUpVotesForQuestionsTaggedWithAtLeastOneOfTagsInMatchedTags_SO_b(IssueTextualInformation issTI, TreeMap<String, String[]> posts1ById) {
        double sumOfInvertedUpVotes = 0;
        int count = 0;
        for (Map.Entry<String, String[]> entry : posts1ById.entrySet()) {
            SOPost post_completeInfo = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(entry.getValue());
            if (post_completeInfo.postTypeId.equals("1")) {        //i.e., this post is a question
                String[] tags = splitAnStringIncludingAllTagsToStringArrayOfTags(post_completeInfo.tags);
                for (int j = 0; j < tags.length; j++) {
                    if (textIsMatchedWithTagOrNot(issTI.projectLanguage, tags[j]) ||
                            textIsMatchedWithTagOrNot(issTI.projectDescription, tags[j]) ||
                            textIsMatchedWithTagOrNot(issTI.issueTitle, tags[j]) ||
                            textIsMatchedWithTagOrNot(issTI.issueBody, tags[j])
                            ) {
                        int scoreOfThePostPlusOne = Integer.parseInt(post_completeInfo.Score) + 1;
                        if (scoreOfThePostPlusOne < 1)
                            scoreOfThePostPlusOne = 1;
                        sumOfInvertedUpVotes = sumOfInvertedUpVotes + 1.0 / scoreOfThePostPlusOne;
                        count++;
                        break;
                    }
                }//for.
            }//if (post_com....
        }// for(Map....
        double result;

        if (count > 0) {
            if (sumOfInvertedUpVotes > 0) {
                result = count / (2 * sumOfInvertedUpVotes);
                if (result < 1)
                    result = 1;
            } else
                result = 1;
        }//if (count....
        else {
            System.out.println("        !! Warning in calculating NF (No question is tagged with the textual info of this bug report) !!");
            result = 1;
        }
        return result;
    }//findMatchedTags_SO_b().

    //---------------------------------------------------------------------------------------------------------------------------------------
    private static CommunityMember getPointerToCommunityMemberOfGHLogin(ArrayList<CommunityMember> communityMembers, String assigneeLogin) {
        int resultIndex = -1;
        for (int i = 0; i < communityMembers.size(); i++)
            if (communityMembers.get(i).ghLogin.equals(assigneeLogin)) {
                resultIndex = i;
                break;
            }//if (comm....
        if (resultIndex != -1) {//Found this assigneeLogin in the communityMembers. So return a (new) copy of that member:
            CommunityMember cm = communityMembers.get(resultIndex); //Return the reference to the same class.
            return cm;
        }//if (resu....
        else //this assigneeLogin does not exist in the communityMembers, so return null:
            return null;
    }//getCommunityMemberOfGHLogin().

    //---------------------------------------------------------------------------------------------------------------------------------------
    public static boolean textIsMatchedWithTagOrNot(String str, String tag) {
//		if (str.equals(tag) || str.startsWith(tag + " ") || str.endsWith(" " + tag) || str.contains(" " + tag + " ") || str.contains(" " + tag + ".")) //2222222222 needs to be fixed: "lucene-4870" and "open source" ("open-source" in SO)
        //We don't need the first three conditions because we added space before and after str in the calling function.
        if (str.contains(" " + tag + " ") || str.equals(tag) || str.contains(" " + tag + ".")) //2222222222 needs to be fixed: "lucene-4870" and "open source" ("open-source" in SO)
            return true;
        else
            return false;
    }//textIsMatchedWithTagOrNot().

    //---------------------------------------------------------------------------------------------------------------------------------------
    public static String[] splitAnStringIncludingAllTagsToStringArrayOfTags(String tagsString) {
        tagsString = tagsString.replaceFirst("\\[", "");
        tagsString = tagsString.replaceFirst("\\]", "");
        tagsString = tagsString.toLowerCase();
        String[] result = tagsString.split(Constants.SEPARATOR_FOR_ARRAY_ITEMS);
        return result;
    }//convertAnStringIncludingAllTagsToStringArrayOfTags

    //---------------------------------------------------------------------------------------------------------------------------------------
    public static void updateScoreOfCommunityMemberComparingTheTag(String str, String originalPostTypeId, String tag, int scoreOfThePost, CommunityMember cm) {
        str = str.toLowerCase();
        if (textIsMatchedWithTagOrNot(str, tag)) {
            cm.intersection_AQ++;
            cm.intersection_AQ_score = cm.intersection_AQ_score + scoreOfThePost + 1; //considering the answer a score, then each upvote adds an score to that.
            if (originalPostTypeId.equals("2")) {//i.e., this post is an answer:
                cm.intersection_A++;
                cm.intersection_A_score = cm.intersection_A_score + scoreOfThePost + 1; //considering the answer a score, then each upvote adds an score to that.
            }//if (origi....
            else if (originalPostTypeId.equals("1")) {        //i.e., this post is an question
                cm.intersection_Q++;
                cm.intersection_Q_score = cm.intersection_Q_score + scoreOfThePost + 1;
            }
        }//if (issTI...              issTI.issueTitle, originalPostTypeId, tags[j], scoreOfThePost, cm
    }//updateScoreOfCommunityMemberComparingTheTag().
    //---------------------------------------------------------------------------------------------------------------------------------------

    //This method calculates the scores of a user considering an issue textual information and his posts. The results are put in cm.
    public static void calculateScores(IssueTextualInformation issTI, String issueDate, ArrayList<SOPost> soPostsOfThisUserAL, TreeMap<String, String[]> allPosts1ById, CommunityMember cm, //ArrayList<Issue> issAL,
                                       int indexOfTheCurrentIssue, int maximumNumberOfPreviousAssignmentsForAMemberInThisProject, double NF) {//NF: Normalization Factor = averageOfUpVotesForQuestionsTaggedWithAtLeastOneOfTagsInMatchedTags_SO_b
//		if (cm.SOId.equals("900911"))
//			System.out.println("ssss");
        if (soPostsOfThisUserAL != null) {
            for (int i = 0; i < soPostsOfThisUserAL.size(); i++) {//For all SO posts of this user:
                SOPost post_onlyLimitedInfo = soPostsOfThisUserAL.get(i);
                if (issueDate.compareTo(post_onlyLimitedInfo.creationDate) > 0) { //means that the issueDate > postDate
                    String originalPostTypeId = post_onlyLimitedInfo.postTypeId;
                    //System.out.println(post_onlyLimitedInfo.id + "\t" + post_onlyLimitedInfo.parentId);
                    SOPost post_completeInfo = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(allPosts1ById.get(post_onlyLimitedInfo.id));
//					int scoreOfThePost = 0;
                    int scoreOfThePost = Integer.parseInt(post_completeInfo.Score); //333333333333
                    if (post_onlyLimitedInfo.postTypeId.equals("2"))//i.e., it is an answer, so fetch its related question:
                        post_completeInfo = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(allPosts1ById.get(post_onlyLimitedInfo.parentId));
                    String[] tags = splitAnStringIncludingAllTagsToStringArrayOfTags(post_completeInfo.tags);
                    int numberOfMatchedTags = 0;
                    for (int j = 0; j < tags.length; j++) {//For all tags
                        //String concatenationOfAllIssueTextualInformation = " " + issTI.projectLanguage + " " + issTI.projectDescription + " " + issTI.issueTitle + " " + issTI.issueBody + " ";
                        //					updateScoreOfCommunityMemberComparingTheTag(concatenationOfAllIssueTextualInformation, originalPostTypeId, tags[j], scoreOfThePost, cm);
                        //concatenationOfAllIssueTextualInformation = concatenationOfAllIssueTextualInformation.toLowerCase();
                        if (textIsMatchedWithTagOrNot(issTI.projectLanguage, tags[j]) ||
                                textIsMatchedWithTagOrNot(issTI.projectDescription, tags[j]) ||
                                textIsMatchedWithTagOrNot(issTI.issueTitle, tags[j]) ||
                                textIsMatchedWithTagOrNot(issTI.issueBody, tags[j])
                                ) {
                            cm.intersection_AQ++;
                            cm.intersection_AQ_score = cm.intersection_AQ_score + scoreOfThePost + 1; //considering the answer a score, then each upvote adds an score to that.
                            if (originalPostTypeId.equals("2")) {//i.e., this post is an answer:
                                cm.intersection_A++;
                                cm.intersection_A_score = cm.intersection_A_score + scoreOfThePost + 1; //considering the answer a score, then each upvote adds an score to that.
                            }//if (origi....
                            else if (originalPostTypeId.equals("1")) {        //i.e., this post is a question
                                cm.intersection_Q++;
                                numberOfMatchedTags++;
                            }
                        }//if (textIs....
                    }//for (j.
                    int tempScoreOfThePost;
                    if (scoreOfThePost < 0)
                        tempScoreOfThePost = 0;
                    else
                        tempScoreOfThePost = scoreOfThePost;
                    cm.intersection_Q_score = cm.intersection_Q_score + numberOfMatchedTags / (tempScoreOfThePost + 1.0);
                }//if issueD....
            }//for (i.
            //z-score that considers votes of Q/A:
            cm.intersection_Q_score = NF * cm.intersection_Q_score;
            if (cm.intersection_A_score + cm.intersection_Q_score > 0)
                cm.intersection_z_score = (cm.intersection_A_score - cm.intersection_Q_score) / Math.sqrt(cm.intersection_A_score + cm.intersection_Q_score);
            else
                cm.intersection_z_score = 0;
//			System.out.println("SOId: " + cm.SOId + "\t" + "QS: " + Constants.floatFormatter.format(cm.intersection_Q_score) + "\t\tAS: " + Constants.floatFormatter.format(cm.intersection_A_score) + "\t\tSSA Z-score: " + Constants.floatFormatter.format(cm.intersection_z_score));
        }//if.
        if (indexOfTheCurrentIssue > 0) {//For the first issue, we don't have these scores (randomScore, randomWeightedScore and zeroRScore). So all users are the same regarding these scores.
            if (cm.numberOfAssignmentsUpToNow > 0) {
                cm.randomScore_zeroOrOne = 1;
                cm.weightedRandomScore_count = cm.numberOfAssignmentsUpToNow;
                if (cm.numberOfAssignmentsUpToNow == maximumNumberOfPreviousAssignmentsForAMemberInThisProject)
                    cm.zeroRScore_zeroOrOne = 1;
                else
                    cm.zeroRScore_zeroOrOne = 0;
            }//if (cm.numb....
        }//if (indexO...

        cm.combinedScore1 = cm.weightedRandomScore_count + 0.02 * cm.intersection_z_score;
        cm.combinedScore2 = cm.weightedRandomScore_count + 0.002 * cm.intersection_z_score;
    }//calculateScores().

    //---------------------------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------------------------------------------------
    public static void extractAndCopyAssigneeValues(ArrayList<Issue> issAL, ArrayList<String> allAssignees) {
        //initialize the treeSet with the descending sorting as default ordering (this object is not needed in this method, but will be used in the caller method):
//		allPreviousAssignees_sortedByNumerOfAssignments_Descending = new TreeMap<Integer, String>(new Comparator<Integer>(){
//			public int compare(Integer s1, Integer s2){//We want the descending order of number:
//				if (s1 < s2)
//					return 1;
//				else
//					if (s1 > s2)
//						return -1;
//					else
//						return 0;
//			}//compare().
//		});//new TreeMap<....

//		allAssignees = new ArrayList<String>();
        for (int p = 0; p < issAL.size(); p++) {
            Issue iss = issAL.get(p);
            allAssignees.add(iss.assigneeLogin);
        }
    }//extractAndCopyAssigneeValues().

    //-----------------------------------------------------------------------------------------------------------------------------------------------------
    //This method gives you rank of real assignee "randomly" with chances based on different scores for different users:
    public static void determineRankOfRealAssigneeRandomlyBasedOnScores(HashMap<String, Scores> usersAndScores, CommunityMember communityMemberThatHasBeenAssignedToThisIssue,
                                                                        CommunityMembersRankingResult assigneeRank, double[] sumOfScores, int numberOfRanksConsideredBefore, int startingScoreIndex, int endingScoreIndex) {
        for (int i = startingScoreIndex; i <= endingScoreIndex; i++) {
            int aRank = 0;
            Random random = new Random();
            for (int j = 0; j < usersAndScores.size() && assigneeRank.differentRankings[i] == -1; j++) {
                double rand = (sumOfScores[i]) * random.nextDouble();
                for (String aLogin : usersAndScores.keySet()) {
                    Scores rs = usersAndScores.get(aLogin);
                    rand = rand - rs.differentScores[i];
                    if (rand < 0) {
                        aRank++;
                        if (communityMemberThatHasBeenAssignedToThisIssue.ghLogin.equals(aLogin))
                            assigneeRank.differentRankings[i] = numberOfRanksConsideredBefore + aRank;
                        else {
                            sumOfScores[i] = sumOfScores[i] - rs.differentScores[i];
                            rs.differentScores[i] = 0;
                            usersAndScores.put(aLogin, rs);
                        }
                        break;
                    }//if (randomV....
                }//for (String aLogi....
            }//for (j.
        }//for (i.
    }//determineRankOfRealAssignee().

    //---------------------------------------------------------------------------------------------------------------------------------------
    //This method gives you rank of real assignee "randomly" with chances based on different scores for different users:
    public static void determineRankOfRealAssignee_FirstStaticallyThenRandomly_BasedOnScores(HashMap<String, Scores> usersAndScores, CommunityMember communityMemberThatHasBeenAssignedToThisIssue,
                                                                                             CommunityMembersRankingResult assigneeRank, int numberOfRanksConsideredBefore, int startingScoreIndex, int endingScoreIndex) {
        if (usersAndScores.containsKey(communityMemberThatHasBeenAssignedToThisIssue.ghLogin)) { //Means that the real assignee is in usersAndScores object --that contains the scores of previous assignees--, otherwise we don't assign any rank to the real assignee (should remain the same '-1')
            for (int i = startingScoreIndex; i <= endingScoreIndex; i++) {
                Random random = new Random();
                int numberOfMembersWithGreaterScore = 0;
                int numberOfMembersWithEqualScore = 0;
                Scores scoresOfTheRealAssignee = usersAndScores.get(communityMemberThatHasBeenAssignedToThisIssue.ghLogin);
                for (String aLogin : usersAndScores.keySet()) {
                    Scores scoresOfALogin = usersAndScores.get(aLogin);
                    if (scoresOfALogin.differentScores[i] > scoresOfTheRealAssignee.differentScores[i])
                        numberOfMembersWithGreaterScore++;
                    else
                        //						if ((scoresOfALogin.differentScores[i] == scoresOfTheRealAssignee.differentScores[i]) && (scoresOfALogin.differentScores[i] > 0))
                        if (scoresOfALogin.differentScores[i] == scoresOfTheRealAssignee.differentScores[i])
                            numberOfMembersWithEqualScore++;
                }//for (aLogin.
                assigneeRank.differentRankings[i] = numberOfRanksConsideredBefore + numberOfMembersWithGreaterScore + random.nextInt(numberOfMembersWithEqualScore) + 1;
//                assigneeRank.differentRankings[i] = numberOfRanksConsideredBefore + numberOfMembersWithGreaterScore + 1;
            }//for (i.
        }//if (users....
    }//determineRankOfRealAssignee().

    //---------------------------------------------------------------------------------------------------------------------------------------
    public static void determineRankOfRealAssigneeCompletelyRandomly(ArrayList<String> users, CommunityMember communityMemberThatHasBeenAssignedToThisIssue,
                                                                     CommunityMembersRankingResult assigneeRank, int numberOfRanksConsideredBefore, int startingScoreIndex, int endingScoreIndex) {
        for (int i = startingScoreIndex; i <= endingScoreIndex; i++)
            if (assigneeRank.differentRankings[i] == -1) {
                Collections.shuffle(users);
                for (int j = 0; j < users.size(); j++)
                    if (users.get(j).equals(communityMemberThatHasBeenAssignedToThisIssue.ghLogin)) {
                        assigneeRank.differentRankings[i] = numberOfRanksConsideredBefore + j + 1;
                        break;
                    }//if (commu....
            }//if (assig....
    }

    //---------------------------------------------------------------------------------------------------------------------------------------
    public static void mergeWith_z_score(ArrayList<CommunityMember> cmAL, HashMap<String, Scores> usersAndScores, int startingScoreIndex, int endingScoreIndex) {
        //Adding z-scores to scores of my metrics (from index 4 to higher):
        double[] minimumScore = new double[Constants.TOTAL_NUMBER_OF_METRICS];
        for (int i = startingScoreIndex; i <= endingScoreIndex; i++)
            minimumScore[i] = Constants.A_FAR_LARGESCORE;
        for (int k = 0; k < cmAL.size(); k++) {
            CommunityMember cm = cmAL.get(k);
            if (usersAndScores.containsKey(cm.ghLogin)) {
                Scores rs = usersAndScores.get(cm.ghLogin);
                for (int i = startingScoreIndex; i <= endingScoreIndex; i++) {
                    //Social Z_Score:
                    rs.differentScores[i] = rs.differentScores[i] + Constants.Z_SCORE_COEFFICIENTS[i] * cm.intersection_z_score;
                    //Traditional Z_Score: 33333333333
//                    rs.differentScores[i] = rs.differentScores[i] + Constants.Z_SCORE_COEFFICIENTS[i] * cm.traditional_z_score;
                    //***********************AnswerNum*****************************
//                    rs.differentScores[i] = rs.differentScores[i] + Constants.Z_SCORE_COEFFICIENTS[i] * cm.totalAnswers;
                    //*********************************
                    if (rs.differentScores[i] < minimumScore[i])
                        minimumScore[i] = rs.differentScores[i];
                }//for (i.
                usersAndScores.put(cm.ghLogin, rs);
            }//if (allPr....
        }//for (k
        //Because of z-score, some of these scores can be negative. In these cases, we'll subtract the minimum of them [that is negative] from all the scores (that is actually adding a positive value to all):
        for (int i = startingScoreIndex; i <= endingScoreIndex; i++)
            if (minimumScore[i] < 0) {//Add minimumScores to minimumScore2:
                for (String aLogin : usersAndScores.keySet()) {
                    Scores rs = usersAndScores.get(aLogin);
                    rs.differentScores[i] = rs.differentScores[i] - minimumScore[i];
                    usersAndScores.put(aLogin, rs);
                }//for (k
            }//if (minim....
    }//mergeWith_z_score().

    //---------------------------------------------------------------------------------------------------------------------------------------
    public static void calculateSums(HashMap<String, Scores> usersAndScores, double[] sumOfScores, int startingScoreIndex, int endingScoreIndex) {
        for (int i = startingScoreIndex; i <= endingScoreIndex; i++)
            sumOfScores[i] = 0;
        for (Scores rs : usersAndScores.values())
            for (int i = startingScoreIndex; i <= endingScoreIndex; i++)
                sumOfScores[i] = sumOfScores[i] + rs.differentScores[i];
    }//calculateSums().

    //---------------------------------------------------------------------------------------------------------------------------------------
    public static void copyUsersWithZeroAssignmentPlusSOScoreToHashMapOfArrayList(HashMap<String, Scores> usersAndScores, HashMap<Integer, ArrayList<String>> usersWithZeroAssignmentPlusSOScore, int startingScoreIndex, int endingScoreIndex) {
        for (String aLogin : usersAndScores.keySet()) {
            Scores rs = usersAndScores.get(aLogin);
            for (int i = startingScoreIndex; i <= endingScoreIndex; i++)
                if (rs.differentScores[i] <= 0) {
                    if (usersWithZeroAssignmentPlusSOScore.containsKey(i)) { //Means that for this number, i, we have at least one user with zero SOScore. So there is no need to create a new ArrayList.
                        usersWithZeroAssignmentPlusSOScore.get(i).add(aLogin);
                    } else {
                        ArrayList<String> anArrayList = new ArrayList<String>();
                        anArrayList.add(aLogin);
                        usersWithZeroAssignmentPlusSOScore.put(i, anArrayList);
                    }//else.
                }
        }//for aLogin.
    }//copyUsersWithZeroAssignmentPlusSOScoreToHashMapOfArrayList().

    //---------------------------------------------------------------------------------------------------------------------------------------
    public static void determineAssigneeRanksRandomly(ArrayList<CommunityMember> cmAL, ArrayList<String> allAssignees,
                                                      int indexOfTheCurrentIssue, HashMap<String, Integer> allPreviousAssignees_unique, CommunityMember communityMemberThatHasBeenAssignedToThisIssue, CommunityMembersRankingResult assigneeRank) {
        //First, give the chance to the previous assignees, randomly:
        HashMap<String, Scores> allPreviousAssigneesAndTheirChances = new HashMap<String, Scores>();
        ArrayList<String> silentUsers = new ArrayList<String>();
        ArrayList<String> silentUsers2_forZeroR = new ArrayList<String>();
        int numberOfUsersWithZeroREqualToOne = 0;
//		int[] numberOfRanksConsideredBefore = new int[Constants.TOTAL_NUMBER_OF_METRICS];
        for (int i = 0; i < Constants.TOTAL_NUMBER_OF_METRICS; i++) {
            assigneeRank.differentRankings[i] = -1; //To check (later, in the next loop) if the user gets his rank in the first loop (considering previous assignees).
//			numberOfRanksConsideredBefore[i] = 0;
        }
        double[] sumOfScores = new double[Constants.TOTAL_NUMBER_OF_METRICS];
        for (int i = 0; i < Constants.TOTAL_NUMBER_OF_METRICS; i++)
            sumOfScores[i] = 0;

//		if (indexOfTheCurrentIssue > 0){
//			//Determning Scores.randomScore, Scores.weightedRandomScore and Scores.zeroRScore:
//			int numberOfUsersWithZeroRValueEqualToOne = 0;
//			for (int k=0; k<cmAL.size(); k++)
//				if (cmAL.get(k).zeroRScore_zeroOrOne == 1)
//					numberOfUsersWithZeroRValueEqualToOne++;
//			for (int k=0; k<cmAL.size(); k++){
//				CommunityMember cm = cmAL.get(k);
//				if (cm.randomScore_zeroOrOne == 1){
//					Scores rs = new Scores();
//					rs.differentScores[0] = (double)1/allPreviousAssignees_unique.size();
//					sumOfScores[0] = sumOfScores[0] + rs.differentScores[0];
//					if (cm.zeroRScore_zeroOrOne == 1){
//						rs.differentScores[2] = (double)1/numberOfUsersWithZeroRValueEqualToOne;
//						sumOfScores[2] = sumOfScores[2] + rs.differentScores[2];
//						numberOfUsersWithZeroREqualToOne++;
//					}//if (cm.zeroR....
//					else
//						silentUsers2_forZeroR.add(cm.ghLogin);
//					rs.differentScores[1] = (double)cm.weightedRandomScore_count/indexOfTheCurrentIssue;
//					sumOfScores[1] = sumOfScores[1] + rs.differentScores[1];
//					allPreviousAssigneesAndTheirChances.put(cm.ghLogin, rs);
//				}//if (cm.rand....
//				else{
//					silentUsers.add(cm.ghLogin);
//					silentUsers2_forZeroR.add(cm.ghLogin);
//				}//else.
//			}//for (k.
//			//Determining Scores.weightedRandomScore2_myMetric               (//Recency metric?:):
//			String aTempLogin;
//			for (int p=0; p<indexOfTheCurrentIssue; p++){
//				aTempLogin = allAssignees.get(p);
//				Scores rs = allPreviousAssigneesAndTheirChances.get(aTempLogin);
//				double recencyScore = (double)1/(indexOfTheCurrentIssue - p + Constants.UNIMPORTANCE_OF_RECENT_ASSIGNMENTS);
//				for (int i=3; i<Constants.TOTAL_NUMBER_OF_METRICS; i++)
//					rs.differentScores[i] = rs.differentScores[i] + recencyScore;
//				allPreviousAssigneesAndTheirChances.put(aTempLogin, rs);
//			}//for (p.
//
//			mergeWith_z_score(cmAL, allPreviousAssigneesAndTheirChances, 4, Constants.TOTAL_NUMBER_OF_METRICS-1);
//			calculateSums(allPreviousAssigneesAndTheirChances, sumOfScores, 3, Constants.TOTAL_NUMBER_OF_METRICS-1);//For indices 0, 1 and 2, we've already calculated the sums before.
//
//			HashMap<Integer, ArrayList<String>> allPreviousAssigneesWithZeroAssignmentPlusSOScore = new HashMap<Integer, ArrayList<String>>();
//			copyUsersWithZeroAssignmentPlusSOScoreToHashMapOfArrayList(allPreviousAssigneesAndTheirChances, allPreviousAssigneesWithZeroAssignmentPlusSOScore, 4, Constants.TOTAL_NUMBER_OF_METRICS-1);
//
//			//Ranking based on random generated double number (until passing over all candidates, or, choosing the currently assigned user):
//			//Part 1/5, determining assigneeRank1_random2:
//			determineRankOfRealAssigneeRandomlyBasedOnScores(allPreviousAssigneesAndTheirChances, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, sumOfScores, 0, 0, 0);
//			//Part 2/5, determining assigneeRank1_weightedRandom2:
//			determineRankOfRealAssigneeRandomlyBasedOnScores(allPreviousAssigneesAndTheirChances, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, sumOfScores, 0, 1, 1);
//			//Part 3/5, determining assigneeRank1_zeroR2:
//			determineRankOfRealAssigneeRandomlyBasedOnScores(allPreviousAssigneesAndTheirChances, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, sumOfScores, 0, 2, 2);
//			//Part 4/5, determining assigneeRank1_combinedRank3_weightedRandom_Recency:
//			determineRankOfRealAssigneeRandomlyBasedOnScores(allPreviousAssigneesAndTheirChances, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, sumOfScores, 0, 3, 3);
//			//Part 5/5, determining assigneeRank1_combinedRank4_weightedRandom_Recency_zScore:
//			//determineRankOfRealAssigneeRandomlyBasedOnScores(allPreviousAssigneesAndTheirChances, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, sumOfScores, 0, 4, Constants.TOTAL_NUMBER_OF_METRICS-1);
//			determineRankOfRealAssignee_FirstStaticallyThenRandomly_BasedOnScores(allPreviousAssigneesAndTheirChances, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, 0, 4, Constants.TOTAL_NUMBER_OF_METRICS-1);
//			for (int i=4; i<Constants.TOTAL_NUMBER_OF_METRICS; i++){
//				ArrayList<String> usersWithZeroAssignmentPlusSOScore_accordingToAScoringType = allPreviousAssigneesWithZeroAssignmentPlusSOScore.get(i);
//				if ((usersWithZeroAssignmentPlusSOScore_accordingToAScoringType != null) && (usersWithZeroAssignmentPlusSOScore_accordingToAScoringType.size() > 0))
//					determineRankOfRealAssigneeCompletelyRandomly(usersWithZeroAssignmentPlusSOScore_accordingToAScoringType, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, allPreviousAssigneesAndTheirChances.size()-usersWithZeroAssignmentPlusSOScore_accordingToAScoringType.size(), i, i);
//			}//for (i.
//		}//if (indexO....
//		else //Means that this is the first issue that is being triaged:
        for (int k = 0; k < cmAL.size(); k++) {
            CommunityMember cm = cmAL.get(k);
            silentUsers.add(cm.ghLogin);
            silentUsers2_forZeroR.add(cm.ghLogin);
        }//for (k.

        //Now if the real assignee was not in the previous assignees (this is checked in the following method), give the chance to the others ("silent users") randomly with the same chance:
        //Step 1/5: determining assigneeRank1_random2:
//		determineRankOfRealAssigneeCompletelyRandomly(silentUsers, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, allPreviousAssigneesAndTheirChances.size(), 0, 0);
//		//Step 2/5: determining assigneeRank1_weightedRandom2:
//		determineRankOfRealAssigneeCompletelyRandomly(silentUsers, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, allPreviousAssigneesAndTheirChances.size(), 1, 1);
//		//Step 3/5: determining assigneeRank1_zeroR2:
//		determineRankOfRealAssigneeCompletelyRandomly(silentUsers2_forZeroR, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, numberOfUsersWithZeroREqualToOne, 2, 2);
//		//Step 4/5: determining assigneeRank1_weightedRandom2_myMetric1:
//		determineRankOfRealAssigneeCompletelyRandomly(silentUsers, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, allPreviousAssigneesAndTheirChances.size(), 3, 3);


        //Step 5/5: determining assigneeRank1_weightedRandom2_myMetric2:
        HashMap<String, Scores> allSilentUsersAndTheirChances = new HashMap<String, Scores>();
        //ArrayList<String> allSilentUsers_zeroSOScore = new ArrayList<String>();
        for (String aLogin : silentUsers) {
            Scores rs = new Scores();
            allSilentUsersAndTheirChances.put(aLogin, rs);
        }//for (String aLogin.
        mergeWith_z_score(cmAL, allSilentUsersAndTheirChances, 4, Constants.TOTAL_NUMBER_OF_METRICS - 1);
        calculateSums(allSilentUsersAndTheirChances, sumOfScores, 4, Constants.TOTAL_NUMBER_OF_METRICS - 1);

        HashMap<Integer, ArrayList<String>> allSilentUsersWithZeroAssignmentPlusSOScore = new HashMap<Integer, ArrayList<String>>();
        copyUsersWithZeroAssignmentPlusSOScoreToHashMapOfArrayList(allSilentUsersAndTheirChances, allSilentUsersWithZeroAssignmentPlusSOScore, 4, Constants.TOTAL_NUMBER_OF_METRICS - 1);
//		determineRankOfRealAssigneeRandomlyBasedOnScores(allSilentUsersAndTheirChances, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, sumOfScores, allPreviousAssigneesAndTheirChances.size(), 4, Constants.TOTAL_NUMBER_OF_METRICS-1);
        determineRankOfRealAssignee_FirstStaticallyThenRandomly_BasedOnScores(allSilentUsersAndTheirChances, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, allPreviousAssigneesAndTheirChances.size(), 4, Constants.TOTAL_NUMBER_OF_METRICS - 1);
//		for (int i=4; i<Constants.TOTAL_NUMBER_OF_METRICS; i++){
//			ArrayList<String> usersWithZeroAssignmentPlusSOScore_accordingToAScoringType = allSilentUsersWithZeroAssignmentPlusSOScore.get(i);
//			if ((usersWithZeroAssignmentPlusSOScore_accordingToAScoringType != null) && (usersWithZeroAssignmentPlusSOScore_accordingToAScoringType.size() > 0))
//				determineRankOfRealAssigneeCompletelyRandomly(usersWithZeroAssignmentPlusSOScore_accordingToAScoringType, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, allPreviousAssigneesAndTheirChances.size()+allSilentUsersAndTheirChances.size()-usersWithZeroAssignmentPlusSOScore_accordingToAScoringType.size(), i, i);
//		}//for (i.
    }//determineAssigneeRanksRandomly().

    //-----------------------------------------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------------------------------------------
    public static void triage2_basedOnMultipleCriteria(String communityInputPath, String communityInputTSVFileName, String communitiesSummaryInputTSV,
                                                       String projectsInputPath, String projectsInputTSV,
                                                       String assignedIssuesInputPath, String assignedIssuesInputTSV,
                                                       String postsOfCommunityMembersInputPath, String postsOfCommunityMembersInputTSV,
                                                       int topXProjectsToTriage,
                                                       String triageResultsOututPath, String triageOutputFileName,
                                                       int showProgressInterval, long testOrReal) {//This method recommends assignee for issues simply by intersection issue textual fields and SO tags. The users with more answers (and maybe questions) tagged with those tags are considered the best candidates.
        try {
            //Read all communities' projects from summary file (maybe just to double check with the community file):
            HashSet<String> ownerLoginAndProjectNames = TSVManipulations.readUniqueFieldFromTSV(
                    communityInputPath, communitiesSummaryInputTSV, 1, 3, ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, Constants.THIS_IS_REAL, 1);
            //Now, read all columns of projects and their members:
            TreeMap<String, String[]> projects = TSVManipulations.readUniqueKeyAndItsValueFromTSV(
                    projectsInputPath, projectsInputTSV, null, 2, 9, Constants.ALL, ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval * 20, Constants.THIS_IS_REAL, 2);
            //Read info of all posts made by all community members by id: (total: ?)
            TreeMap<String, String[]> posts1ById = TSVManipulations.readUniqueKeyAndItsValueFromTSV(
                    postsOfCommunityMembersInputPath, postsOfCommunityMembersInputTSV, null, 0, 9, "0$1$2$3$4$5", ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval * 10, testOrReal, 3);

            //Read info of all posts made by all community members (by field ownerUserId): (total: 3,642,245)
            TreeMap<String, ArrayList<String[]>> posts2ByOwnerId = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
                    postsOfCommunityMembersInputPath, postsOfCommunityMembersInputTSV, null, 2, SortOrder.DEFAULT_FOR_STRING, 9, "0$1$2$3$6", ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval * 10, testOrReal, 4);
            //Reading assigned issues in all communities (projects):
            TreeMap<String, ArrayList<String[]>> ownerLoginAndProjectName_and_allAssignedIssuesInThisProject = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
                    assignedIssuesInputPath, assignedIssuesInputTSV, null, 1, SortOrder.DEFAULT_FOR_STRING, 11, "0$5$6$8$9$10", ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval * 5, testOrReal, 5);
            //fillStopWords();
            //Reading the community file:
            TreeMap<String, ArrayList<String[]>> ownerLoginAndProjectName_and_ItsCommunityMembers = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
                    communityInputPath, communityInputTSVFileName, null, 1, SortOrder.DEFAULT_FOR_STRING, 6, Constants.ALL, ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, 6);

            //Starting from the top numberOfProjectmembers, and writing triage results in output files:
            System.out.println("7- Triaging! And writing the precision in output file (" + triageOutputFileName + "):");
            FileWriter writer = new FileWriter(triageResultsOututPath + "\\" + triageOutputFileName);

            String output = "mySQLProjectId\townerLogin/projName\t#ofCommunityMembers\tissueId\t"
                    + "assigneeSOId\tassigneeGHLogin\tassigneeGHMySQLId\tassigneeGHMongoDBId\t"
                    + "aR0:random\taR1:wRandom\taR2:zeroR\taR3:wR+Recency\taR4:Recency+zScore\t";
            for (int j = 5; j < Constants.TOTAL_NUMBER_OF_METRICS; j++)
                output = output + "aR" + j + ":tuning(" + Constants.highPrecisionFloatFormatter.format(Constants.Z_SCORE_COEFFICIENTS[j]) + ")\t";
            output = output + "aRR0:random\taRR1:wRandom\taRR2:zeroR\taRR3:wR+Recency\taRR4:Recency+zScore\t";
            for (int j = 5; j < Constants.TOTAL_NUMBER_OF_METRICS; j++)
                output = output + "aRR" + j + ":tunning(" + Constants.highPrecisionFloatFormatter.format(Constants.Z_SCORE_COEFFICIENTS[j]) + ")\t";
            output = output + "projectLanguage\tprojectDescription\tissueTitle\tissueLabels\tissueBody\n";
            writer.append(output);

            Date d0 = new Date();
            int i = 0, projectsTriaged = 0;
            //For all projects (chronologocally based on the projects that are in the summary file):
            for (String anOwnerLoginAndProjectName : ownerLoginAndProjectNames) {
                if (ownerLoginAndProjectName_and_ItsCommunityMembers.containsKey(anOwnerLoginAndProjectName)) {//This condition is just for double checking the existence of the records of this project (that has beed read from communitiesSummary) with the info in the community file, and then triaging them:
                    System.out.println("    - Proj " + (i + 1) + ") Started triaging in project \"" + anOwnerLoginAndProjectName + "\"");
                    Date d1 = new Date();
                    //Reading community members:
                    ArrayList<CommunityMember> cmAL = ProvideData.copyCommunityFromArrayListOfStringArray(ownerLoginAndProjectName_and_ItsCommunityMembers.get(anOwnerLoginAndProjectName));
                    ProvideData.initializeRandomScores(cmAL);
                    //******************作者的计算没有匹配tag的传统Z-score**********************
//                    ProvideData.calculateTraditionalZ_ScoreParameters(cmAL, posts2ByOwnerId);
                    //**********************************************************************

                    int maximumNumberOfPreviousAssignmentsForAMemberInThisProject = 0;
                    System.out.println("        Total # of community members: " + cmAL.size());
                    Project aProject = ProvideData.copyProjectFromStringArray(projects.get(anOwnerLoginAndProjectName));
                    //Triage all assigned issues in this project:
                    ArrayList<Issue> issAL = ProvideData.copyIssuesFromArrayListOfStringArray_OnlyThereAreSomeFields3(ownerLoginAndProjectName_and_allAssignedIssuesInThisProject.get(anOwnerLoginAndProjectName));
                    Collections.sort(issAL);
                    System.out.println("        Total # of issues: " + issAL.size());

                    ArrayList<String> allAssignees = new ArrayList<String>();
                    //TreeMap<Integer, String> allPreviousAssignees_sortedByAssignmentTimes_Descending = null;
                    extractAndCopyAssigneeValues(issAL, allAssignees);
                    HashMap<String, Integer> allPreviousAssignees_unique = new HashMap<String, Integer>();

                    for (int p = 0; p < issAL.size(); p++) {
                        Issue iss = issAL.get(p);
                        IssueTextualInformation issTI = new IssueTextualInformation(aProject.language, aProject.description, iss.labels, iss.title, iss.body);
                        CommunityMember communityMemberThatHasBeenAssignedToThisIssue = getPointerToCommunityMemberOfGHLogin(cmAL, iss.assigneeLogin);
                        //Checking if the assignee is from the community (the only case that triaging means. Otherwise triaging is useless because the issue is assigned to neither of the community members who we are to rank and recommend them):
                        if (communityMemberThatHasBeenAssignedToThisIssue != null) {//Triage:
                            ProvideData.initializeIntersectionWithBugsScores(cmAL);

                            //Finding NF (Normalization Factor); <averageOfUpVotesForQuestionsTaggedWithAtLeastOneOfTagsInMatchedTags_SO_b>:
                            //333333333333333:
//							double NF = 1;
                            double NF = calculateHarmonicMeanOfUpVotesForQuestionsTaggedWithAtLeastOneOfTagsInMatchedTags_SO_b(issTI, posts1ById);
                            //Iterating over all community members, and calculating their scores for this issue:
                            for (int k = 0; k < cmAL.size(); k++) {
                                //Identifying the posts of this community member:
                                CommunityMember cm = cmAL.get(k);
                                ArrayList<SOPost> soPostsOfThisCommunityMemberAL = null;
                                if (posts2ByOwnerId.containsKey(cm.SOId))
                                    soPostsOfThisCommunityMemberAL = ProvideData.copyPostContentsFromArrayListOfStringArray_OnlyThereAreSomeFields1(posts2ByOwnerId.get(cm.SOId));
                                //作者的计算SSA-Z-score的方法
//                                calculateScores(issTI, iss.createdAt, soPostsOfThisCommunityMemberAL, posts1ById, cm, p, maximumNumberOfPreviousAssignmentsForAMemberInThisProject, NF);
                                calculateWeightScores(issTI, iss.createdAt, soPostsOfThisCommunityMemberAL, posts1ById, cm, p, maximumNumberOfPreviousAssignmentsForAMemberInThisProject, NF);

                                //********************计算至少匹配一个的AnswerNum和Z-Score*******************
//                                calculateZScores(issTI, iss.createdAt, soPostsOfThisCommunityMemberAL, posts1ById, cm, p, maximumNumberOfPreviousAssignmentsForAMemberInThisProject);
                                //****************纯计算AnswerNum和Z-score*************************************************
//                                calculateNoTagZScores(iss.createdAt, soPostsOfThisCommunityMemberAL, cm);
                            }//for (i.
                            //										System.out.println();
                            CommunityMembersRankingResult assigneeRank = new CommunityMembersRankingResult();
//							determineAssigneeRanksStatically(communityMemberThatHasBeenAssignedToThisIssue, cmAL, assigneeRank);

                            determineAssigneeRanksRandomly(cmAL, allAssignees,
                                    p, allPreviousAssignees_unique, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank);

                            if (allPreviousAssignees_unique.containsKey(communityMemberThatHasBeenAssignedToThisIssue.ghLogin)) {
                                int tempNumber = allPreviousAssignees_unique.get(communityMemberThatHasBeenAssignedToThisIssue.ghLogin);
                                allPreviousAssignees_unique.put(communityMemberThatHasBeenAssignedToThisIssue.ghLogin, tempNumber + 1);
                            }//if (allP....
                            else
                                allPreviousAssignees_unique.put(communityMemberThatHasBeenAssignedToThisIssue.ghLogin, 1);
                            //Creating output string and writing it:
                            output = aProject.projectMySQLId + "\t" + aProject.ownerLoginAndProjectName + "\t" + cmAL.size() + "\t" + iss.id + "\t"
                                    + communityMemberThatHasBeenAssignedToThisIssue.SOId + "\t" + communityMemberThatHasBeenAssignedToThisIssue.ghLogin + "\t" + communityMemberThatHasBeenAssignedToThisIssue.ghMySQLId + "\t" + communityMemberThatHasBeenAssignedToThisIssue.ghMongoDBId + "\t";
                            for (int j = 0; j < Constants.TOTAL_NUMBER_OF_METRICS; j++)
                                output = output + assigneeRank.differentRankings[j] + "\t";
                            for (int j = 0; j < Constants.TOTAL_NUMBER_OF_METRICS; j++)
                                output = output + 1.0 / assigneeRank.differentRankings[j] + "\t";
                            output = output + aProject.language + "\t" + aProject.description + "\t" + iss.title + "\t" + iss.labels + "\t" + iss.body + "\n";
                            writer.append(output);

                            communityMemberThatHasBeenAssignedToThisIssue.numberOfAssignmentsUpToNow++;
                            if (communityMemberThatHasBeenAssignedToThisIssue.numberOfAssignmentsUpToNow > maximumNumberOfPreviousAssignmentsForAMemberInThisProject)
                                maximumNumberOfPreviousAssignmentsForAMemberInThisProject = communityMemberThatHasBeenAssignedToThisIssue.numberOfAssignmentsUpToNow;
                        }
                    }//for (int p ...
                    projectsTriaged++;
                    Date d2 = new Date();
                    System.out.println("    Finished in " + Constants.integerFormatter.format((d2.getTime() - d1.getTime()) / 1000) + " seconds (" + Constants.integerFormatter.format(((d2.getTime() - d1.getTime()) / 1000) / 60) + " minutes and " + Constants.integerFormatter.format(((d2.getTime() - d1.getTime()) / 1000) % 60) + " seconds).");
                    System.out.println("-------------------------------------------");
                    if (projectsTriaged >= topXProjectsToTriage)
                        break;
                } else {
                    System.out.println("            Weird error: The communitesSummary file contains project \"" + anOwnerLoginAndProjectName + "\", but the community file does not! (maybe due to \"testOrReal\"=THIS_IS_A_TEST):");
                    break;
                }//else.
                i++;
                if (i % showProgressInterval == 0)
                    System.out.println("    number of numberOfMembers (including all projects in that range) examined: " + Constants.integerFormatter.format(i));
                if (testOrReal > Constants.THIS_IS_REAL)
                    if (i >= testOrReal) //instead of i >= testOrReal (because we want to assess just the top project's members for testing)
                        break;
                if (projectsTriaged >= topXProjectsToTriage)
                    break;
            }//for (String anOwn...
            writer.flush();
            writer.close();
            Date d3 = new Date();
            System.out.println("    Finished in " + Constants.integerFormatter.format((d3.getTime() - d0.getTime()) / 1000) + " seconds (" +
                    Constants.integerFormatter.format(((d3.getTime() - d0.getTime()) / 1000) / 3600) + " hours and " +
                    Constants.integerFormatter.format((((d3.getTime() - d0.getTime()) / 1000) % 3600) / 60) + " minutes and " +
                    Constants.integerFormatter.format(((d3.getTime() - d0.getTime()) / 1000) % 60) + " seconds).");
            System.out.println("===========================================");
            System.out.println("===========================================");
            System.out.println("===========================================");
            //				if (projectsTriaged >= topXProjectsToTriage)
            //					break;
            //					break;
            System.out.println("End of triaging.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//triage1_basedOnSimpleIntersectionOfIssueTextualFieldsInGHAndTagsInSO().

    public static void calculateWeightScores(IssueTextualInformation issTI, String issueDate, ArrayList<SOPost> soPostsOfThisCommunityMemberAL, TreeMap<String, String[]> posts1ById, CommunityMember cm, int indexOfTheCurrentIssue, int maximumNumberOfPreviousAssignmentsForAMemberInThisProject, double NF) {
        if (soPostsOfThisCommunityMemberAL != null) {
            for (int i = 0; i < soPostsOfThisCommunityMemberAL.size(); i++) {//For all SO posts of this user:
                SOPost post_onlyLimitedInfo = soPostsOfThisCommunityMemberAL.get(i);
                if (issueDate.compareTo(post_onlyLimitedInfo.creationDate) > 0) { //means that the issueDate > postDate
                    String originalPostTypeId = post_onlyLimitedInfo.postTypeId;
                    //System.out.println(post_onlyLimitedInfo.id + "\t" + post_onlyLimitedInfo.parentId);
                    SOPost post_completeInfo = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(posts1ById.get(post_onlyLimitedInfo.id));
//					int scoreOfThePost = 0;
                    int scoreOfThePost = Integer.parseInt(post_completeInfo.Score); //333333333333
                    if (post_onlyLimitedInfo.postTypeId.equals("2"))//i.e., it is an answer, so fetch its related question:
                        post_completeInfo = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(posts1ById.get(post_onlyLimitedInfo.parentId));
                    String[] tags = splitAnStringIncludingAllTagsToStringArrayOfTags(post_completeInfo.tags);

                    HashMap<String, Double> numOfTags = calculateNumOfTagInIssueText(tags, issTI);
                    double wOfTags = 0;
                    for (int j = 0; j < tags.length; j++) {
                        if (textIsMatchedWithTagOrNot(issTI.projectLanguage,
                                tags[j])
                                || textIsMatchedWithTagOrNot(
                                issTI.projectDescription, tags[j])
                                || textIsMatchedWithTagOrNot(issTI.issueTitle,
                                tags[j])
                                || textIsMatchedWithTagOrNot(issTI.issueBody,
                                tags[j])) {
                            if (originalPostTypeId.equals("2")) {
                                cm.intersection_A++;
                                cm.intersection_A_score = cm.intersection_A_score + scoreOfThePost * numOfTags.get(tags[j]);
                            } else if (originalPostTypeId.equals("1")) {
                                cm.intersection_Q_score++;
                                wOfTags = wOfTags + numOfTags.get(tags[j]);
                            }
                        }
                    }
                    int tempScoreOfThePost;
                    if (scoreOfThePost < 0) {
                        tempScoreOfThePost = 0;
                    } else{
                        tempScoreOfThePost = scoreOfThePost;
                    }
                    cm.intersection_Q_score = cm.intersection_Q_score + wOfTags / (tempScoreOfThePost + 1.0);
                }//if issueD....
            }//for (i.
            //z-score that considers votes of Q/A:
            cm.intersection_Q_score = NF * cm.intersection_Q_score;
            if (cm.intersection_A_score + cm.intersection_Q_score > 0)
                cm.intersection_z_score = (cm.intersection_A_score - cm.intersection_Q_score) / Math.sqrt(cm.intersection_A_score + cm.intersection_Q_score);
            else
                cm.intersection_z_score = 0;
//			System.out.println("SOId: " + cm.SOId + "\t" + "QS: " + Constants.floatFormatter.format(cm.intersection_Q_score) + "\t\tAS: " + Constants.floatFormatter.format(cm.intersection_A_score) + "\t\tSSA Z-score: " + Constants.floatFormatter.format(cm.intersection_z_score));
        }//if.
        if (indexOfTheCurrentIssue > 0) {//For the first issue, we don't have these scores (randomScore, randomWeightedScore and zeroRScore). So all users are the same regarding these scores.
            if (cm.numberOfAssignmentsUpToNow > 0) {
                cm.randomScore_zeroOrOne = 1;
                cm.weightedRandomScore_count = cm.numberOfAssignmentsUpToNow;
                if (cm.numberOfAssignmentsUpToNow == maximumNumberOfPreviousAssignmentsForAMemberInThisProject)
                    cm.zeroRScore_zeroOrOne = 1;
                else
                    cm.zeroRScore_zeroOrOne = 0;
            }//if (cm.numb....
        }//if (indexO...

        cm.combinedScore1 = cm.weightedRandomScore_count + 0.02 * cm.intersection_z_score;
        cm.combinedScore2 = cm.weightedRandomScore_count + 0.002 * cm.intersection_z_score;
    }

    public static HashMap<String, Double> calculateNumOfTagInIssueText(String[] tags, IssueTextualInformation issTI) {
        HashMap<String, Double> hmI = new HashMap<String, Double>();
        // 将需要比对的BR文本合成一个string并用空格替换标点符号
        String issText = (issTI.projectLanguage + issTI.projectDescription + issTI.issueLabels + issTI.issueTitle + issTI.issueBody).replaceAll("[,\\.，。\\!！《》、;\\:；：\\s]", " ");
        // 空格分词法
        String[] issWord = issText.split(" ");
        for (int i = 0; i < tags.length; i++) {
            double count = 0;
            for (int j = 0; j < issWord.length; j++) {
                if (tags[i].equals(issWord[j])) {
                    count++;
                }
            }
            hmI.put(tags[i], count);
        }
        return hmI;
    }

    public static void calculateNoTagZScores(String issDate, ArrayList<SOPost> soPostsOfThisCommunityMemberAL, CommunityMember cm) {
        if (soPostsOfThisCommunityMemberAL != null) {
            for (int i = 0; i < soPostsOfThisCommunityMemberAL.size(); i++) {
                SOPost post_onlyLimitedInfo = soPostsOfThisCommunityMemberAL.get(i);
                if (issDate.compareTo(post_onlyLimitedInfo.creationDate) > 0) {
                    if (post_onlyLimitedInfo.postTypeId.equals("2")) {
                        cm.totalAnswers++;
                    } else if (post_onlyLimitedInfo.postTypeId.equals("1")) {
                        cm.totalQuestions++;
                    }
                }
            }
            if ((cm.totalAnswers + cm.totalQuestions) > 0) {
                cm.traditional_z_score = (cm.totalAnswers - cm.totalQuestions) / Math.sqrt(cm.totalAnswers + cm.totalQuestions);
            }
        }
    }

    //issTI,iss.createdAt, soPostsOfThisCommunityMemberAL, posts1ById, cm, p, maximumNumberOfPreviousAssignmentsForAMemberInThisProject
    public static void calculateZScores(IssueTextualInformation issTI, String issDate, ArrayList<SOPost> soPostsOfThisCommunityMemberAL, TreeMap<String, String[]> posts1ById, CommunityMember cm, int p, int maximumNumberOfPreviousAssignmentsForAMemberInThisProject) {
        if (soPostsOfThisCommunityMemberAL != null) {
            for (int i = 0; i < soPostsOfThisCommunityMemberAL.size(); i++) {
                SOPost post_onlyLimitedInfo = soPostsOfThisCommunityMemberAL.get(i);
                if (issDate.compareTo(post_onlyLimitedInfo.creationDate) > 0) {
                    String originalPostTypeId = post_onlyLimitedInfo.postTypeId;
                    SOPost post_completeInfo = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(posts1ById.get(post_onlyLimitedInfo.id));
                    if (post_onlyLimitedInfo.postTypeId.equals("2")) {
                        post_completeInfo = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(posts1ById.get(post_onlyLimitedInfo.parentId));
                    }
                    String[] tags = splitAnStringIncludingAllTagsToStringArrayOfTags(post_completeInfo.tags);
                    for (int j = 0; j < tags.length; j++) {
                        //只匹配缺陷报告的标题和文本textIsMatchedWithTagOrNot(issTI.projectLanguage, tags[j]) ||textIsMatchedWithTagOrNot(issTI.projectDescription, tags[j]) ||
                        if (textIsMatchedWithTagOrNot(issTI.issueTitle, tags[j]) ||
                                textIsMatchedWithTagOrNot(issTI.issueBody, tags[j])) {
                            if (originalPostTypeId.equals("2")) {
                                cm.totalAnswers++;
                            } else if (originalPostTypeId.equals("1")) {
                                cm.totalQuestions++;
                            }
                            break;//一条post只需要成功匹配一个tag就行
                        }
                    }
                }
            }
            if ((cm.totalAnswers + cm.totalQuestions) > 0) {
                cm.traditional_z_score = (cm.totalAnswers - cm.totalQuestions) / Math.sqrt(cm.totalAnswers + cm.totalQuestions);
            }
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------------------------------------------

    //在作者的原码基础上计算AnswerNum,Z-score,自己的加权方法,SSA-Z-score的计算在eclipse


    public static void main(String[] args) {
//Was run successfully:
//		triage1_basedOnSimpleIntersectionOfIssueTextualFieldsInGHAndTagsInSO(Constants.DATASET_DIRECTORY_COMMUNITY, "sortedCommunity", "communitiesSummary.tsv",
//				Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "projects2-Cleaned-ownerIdReplacedWithLogin-mergedTwoColumns.tsv",
//				Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "issues2.tsv",
//				Constants.DATASET_DIRECTORY_GH_AND_SO_Mixed_TOGETHER, "Posts-madeByCommunityMembers.tsv",
//				5, //top n number of members (FE'LAN! Ba'dan 10)
//				Constants.DATASET_DIRECTORY_TRIAGE_RESULTS, "triage1_result17_community",
//				100000, Constants.THIS_IS_REAL);

        //Triaging all communities:
        //Was run successfully:
//		triage1_basedOnSimpleIntersectionOfIssueTextualFieldsInGHAndTagsInSO(Constants.DATASET_DIRECTORY_COMMUNITY, "communitiesOf20TopProjects", "communitiesSummary4-top 500 projects of community25 - Top20ProjectsWithMostNumberOfAssignees.tsv",
//				Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "projects2-Cleaned-ownerIdReplacedWithLogin-mergedTwoColumns.tsv",
//				Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "issues2.tsv",
//				Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, "Posts-madeByCommunityMembers-20Projects.tsv",
//				20, //top n number of members (FE'LAN! Ba'dan 10)
//				Constants.DATASET_DIRECTORY_TRIAGE_RESULTS, "triage1_result21_test_community",
//				100000, Constants.THIS_IS_REAL);

        //Triaging Only one community (25), and using the generic folder (Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING):
//		triage1_basedOnSimpleIntersectionOfIssueTextualFieldsInGHAndTagsInSO(Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, "communitiesOf20TopProjects", "communitiesSummary4-top 500 projects of community25 - Top20ProjectsWithMostNumberOfAssignees.tsv",
//		Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, "projects-top20.tsv",
//		Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, "issues2-forTop20Projects.tsv",
//		Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, "Posts-madeByCommunityMembers-top20Projects.tsv",
//		20, //top n number of members (FE'LAN! Ba'dan 10)
//		Constants.DATASET_DIRECTORY_TRIAGE_RESULTS, "triage1_result21_community",
//		100000, Constants.THIS_IS_REAL);

        //Was run successfully:
        //Triaging Only one community (25), and using the generic folder (Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING):
//		triage2_basedOnMultipleCriteria(Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, "communitiesOf20TopProjects25.tsv", "communitiesSummary4-top 500 projects of community25 - Top20ProjectsWithMostNumberOfAssignees.tsv",
//		Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, "projects-top20.tsv",
//		Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, "issues2-forTop20Projects.tsv",
//		Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, "Posts-madeByCommunityMembers-top20Projects.tsv",
//		20, //top n number of members (FE'LAN! Ba'dan 10)
//		Constants.DATASET_DIRECTORY_TRIAGE_RESULTS, "triage3_result5-WR_recency_zScore-tunningWithDifferentAttempts.tsv",
//		100000, Constants.THIS_IS_REAL);

        //Main Run (17 projects):
//		for (int i=0; i<1; i++)
//			triage2_basedOnMultipleCriteria(Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, "communitiesOf17Projects.tsv", "communitiesSummary(17Projects).tsv",
//			Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, "projects-top20.tsv",
//			Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, "issues2-forTop20Projects.tsv",
//			Constants.DATASET_DIRECTORY_DATASETS_FOR_TRIAGING, "Posts-madeByCommunityMembers-top20Projects.tsv",
//			20,
//			Constants.DATASET_DIRECTORY_TRIAGE_RESULTS, "17Projects.tsv",
//			100000, Constants.THIS_IS_REAL);

        //Test Run (3 projects):
        for (int i = 0; i < 1; i++)
            triage2_basedOnMultipleCriteria(Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND, "communitiesOf3Projects.tsv", "communitiesSummary(3Projects).tsv",
                    Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND, "projects-top20.tsv",
                    Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND, "issues2-forTop20Projects.tsv",
                    Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND, "Posts-madeByCommunityMembers-top20Projects.tsv",
                    20,
                    Constants.DATASET_DIRECTORY_RECOMMEND_RESULTS, "3Projects.tsv",
                    100000, Constants.THIS_IS_REAL);

    }//main().

}

