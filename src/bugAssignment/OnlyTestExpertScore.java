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

public class OnlyTestExpertScore {
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

                    int scoreOfThePost = Integer.parseInt(post_completeInfo.Score); //333333333333

                    if (post_onlyLimitedInfo.postTypeId.equals("2"))//i.e., it is an answer, so fetch its related question:
                        post_completeInfo = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(allPosts1ById.get(post_onlyLimitedInfo.parentId));
                    String[] tags = splitAnStringIncludingAllTagsToStringArrayOfTags(post_completeInfo.tags);
/*
                    //**************************************关键字加权开始**************************************
                    HashMap<String, Double> wOfTags = calculateWeightOfTagInIssueText(tags, issTI);
                    double totalWOfTags = 0;
                    for (int j = 0; j < tags.length; j++) {
                        if (textIsMatchedWithTagOrNot(issTI.projectLanguage, tags[j]) || textIsMatchedWithTagOrNot(issTI.projectDescription, tags[j])
                                || textIsMatchedWithTagOrNot(issTI.issueTitle, tags[j]) || textIsMatchedWithTagOrNot(issTI.issueBody, tags[j])) {
                            if (originalPostTypeId.equals("2")) {
                                cm.intersection_A_score = cm.intersection_A_score + (scoreOfThePost + 1) * wOfTags.get(tags[j]);
                            } else if (originalPostTypeId.equals("1")) {
                                totalWOfTags = totalWOfTags + wOfTags.get(tags[j]);
                            }
                        }
                    }
                    int tempScoreOfThePost;
                    if (scoreOfThePost < 0) {
                        tempScoreOfThePost = 0;
                    } else {
                        tempScoreOfThePost = scoreOfThePost;
                    }
                    cm.intersection_Q_score = cm.intersection_Q_score + totalWOfTags / (tempScoreOfThePost + 1.0);
                    //**************************************关键字加权结束**************************************
*/
                    int numberOfMatchedTags = 0;
                    for (int j = 0; j < tags.length; j++) {//For all tags
                        if (textIsMatchedWithTagOrNot(issTI.projectLanguage, tags[j]) ||
                                textIsMatchedWithTagOrNot(issTI.projectDescription, tags[j]) ||
                                textIsMatchedWithTagOrNot(issTI.issueTitle, tags[j]) ||
                                textIsMatchedWithTagOrNot(issTI.issueBody, tags[j])
                                ) {
                            cm.intersection_AQ++;
                            cm.intersection_AQ_score = cm.intersection_AQ_score + scoreOfThePost + 1; //considering the answer a score, then each upvote adds an score to that.
                            if (originalPostTypeId.equals("2")) {//i.e., this post is an answer:
                                cm.intersection_A++;
//                                作者的代码是加上点赞数
                                cm.intersection_A_score = cm.intersection_A_score + scoreOfThePost + 1; //considering the answer a score, then each upvote adds an score to that.
                                //测试不加点赞数
//                                cm.intersection_A_score = cm.intersection_A_score + 1; //considering the answer a score, then each upvote adds an score to that.
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
                    /*作者的代码是加上点赞数*/
                    cm.intersection_Q_score = cm.intersection_Q_score + numberOfMatchedTags / (tempScoreOfThePost + 1.0);
                    /*测试不加点赞数*/
                    cm.intersection_Q_score = cm.intersection_Q_score + numberOfMatchedTags / 1.0;
                }//if issueD....
            }//for (i.
            //z-score that considers votes of Q/A:
            //**************************设置u的值***************************
            cm.intersection_Q_score = NF * cm.intersection_Q_score;
//            cm.intersection_Q_score = 1 * cm.intersection_Q_score;
            //**************************************************************
            if (cm.intersection_A_score + cm.intersection_Q_score > 0)
                cm.intersection_z_score = (cm.intersection_A_score - cm.intersection_Q_score) / Math.sqrt(cm.intersection_A_score + cm.intersection_Q_score);
            else
                cm.intersection_z_score = 0;
//			System.out.println("SOId: " + cm.SOId + "\t" + "QS: " + Constants.floatFormatter.format(cm.intersection_Q_score) + "\t\tAS: " + Constants.floatFormatter.format(cm.intersection_A_score) + "\t\tSSA Z-score: " + Constants.floatFormatter.format(cm.intersection_z_score));
        }//if.
    }//calculateScores().

    //****************************计算关键字的权值的方法*************************************
    public static HashMap<String, Double> calculateWeightOfTagInIssueText(String[] tags, IssueTextualInformation issTI) {
        HashMap<String, Double> hmCount = new HashMap<String, Double>();
        HashMap<String, Double> hmWeight = new HashMap<String, Double>();
        //用空格替换标点符号
        String issText = (issTI.projectLanguage + issTI.projectDescription + issTI.issueLabels + issTI.issueTitle + issTI.issueBody)
                .replaceAll("[,\\.，。\\!！《》、;\\:；：\\s]", " ");
        //空格分词法
        String[] issWord = issText.split(" ");
        double countTotalTag = 0;
        //定义匹配成功的标签的平均数，即匹配的总数/标签类别数
        double meanTagNumOfEveryTag = 0;
        for (int i = 0; i < tags.length; i++) {
            double count = 0;
            for (int j = 0; j < issWord.length; j++) {
                if (tags[i].equals(issWord[j])) {
                    count++;
                    countTotalTag++;
                }
            }
            //存放每个tag在issue中出现的次数
            hmCount.put(tags[i], count);
        }
        meanTagNumOfEveryTag = countTotalTag / tags.length;

        double weightOfTag;
        for (Map.Entry<String, Double> entry : hmCount.entrySet()) {
            //以平均次数等价作者的1次，然后看每个标签与平均次数的离散程度
            weightOfTag = ((entry.getValue() - meanTagNumOfEveryTag) / meanTagNumOfEveryTag) + 1;
            hmWeight.put(entry.getKey(), weightOfTag);
        }
//        测标签个数平均值算权值
//        return hmWeight;
        //测标签个数算权值
        return hmCount;
    }

    //************************************************************************************
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
    public static void assign_basedOnOneCriteria(String communityInputPath, String communityInputTSVFileName, String communitiesSummaryInputTSV,
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
                    + "calculateScore\trank\tMAP\t\n";
            writer.append(output);

            Date d0 = new Date();
            int i = 0, projectsTriaged = 0;
            for (String anOwnerLoginAndProjectName : ownerLoginAndProjectNames) {
                if (ownerLoginAndProjectName_and_ItsCommunityMembers.containsKey(anOwnerLoginAndProjectName)) {//This condition is just for double checking the existence of the records of this project (that has beed read from communitiesSummary) with the info in the community file, and then triaging them:
                    System.out.println("    - Proj " + (i + 1) + ") Started triaging in project \"" + anOwnerLoginAndProjectName + "\"");
                    Date d1 = new Date();
                    //Reading community members:
                    ArrayList<CommunityMember> cmAL = ProvideData.copyCommunityFromArrayListOfStringArray(ownerLoginAndProjectName_and_ItsCommunityMembers.get(anOwnerLoginAndProjectName));
                    ProvideData.initializeRandomScores(cmAL);
                    ProvideData.calculateTraditionalZ_ScoreParameters(cmAL, posts2ByOwnerId);
                    int maximumNumberOfPreviousAssignmentsForAMemberInThisProject = 0;
                    System.out.println("        Total # of community members: " + cmAL.size());
                    Project aProject = ProvideData.copyProjectFromStringArray(projects.get(anOwnerLoginAndProjectName));
                    //Triage all assigned issues in this project:
                    ArrayList<Issue> issAL = ProvideData.copyIssuesFromArrayListOfStringArray_OnlyThereAreSomeFields3(ownerLoginAndProjectName_and_allAssignedIssuesInThisProject.get(anOwnerLoginAndProjectName));
                    Collections.sort(issAL);
                    System.out.println("        Total # of issues: " + issAL.size());

                    ArrayList<String> allAssignees = new ArrayList<String>();
                    extractAndCopyAssigneeValues(issAL, allAssignees);

                    for (int p = 0; p < issAL.size(); p++) {
                        Issue iss = issAL.get(p);
                        IssueTextualInformation issTI = new IssueTextualInformation(aProject.language, aProject.description, iss.labels, iss.title, iss.body);
                        CommunityMember communityMemberThatHasBeenAssignedToThisIssue = getPointerToCommunityMemberOfGHLogin(cmAL, iss.assigneeLogin);
                        //Checking if the assignee is from the community (the only case that triaging means. Otherwise triaging is useless because the issue is assigned to neither of the community members who we are to rank and recommend them):
                        if (communityMemberThatHasBeenAssignedToThisIssue != null) {//Triage:
                            ProvideData.initializeIntersectionWithBugsScores(cmAL);

                            double NF = calculateHarmonicMeanOfUpVotesForQuestionsTaggedWithAtLeastOneOfTagsInMatchedTags_SO_b(issTI, posts1ById);
                            //Iterating over all community members, and calculating their scores for this issue:
                            for (int k = 0; k < cmAL.size(); k++) {
                                //Identifying the posts of this community member:
                                CommunityMember cm = cmAL.get(k);
                                ArrayList<SOPost> soPostsOfThisCommunityMemberAL = null;
                                if (posts2ByOwnerId.containsKey(cm.SOId))
                                    soPostsOfThisCommunityMemberAL = ProvideData.copyPostContentsFromArrayListOfStringArray_OnlyThereAreSomeFields1(posts2ByOwnerId.get(cm.SOId));
                                calculateScores(issTI, iss.createdAt, soPostsOfThisCommunityMemberAL, posts1ById, cm, p, maximumNumberOfPreviousAssignmentsForAMemberInThisProject, NF);
                            }//for (i.
                            HashMap<String, Integer> assigneeRank = new HashMap<String, Integer>();
                            rankAssigneeByWeightScore(communityMemberThatHasBeenAssignedToThisIssue, cmAL, assigneeRank);

                            output = aProject.projectMySQLId + "\t" + aProject.ownerLoginAndProjectName + "\t" + cmAL.size() + "\t" + iss.id + "\t"
                                    + communityMemberThatHasBeenAssignedToThisIssue.SOId + "\t" + communityMemberThatHasBeenAssignedToThisIssue.ghLogin + "\t"
                                    + communityMemberThatHasBeenAssignedToThisIssue.ghMySQLId + "\t" + communityMemberThatHasBeenAssignedToThisIssue.ghMongoDBId + "\t"
                                    + communityMemberThatHasBeenAssignedToThisIssue.intersection_z_score + "\t"
                                    + assigneeRank.get(communityMemberThatHasBeenAssignedToThisIssue.ghLogin) + "\t"
                                    + 1.0 / assigneeRank.get(communityMemberThatHasBeenAssignedToThisIssue.ghLogin) + "\n";
                            writer.append(output);
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
    }

    public static void rankAssigneeByWeightScore(CommunityMember communityMemberThatHasBeenAssignedToThisIssue, ArrayList<CommunityMember> cmAL, HashMap<String, Integer> assigneeRank) {
        Random random = new Random();
        int numOfMemWithGreaterScore = 0;
        int numOfMemWithEqualScore = 0;
        double scoresOfTheRealAssignee = communityMemberThatHasBeenAssignedToThisIssue.intersection_z_score;
        for (int k = 0; k < cmAL.size(); k++) {
            CommunityMember cm = cmAL.get(k);
            if (cm.intersection_z_score > scoresOfTheRealAssignee) {
                numOfMemWithGreaterScore++;
            } else if (cm.intersection_z_score == scoresOfTheRealAssignee) {
                numOfMemWithEqualScore++;
            }
        }
        //nextInt(x)是0到x，不包括x
        int rank = numOfMemWithGreaterScore + random.nextInt(numOfMemWithEqualScore) + 1;
        assigneeRank.put(communityMemberThatHasBeenAssignedToThisIssue.ghLogin, rank);
    }

    //该代码是测试只加权或不加权，完全不考虑时效性
    //-----------------------------------------------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) {

        //Test Run (3 projects):
        assign_basedOnOneCriteria(
                Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
                "communitiesOf3Projects.tsv",
                "communitiesSummary(3Projects).tsv",
                Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
                "projects-top20.tsv",
                Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
                "issues2-forTop20Projects.tsv",
                Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
                "Posts-madeByCommunityMembers-top20Projects.tsv", 20,
                Constants.DATASET_DIRECTORY_RECOMMEND_RESULTS,
                "testOnlyWeightFor3Projects.tsv",
                100000, Constants.THIS_IS_REAL);

    }//main().

}
