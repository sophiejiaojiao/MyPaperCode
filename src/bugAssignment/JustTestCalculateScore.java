package bugAssignment;

import java.io.FileWriter;

import java.util.*;

import data.*;

public class JustTestCalculateScore {
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
                    communityInputPath, communitiesSummaryInputTSV, 1, 3, Constants.ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, Constants.THIS_IS_REAL, 1);
            //Now, read all columns of projects and their members:
            TreeMap<String, String[]> projects = TSVManipulations.readUniqueKeyAndItsValueFromTSV(
                    projectsInputPath, projectsInputTSV, null, 2, 9, Constants.ALL, Constants.ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval * 20, Constants.THIS_IS_REAL, 2);
            //Read info of all posts made by all community members by id: (total: ?)
            TreeMap<String, String[]> posts1ById = TSVManipulations.readUniqueKeyAndItsValueFromTSV(
                    postsOfCommunityMembersInputPath, postsOfCommunityMembersInputTSV, null, 0, 9, "0$1$2$3$4$5", Constants.ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval * 10, testOrReal, 3);

            //Read info of all posts made by all community members (by field ownerUserId): (total: 3,642,245)
            TreeMap<String, ArrayList<String[]>> posts2ByOwnerId = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
                    postsOfCommunityMembersInputPath, postsOfCommunityMembersInputTSV, null, 2, Constants.SortOrder.DEFAULT_FOR_STRING, 9, "0$1$2$3$6", Constants.ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval * 10, testOrReal, 4);
            //Reading assigned issues in all communities (projects):
            TreeMap<String, ArrayList<String[]>> ownerLoginAndProjectName_and_allAssignedIssuesInThisProject = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
                    assignedIssuesInputPath, assignedIssuesInputTSV, null, 1, Constants.SortOrder.DEFAULT_FOR_STRING, 11, "0$5$6$8$9$10", Constants.ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval * 5, testOrReal, 5);
            //fillStopWords();
            //Reading the community file:
            TreeMap<String, ArrayList<String[]>> ownerLoginAndProjectName_and_ItsCommunityMembers = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
                    communityInputPath, communityInputTSVFileName, null, 1, Constants.SortOrder.DEFAULT_FOR_STRING, 6, Constants.ALL, Constants.ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, 6);

            //Starting from the top numberOfProjectmembers, and writing triage results in output files:
            System.out.println("7- Triaging! And writing the precision in output file (" + triageOutputFileName + "):");
            FileWriter writer = new FileWriter(triageResultsOututPath + "\\" + triageOutputFileName);

            String output = "mySQLProjectId\townerLogin/projName\t#ofCommunityMembers\tissueId\t"
                    + "assigneeSOId\tassigneeGHLogin\tassigneeGHMySQLId\tassigneeGHMongoDBId\t"
                    + "AnswerNumRank\tAnswerNumMAP\tZ-scoreRank\tZ-scoreMAP\n";
            writer.append(output);

            Date d0 = new Date();
            int i = 0, projectsTriaged = 0;
            //循环每一个项目
            for (String anOwnerLoginAndProjectName : ownerLoginAndProjectNames) {
                if (ownerLoginAndProjectName_and_ItsCommunityMembers.containsKey(anOwnerLoginAndProjectName)) {
                    System.out.println("    - Proj " + (i + 1) + ") Started triaging in project \"" + anOwnerLoginAndProjectName + "\"");
                    Date d1 = new Date();
                    //Reading community members:
                    ArrayList<CommunityMember> cmAL = ProvideData.copyCommunityFromArrayListOfStringArray(ownerLoginAndProjectName_and_ItsCommunityMembers.get(anOwnerLoginAndProjectName));
                    ProvideData.initializeRandomScores(cmAL);

//                    ProvideData.calculateTraditionalZ_ScoreParameters(cmAL, posts2ByOwnerId);

                    System.out.println("        Total # of community members: " + cmAL.size());
                    Project aProject = ProvideData.copyProjectFromStringArray(projects.get(anOwnerLoginAndProjectName));
                    //Triage all assigned issues in this project:
                    ArrayList<Issue> issAL = ProvideData.copyIssuesFromArrayListOfStringArray_OnlyThereAreSomeFields3(ownerLoginAndProjectName_and_allAssignedIssuesInThisProject.get(anOwnerLoginAndProjectName));
                    Collections.sort(issAL);
                    System.out.println("        Total # of issues: " + issAL.size());
                    //循环每一份待修复的缺陷报告
                    for (int p = 0; p < issAL.size(); p++) {
                        Issue iss = issAL.get(p);
                        IssueTextualInformation issTI = new IssueTextualInformation(aProject.language, aProject.description, iss.labels, iss.title, iss.body);
                        CommunityMember communityMemberThatHasBeenAssignedToThisIssue = getPointerToCommunityMemberOfGHLogin(cmAL, iss.assigneeLogin);
                        if (communityMemberThatHasBeenAssignedToThisIssue != null) {
                            ProvideData.initializeIntersectionWithBugsScores(cmAL);
                            //循环每一位候选修复者
                            for (int k = 0; k < cmAL.size(); k++) {
                                //提取该候选修复者的所有Posts
                                CommunityMember cm = cmAL.get(k);
                                ArrayList<SOPost> soPostsOfThisCommunityMemberAL;
                                if (posts2ByOwnerId.containsKey(cm.SOId)) {
                                    //post2ById存放着Id,PostTypeId,OwnerUserId,ParentId,CreationDate
                                    soPostsOfThisCommunityMemberAL = ProvideData.copyPostContentsFromArrayListOfStringArray_OnlyThereAreSomeFields1(posts2ByOwnerId.get(cm.SOId));

                                    if (soPostsOfThisCommunityMemberAL != null) {
                                        for (int j = 0; j < soPostsOfThisCommunityMemberAL.size(); j++) {
                                            SOPost post_onlyLimitedInfo = soPostsOfThisCommunityMemberAL.get(j);
                                            if (iss.createdAt.compareTo(post_onlyLimitedInfo.creationDate) > 0) {
                                                String originalPostTypeId = post_onlyLimitedInfo.postTypeId;
                                                //posts1ById存放着Id,PostTypeId,OwnerUserId,ParentId,Score,Tags
                                                SOPost post_completeInfo = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(posts1ById.get(post_onlyLimitedInfo.id));
                                                if (post_onlyLimitedInfo.postTypeId.equals("2")) {
                                                    post_completeInfo = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(posts1ById.get(post_onlyLimitedInfo.parentId));
                                                }
                                                String[] tags = splitAnStringIncludingAllTagsToStringArrayOfTags(post_completeInfo.tags);
                                                for (int t = 0; t < tags.length; t++) {
                                                    //textIsMatchedWithTagOrNot(issTI.projectLanguage, tags[t]) ||
//                                                    textIsMatchedWithTagOrNot(issTI.projectDescription, tags[t]) ||
                                                    if (
                                                            textIsMatchedWithTagOrNot(issTI.issueTitle, tags[t]) ||
                                                            textIsMatchedWithTagOrNot(issTI.issueBody, tags[t])
                                                            ) {

                                                        if (originalPostTypeId.equals("2")) {
                                                            cm.answerNum++;
                                                            cm.totalAnswers++;
                                                        } else if (originalPostTypeId.equals("1")) {
                                                            cm.totalQuestions++;
                                                        }
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        if ((cm.totalAnswers + cm.totalQuestions) > 0) {
                                            cm.traditional_z_score = (cm.totalAnswers - cm.totalQuestions) / Math.sqrt(cm.totalAnswers + cm.totalQuestions);
                                        }
                                    }
                                }
                            }//for (i.
                            //****************************************添加计算rank的方法开始****************************
                            HashMap<String, Integer> assigneeAnswerNumRank = new HashMap<String, Integer>();
                            rankAssigneeByAnswerNumScore(communityMemberThatHasBeenAssignedToThisIssue, cmAL, assigneeAnswerNumRank);
                            HashMap<String, Integer> assigneeZScoreRank = new HashMap<String, Integer>();
                            rankAssigneeByZScore(communityMemberThatHasBeenAssignedToThisIssue, cmAL, assigneeZScoreRank);
                            //****************************************添加计算rank的方法结束****************************
                            //*************注释掉**********Creating output string and writing it**************:
                            output = aProject.projectMySQLId + "\t" + aProject.ownerLoginAndProjectName + "\t" + cmAL.size() + "\t" + iss.id + "\t"
                                    + communityMemberThatHasBeenAssignedToThisIssue.SOId + "\t" + communityMemberThatHasBeenAssignedToThisIssue.ghLogin
                                    + "\t" + communityMemberThatHasBeenAssignedToThisIssue.ghMySQLId + "\t" + communityMemberThatHasBeenAssignedToThisIssue.ghMongoDBId + "\t"
                                    + assigneeAnswerNumRank.get(communityMemberThatHasBeenAssignedToThisIssue.ghLogin) + "\t"
                                    + 1.0 / assigneeAnswerNumRank.get(communityMemberThatHasBeenAssignedToThisIssue.ghLogin) + "\t"
                                    + assigneeZScoreRank.get(communityMemberThatHasBeenAssignedToThisIssue.ghLogin) + "\t"
                                    + 1.0 / assigneeZScoreRank.get(communityMemberThatHasBeenAssignedToThisIssue.ghLogin) + "\n";
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
            System.out.println("End of triaging.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CommunityMember getPointerToCommunityMemberOfGHLogin(ArrayList<CommunityMember> communityMembers, String assigneeLogin) {
        int resultIndex = -1;
        for (int i = 0; i < communityMembers.size(); i++) {
            if (communityMembers.get(i).ghLogin.equals(assigneeLogin)) {
                resultIndex = i;
                break;
            }
        }
        if (resultIndex != -1) {
            CommunityMember cm = communityMembers.get(resultIndex);
            return cm;
        } else {
            return null;
        }
    }

    //***************************只根据calculateScore进行排名***************************************
    public static void rankAssigneeByAnswerNumScore(CommunityMember communityMemberThatHasBeenAssignedToThisIssue, ArrayList<CommunityMember> cmAL, HashMap<String, Integer> assigneeAnswerNumRank) {
        Random random = new Random();
        int numOfMemWithGreaterScore = 0;
        int numOfMemWithEqualScore = 0;
        double scoresOfTheRealAssignee = communityMemberThatHasBeenAssignedToThisIssue.answerNum;
        for (int k = 0; k < cmAL.size(); k++) {
            CommunityMember cm = cmAL.get(k);
            if (cm.answerNum > scoresOfTheRealAssignee) {
                numOfMemWithGreaterScore++;
            } else if (cm.answerNum == scoresOfTheRealAssignee) {
                numOfMemWithEqualScore++;
            }
        }
        int rank = numOfMemWithGreaterScore + random.nextInt(numOfMemWithEqualScore) + 1;
//        int rank = numOfMemWithGreaterScore + 1;
        assigneeAnswerNumRank.put(communityMemberThatHasBeenAssignedToThisIssue.ghLogin, rank);
    }

    public static void rankAssigneeByZScore(CommunityMember communityMemberThatHasBeenAssignedToThisIssue, ArrayList<CommunityMember> cmAL, HashMap<String, Integer> assigneeZScoreRank) {
        Random random = new Random();
        int numOfMemWithGreaterScore = 0;
        int numOfMemWithEqualScore = 0;
        double scoresOfTheRealAssignee = communityMemberThatHasBeenAssignedToThisIssue.traditional_z_score;
        for (int k = 0; k < cmAL.size(); k++) {
            CommunityMember cm = cmAL.get(k);
            if (cm.traditional_z_score > scoresOfTheRealAssignee) {
                numOfMemWithGreaterScore++;
            } else if (cm.traditional_z_score == scoresOfTheRealAssignee) {
                numOfMemWithEqualScore++;
            }
        }
        int rank = numOfMemWithGreaterScore + random.nextInt(numOfMemWithEqualScore) + 1;
//        int rank = numOfMemWithGreaterScore + 1;
        assigneeZScoreRank.put(communityMemberThatHasBeenAssignedToThisIssue.ghLogin, rank);
    }

    public static String[] splitAnStringIncludingAllTagsToStringArrayOfTags(String tagsString) {
        tagsString = tagsString.replaceFirst("\\[", "");
        tagsString = tagsString.replaceFirst("\\]", "");
        tagsString = tagsString.toLowerCase();
        String[] result = tagsString.split(Constants.SEPARATOR_FOR_ARRAY_ITEMS);
        return result;
    }

    public static boolean textIsMatchedWithTagOrNot(String str, String tag) {
//		if (str.equals(tag) || str.startsWith(tag + " ") || str.endsWith(" " + tag) || str.contains(" " + tag + " ") || str.contains(" " + tag + ".")) //2222222222 needs to be fixed: "lucene-4870" and "open source" ("open-source" in SO)
        //We don't need the first three conditions because we added space before and after str in the calling function.
        if (str.contains(" " + tag + " ") || str.equals(tag) || str.contains(" " + tag + ".")) //2222222222 needs to be fixed: "lucene-4870" and "open source" ("open-source" in SO)
            return true;
        else
            return false;
    }

    //该代码是测试answerNum,Z-score
    //-----------------------------------------------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) {
        //测试3P
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
                "testAnswerNumAndZ-scoreFor3Projects.tsv",
                100000, Constants.THIS_IS_REAL);

        //测试17P
//        assign_basedOnOneCriteria(
//                Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                "communitiesOf17Projects.tsv",
//                "communitiesSummary(17Projects).tsv",
//                Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                "projects-top20.tsv",
//                Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                "issues2-forTop20Projects.tsv",
//                Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                "Posts-madeByCommunityMembers-top20Projects.tsv", 20,
//                Constants.DATASET_DIRECTORY_RECOMMEND_RESULTS,
//                "testAnswerNumAndZ-scoreFor17Projects.tsv",
//                100000, Constants.THIS_IS_REAL);

//        //测试20P
//        assign_basedOnOneCriteria(
//                Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                "communitiesOf20Projects.tsv",
//                "communitiesSummary(20Projects).tsv",
//                Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                "projects-top20.tsv",
//                Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                "issues2-forTop20Projects.tsv",
//                Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                "Posts-madeByCommunityMembers-top20Projects.tsv", 20,
//                Constants.DATASET_DIRECTORY_RECOMMEND_RESULTS,
//                "testAnswerNumAndZ-scoreFor17Projects.tsv",
//                100000, Constants.THIS_IS_REAL);
    }
}
