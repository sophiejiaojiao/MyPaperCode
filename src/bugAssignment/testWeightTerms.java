package bugAssignment;

import data.*;

import java.io.FileWriter;
import java.util.*;

public class testWeightTerms {
    public static void main(String[] args) {
        assign_basedOnMultipleCriteria(Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND, "communitiesOf3Projects.tsv", "communitiesSummary(3Projects).tsv",
                Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND, "projects-top20.tsv",
                Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND, "issues2-forTop20Projects.tsv",//"issues2-forTop20Projects.tsv"
                Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND, "Posts-madeByCommunityMembers-top20Projects.tsv",
                20,
                Constants.DATASET_DIRECTORY_RECOMMEND_RESULTS, "testTimeForJuliaLang.tsv", 10000,
                Constants.THIS_IS_REAL);
    }

    public static void assign_basedOnMultipleCriteria(String communityInputPath, String communityInputTSVFileName, String communitiesSummaryInputTSV,
                                                      String projectsInputPath, String projectsInputTSV, String assignedIssuesInputPath, String assignedIssuesInputTSV,
                                                      String postsOfCommunityMembersInputPath, String postsOfCommunityMembersInputTSV,
                                                      int topXProjectsToAssign, String assignResultsOutputPath, String assignOutputFileName,
                                                      int showProgressInterval, long testOrReal) {
        try {
            //存放待测试的所有项目的名称，size为项目数量
            HashSet<String> ownerLoginAndProjectNames = TSVManipulations.readUniqueFieldFromTSV(
                    communityInputPath, communitiesSummaryInputTSV, 1, 3, Constants.ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, Constants.THIS_IS_REAL, 1
            );
            //存放项目名称及对应属性，size为projects文件中的项目数
            TreeMap<String, String[]> projects = TSVManipulations.readUniqueKeyAndItsValueFromTSV(
                    projectsInputPath, projectsInputTSV, null, 2, 9, Constants.ALL, Constants.ConditionType.NO_CONDITION.NO_CONDITION, 0, "", 0, "", showProgressInterval * 20, Constants.THIS_IS_REAL, 2
            );
            //存放posts的前6列数据，key为post的id，value为id、postTypeId、OwnerUserId、ParentId、Score、Tags，主要是截取需要的post信息
            TreeMap<String, String[]> posts1ById = TSVManipulations.readUniqueKeyAndItsValueFromTSV(
                    postsOfCommunityMembersInputPath, postsOfCommunityMembersInputTSV, null, 0, 9, "0$1$2$3$4$5", Constants.ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval * 10, testOrReal, 3
            );
            //存放post用户及其post的关系，即一个用户对应的所有post，信息包括post的id、postTypeId、OwnerUserId、ParentId、CreationDate
            TreeMap<String, ArrayList<String[]>> posts2ByOwnerId = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
                    postsOfCommunityMembersInputPath, postsOfCommunityMembersInputTSV, null, 2, Constants.SortOrder.DEFAULT_FOR_STRING, 9, "0$1$2$3$6", Constants.ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval * 10, testOrReal, 4
            );
            //存放每个项目对应的缺陷报告信息
            TreeMap<String, ArrayList<String[]>> ownerLoginAndProjectName_and_allAssignedIssuesInThisProject = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
                    assignedIssuesInputPath, assignedIssuesInputTSV, null, 1, Constants.SortOrder.DEFAULT_FOR_STRING, 11, "0$5$6$8$9$10", Constants.ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval * 5, testOrReal, 5
            );
            //存放每个项目对应的候选修复者的信息
            TreeMap<String, ArrayList<String[]>> ownerLoginAndProjectName_and_ItsCommunityMembers = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
                    communityInputPath, communityInputTSVFileName, null, 1, Constants.SortOrder.DEFAULT_FOR_STRING, 6, Constants.ALL, Constants.ConditionType.NO_CONDITION, 0, "", 0, "", showProgressInterval, testOrReal, 6
            );
            System.out.println("7- Assigning! And writing the socres in output file (" + assignOutputFileName + "):");
            FileWriter writer = new FileWriter(assignResultsOutputPath + "\\" + assignOutputFileName);
            String output = "mySQLProjectId\townerLogin/projName\t#ofCommunityMembers\tissueId\t"
                    + "assigneeSOId\tassigneeGHLogin\tassigneeGHMySQLId\tassigneeGHMongoDBId\t";
            for (int j = 0; j < Constants.TOTAL_NUMBER_OF_METRICS; j++)
                output = output + "aR" + j + ":tuning(" + Constants.highPrecisionFloatFormatter.format(Constants.Z_SCORE_COEFFICIENTS[j]) + ")\t";
            for (int j = 0; j < Constants.TOTAL_NUMBER_OF_METRICS; j++)
                output = output + "aRR" + j + ":tunning(" + Constants.highPrecisionFloatFormatter.format(Constants.Z_SCORE_COEFFICIENTS[j]) + ")\t";
            output = output + "projectLanguage\tprojectDescription\tissueTitle\tissueLabels\tissueBody\n";
            writer.append(output);
            Date d0 = new Date();
            int i = 0, projectsAssigned = 0;
            //依次循环每个项目进行分派
            for (String anOwnerLoginAndProjectName : ownerLoginAndProjectNames) {
                if (ownerLoginAndProjectName_and_ItsCommunityMembers.containsKey(anOwnerLoginAndProjectName)) {
                    System.out.println("    - Proj " + (i + 1) + ") Started assigning in project \"" + anOwnerLoginAndProjectName + "\"");
                    Date d1 = new Date();
                    //以JuliaLang/julia为例，cmAL的size=73，值为mySQLProjectId,ownerLogin/projName,SOId,ghLogin,ghMySQLId,ghMongoDBId
                    ArrayList<CommunityMember> cmAL = ProvideData.copyCommunityFromArrayListOfStringArray(ownerLoginAndProjectName_and_ItsCommunityMembers.get(anOwnerLoginAndProjectName));
                    ProvideData.initializeRandomScores(cmAL);
                    //该值是该项目当中所有成员分派到缺陷报告数量当中的最大值
                    int maximumNumberOfPreviousAssignmentsForAMemberInThisProject = 0;
                    System.out.println("        Total # of community members: " + cmAL.size());
                    //aProject里存放该项目的项目信息，共9个属性
                    Project aProject = ProvideData.copyProjectFromStringArray(projects.get(anOwnerLoginAndProjectName));
                    //issAL存放该项目的issue信息，共6个属性：issue的id，assigneeLogin,created_at,labels,title,body
                    ArrayList<Issue> issAL = ProvideData.copyIssuesFromArrayListOfStringArray_OnlyThereAreSomeFields3(ownerLoginAndProjectName_and_allAssignedIssuesInThisProject.get(anOwnerLoginAndProjectName));
                    //比较的是issue里面的时间，按照时间的大小递增
                    Collections.sort(issAL);
                    System.out.println("        Total # of issues: " + issAL.size());

                    ArrayList<String> allAssignees = new ArrayList<String>();
                    //allAssignees里面存放从该项目的所有缺陷报告（issAL）中获取的真实assigneeLogin
                    extractAndCopyAssigneeValues(issAL, allAssignees);
                    HashMap<String, Integer> allPreviousAssignees_unique = new HashMap<String, Integer>();

                    //依次循环每一条缺陷报告
                    for (int p = 0; p < issAL.size(); p++) {
                        Issue iss = issAL.get(p);
                        //提取待修复缺陷报告的文本信息，用于和tag进行匹配
                        IssueTextualInformation issTI = new IssueTextualInformation(aProject.language, aProject.description, iss.labels, iss.title, iss.body);
                        //communityMemberThatHasBeenAssignedToThisIssue表示待分派的这份缺陷报告对应的真实的修复者的信息，包括所属项目Id和名称，SOId，ghLogin等6个属性
                        CommunityMember communityMemberThatHasBeenAssignedToThisIssue = getPointerToCommunityMemberOfGHLogin(cmAL, iss.assigneeLogin);
                        //如果存在候选修复者当中有真实的修复者，这里排除离职或该修复者当前不可用的情况
                        if (communityMemberThatHasBeenAssignedToThisIssue != null) {
                            ProvideData.initializeIntersectionWithBugsScores(cmAL);
                            double NF = calculateHarmonicMeanOfUpVotesForQuestionsTaggedWithAtLeastOneOfTagsInMatchedTags_SO_b(issTI, posts1ById);
                            //System.out.println("NF:" + NF);
                            //循环每一个候选的修复者
                            for (int k = 0; k < cmAL.size(); k++) {
                                CommunityMember cm = cmAL.get(k);
                                ArrayList<SOPost> soPostsOfThisCommunityMemberAL = null;
                                //变量soPostsOfThisCommunityMemberAL存放该候选者的所有post信息
                                if (posts2ByOwnerId.containsKey(cm.SOId)) {
                                    soPostsOfThisCommunityMemberAL = ProvideData.copyPostContentsFromArrayListOfStringArray_OnlyThereAreSomeFields1(posts2ByOwnerId.get(cm.SOId));
                                }
                                /*
                                issTI表示待分派的缺陷报告的文本信息
                                iss.createdAt表示待分派缺陷报告的创建时间
                                soPostsOfThisCommunityMemberAL存放该候选者的所有post信息，有post的id，PostTypeId，OwnerUserId，ParentId，CreationDate
                                posts1ById存放所有post的相关信息，有post的id，PostTypeId，OwnerUserId，ParentId，Score，Tags
                                cm表示当前的候选者的用户信息
                                p表示当前是第几条缺陷报告，初始值为0
                                maximumNumberOfPreviousAssignmentsForAMemberInThisProject初始值为0
                                NF表示计算Q_Score的参数
                                 */
                                calculateScores(issTI, iss.createdAt, soPostsOfThisCommunityMemberAL, posts1ById, cm, p, maximumNumberOfPreviousAssignmentsForAMemberInThisProject, NF);
                            }

                            CommunityMembersRankingResult assigneeRank = new CommunityMembersRankingResult();
                            /*
                            cmAL表示该项目的所有成员信息
                            allAssignees表示该项目的所有缺陷报告对应的真实修复者的assigneeLogin
                            p表示第几条缺陷报告
                            allPreviousAssignees_unique表示。。。。。
                            communityMemberThatHasBeenAssignedToThisIssue表示待分派的这份缺陷报告对应的真实的修复者的信息，包括所属项目Id和名称，SOId，ghLogin等6个属性
                            assigneeRank表示修复者的排名（是真实修复者还是算法推荐的修复者？）
                             */
                            determineAssigneeRankRandomly(cmAL, allAssignees, p, allPreviousAssignees_unique, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank);
                            //对于第一条缺陷报告，allPreviousAssignees_unique值为0，因此进行else操作，将该缺陷报告的真实修复者名称存入allPreviousAssignees_unique
                            if (allPreviousAssignees_unique.containsKey(communityMemberThatHasBeenAssignedToThisIssue.ghLogin)) {
                                int tempNumber = allPreviousAssignees_unique.get(communityMemberThatHasBeenAssignedToThisIssue.ghLogin);
                                allPreviousAssignees_unique.put(communityMemberThatHasBeenAssignedToThisIssue.ghLogin, tempNumber + 1);
                            } else {
                                allPreviousAssignees_unique.put(communityMemberThatHasBeenAssignedToThisIssue.ghLogin, 1);
                            }
                            //输出真实修复者的排名结果
                            output = aProject.projectMySQLId + "\t" + aProject.ownerLoginAndProjectName + "\t" + cmAL.size() + "\t" + iss.id + "\t"
                                    + communityMemberThatHasBeenAssignedToThisIssue.SOId + "\t" + communityMemberThatHasBeenAssignedToThisIssue.ghLogin + "\t" + communityMemberThatHasBeenAssignedToThisIssue.ghMySQLId + "\t" + communityMemberThatHasBeenAssignedToThisIssue.ghMongoDBId + "\t";
                            for (int j = 0; j < Constants.TOTAL_NUMBER_OF_METRICS; j++) {
                                output = output + assigneeRank.differentRankings[j] + "\t";
                            }
                            for (int j = 0; j < Constants.TOTAL_NUMBER_OF_METRICS; j++) {
                                output = output + 1.0 / assigneeRank.differentRankings[j] + "\t";
                            }
                            output = output + aProject.language + "\t" + aProject.description + "\t" + iss.title + "\t" + iss.labels + "\t" + iss.body + "\n";
                            writer.append(output);
                            //numberOfAssignmentsUpToNow主要是用来统计该真实修复者真实的修复的缺陷报告数量，每个真实修复者都有该值
                            //maxium...用来统计该项目的缺陷报告分派结束后，获得最多缺陷报告的用户的报告数量
                            /*
                            分派第一条缺陷报告时，该缺陷报告对应的真实修复者A的则numberOfAssignment..从0增到1，此时maxiumNum...=0，因此maxiumNum=1
                            分派第二条缺陷报告时，如果该缺陷报告对应的真实修复者仍然是A，则numberOfAssignment..从1增到2，此时maxiumNum..=1，因此maxiumNum=2
                            如果分派第二条缺陷报告时，其对应的真实修复者是是B，则B的numberOfAssignment...从0增到1，此时maxiumNum..=1（第一行注释）,if条件为false，因此maxiumNum..仍然是=1
                             */
                            //表示该缺陷报告对应的真实修复者的缺陷报告被分派的数量加1，也就是说是为了统计每个真实修复者真实修复的缺陷报告数量
                            communityMemberThatHasBeenAssignedToThisIssue.numberOfAssignmentsUpToNow++;
                            //maxi...是为了统计在所有的真实修复者真实的修复的缺陷报告中的最大值是多少
                            if (communityMemberThatHasBeenAssignedToThisIssue.numberOfAssignmentsUpToNow > maximumNumberOfPreviousAssignmentsForAMemberInThisProject) {
                                maximumNumberOfPreviousAssignmentsForAMemberInThisProject = communityMemberThatHasBeenAssignedToThisIssue.numberOfAssignmentsUpToNow;
                            }
                        }
                    }
                    projectsAssigned++;
                    Date d2 = new Date();
                    System.out.println("    Finished in " + Constants.integerFormatter.format((d2.getTime() - d1.getTime()) / 1000) + " seconds (" + Constants.integerFormatter.format(((d2.getTime() - d1.getTime()) / 1000) / 60) + " minutes and " + Constants.integerFormatter.format(((d2.getTime() - d1.getTime()) / 1000) % 60) + " seconds).");
                    System.out.println("-------------------------------------------");
                    if (projectsAssigned >= topXProjectsToAssign) {
                        break;
                    }
                } else {
                    System.out.println("            Weird error: The communitesSummary file contains project \"" + anOwnerLoginAndProjectName + "\", but the community file does not! (maybe due to \"testOrReal\"=THIS_IS_A_TEST):");
                    break;
                }
                i++;
                if (i % showProgressInterval == 0) {
                    System.out.println("    number of numberOfMembers (including all projects in that range) examined: " + Constants.integerFormatter.format(i));
                }
                if (testOrReal > Constants.THIS_IS_REAL) {
                    if (i >= testOrReal) {
                        break;
                    }
                }
                if (projectsAssigned >= topXProjectsToAssign) {
                    break;
                }
            }
            writer.flush();
            writer.close();
            Date d3 = new Date();
            System.out.println("    Finished in " + Constants.integerFormatter.format((d3.getTime() - d0.getTime()) / 1000) + " seconds (" +
                    Constants.integerFormatter.format(((d3.getTime() - d0.getTime()) / 1000) / 3600) + " hours and " +
                    Constants.integerFormatter.format((((d3.getTime() - d0.getTime()) / 1000) % 3600) / 60) + " minutes and " +
                    Constants.integerFormatter.format(((d3.getTime() - d0.getTime()) / 1000) % 60) + " seconds).");
            System.out.println("===========================================");
            System.out.println("End of assigning.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void determineAssigneeRankRandomly(ArrayList<CommunityMember> cmAL, ArrayList<String> allAssignees, int indexOfTheCurrentIssue, HashMap<String, Integer> allPreviousAssignees_unique, CommunityMember communityMemberThatHasBeenAssignedToThisIssue, CommunityMembersRankingResult assigneeRank) {
        HashMap<String, Scores> allPreviousAssigneesAndTheirChances = new HashMap<String, Scores>();
        ArrayList<String> silentUsers = new ArrayList<String>();
        //初始化所有的assignee的differentRankings为-1
        for (int i = 0; i < Constants.TOTAL_NUMBER_OF_METRICS; i++) {
            assigneeRank.differentRankings[i] = -1;
        }
        //如果当前处理的缺陷报告不是第一条缺陷报告
        if (indexOfTheCurrentIssue > 0) {
            for (int k = 0; k < cmAL.size(); k++) {
                CommunityMember cm = cmAL.get(k);
                //*************************此处是做是否为已经分派过缺陷报告的标识*****************************
                //依次判断成员在此待分派缺陷报告之前是否已经分派到了缺陷报告，如果该成员之前分派到了缺陷报告，则进行if操作，否则进行else操作
                if (cm.randomScore_zeroOrOne == 1) {
                    Scores rs = new Scores();
                    allPreviousAssigneesAndTheirChances.put(cm.ghLogin, rs);
                } else {
                    silentUsers.add(cm.ghLogin);
                }
            }
            String aTempLogin;
            //p表示之前已经分派过的缺陷报告的索引值
            for (int p = 0; p < indexOfTheCurrentIssue; p++) {
                //allAssignees里面根据缺陷报告的顺序依次存放真实的修复者名称，当p=0时，aTempLogin表示第一个真实修复者的名称
                aTempLogin = allAssignees.get(p);
                Scores rs = allPreviousAssigneesAndTheirChances.get(aTempLogin);//获取该真实修复者的分数
                /*
                计算时效性
                当indexOfTheCurrentIssue=1,p=0,那么recencyScore=1/(1-0+0.0)=1/1(表示处理第二条缺陷报告时，只有第一条缺陷报告的真实修复者可计算时效性分数)
                当indexOfTheCurrentIssue=2时
                      p=0,那么recencyScore=1/(2-0+0.0)=1/2(表示处理第三条缺陷报告时，只有第一条和第二条缺陷报告的真实修复者可计算时效性分数)
                      p=1,那么recencyScore=1/(2-1+0.0)=1/1
                 */
                double recencyScore = (double) 1 / (indexOfTheCurrentIssue - p + Constants.UNIMPORTANCE_OF_RECENT_ASSIGNMENTS);
                for (int i = 3; i < Constants.TOTAL_NUMBER_OF_METRICS; i++) {
                    rs.differentScores[i] = rs.differentScores[i] + recencyScore;
                }
                allPreviousAssigneesAndTheirChances.put(aTempLogin, rs);
            }
            mergeWith_z_score(cmAL, allPreviousAssigneesAndTheirChances, 0, Constants.TOTAL_NUMBER_OF_METRICS-1);

            HashMap<Integer, ArrayList<String>> allPreviousAssigneesWithZeroAssignmentPlusSOScore = new HashMap<Integer, ArrayList<String>>();
            copyUsersWithZeroAssignmentPlusSOScoreToHashMapOfArrayList(allPreviousAssigneesAndTheirChances, allPreviousAssigneesWithZeroAssignmentPlusSOScore, 0, Constants.TOTAL_NUMBER_OF_METRICS-1);

            determineRankOfRealAssignee_FirstStaticallyThenRandomly_BasedOnScores(allPreviousAssigneesAndTheirChances, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, 0, 0, Constants.TOTAL_NUMBER_OF_METRICS-1);
            for (int i = 4; i < Constants.TOTAL_NUMBER_OF_METRICS; i++) {
                ArrayList<String> usersWithZeroAssignmentPlusSOScore_accordingToAScoringType = allPreviousAssigneesWithZeroAssignmentPlusSOScore
                        .get(i);
                if ((usersWithZeroAssignmentPlusSOScore_accordingToAScoringType != null)
                        && (usersWithZeroAssignmentPlusSOScore_accordingToAScoringType
                        .size() > 0))
                    determineRankOfRealAssigneeCompletelyRandomly(
                            usersWithZeroAssignmentPlusSOScore_accordingToAScoringType,
                            communityMemberThatHasBeenAssignedToThisIssue,
                            assigneeRank,
                            allPreviousAssigneesAndTheirChances.size()
                                    - usersWithZeroAssignmentPlusSOScore_accordingToAScoringType
                                    .size(), i, i);
            }
        } else {
            //如果当前的缺陷报告是该项目的第一条缺陷报告
            //for循环表示依次循环该项目的每个成员，由于在此之前所有人都没有分派到缺陷报告，所以每个成员都是silentUsers和silentUsers2_forZeroR
            //？？？考虑silentUsers2_forZeroR有什么用
            for (int k = 0; k < cmAL.size(); k++) {
                CommunityMember cm = cmAL.get(k);
                silentUsers.add(cm.ghLogin);
            }
        }
            //以下表示完全随机的给真实修复者进行排名
            /*
            silentUsers表示在当前缺陷报告之前没有分派到缺陷报告的成员
            ？？？silentUsers2_forZeroR
            communityMemberThatHasBeenAssignedToThisIssue表示当前缺陷报告的真实修复者的信息，共6个属性
            assigneeRank表示所有成员的排名，由于是第一条缺陷报告，则所有值为differentScores初值-1
            allPreviousAssigneesAndTheirChances.size()表示在当前缺陷报告之前已经分派到缺陷报告的成员的个数（该数量其实和已经分派的缺陷报告的数量是一致的），由于此为第一条缺陷报告，因此为0
            0、1、2、3表示需要计算成员的分数的开始索引
            0、1、2、3表示需要成员某个分数的结束索引
             */
            //计算所有候选者针对第一份缺陷报告的第5种及以后的分数，即Recency+zScore及之后
            //???allSilentUserAndTheirChances具体表示什么含义
            HashMap<String, Scores> allSilentUserAndTheirChances = new HashMap<String, Scores>();
            //依次计算每个成员的rs，即recencyScore，并把分数存放在allSilentUserAndTheirChances
            for (String aLogin : silentUsers) {
                Scores rs = new Scores();
                //此处的rs均为0.0，因为大家都是silentUser，所以没有时效性分数；allSilentUserAndTheirChances存放的是该项目所有的候选者
                allSilentUserAndTheirChances.put(aLogin, rs);
            }
            //合并成员通过calculateScores()得到的专业知识分数和时效性分数
            mergeWith_z_score(cmAL, allSilentUserAndTheirChances, 0, Constants.TOTAL_NUMBER_OF_METRICS - 1);
            HashMap<Integer, ArrayList<String>> allSilentUsersWithZeroAssignmentPlusSOScore = new HashMap<Integer, ArrayList<String>>();
            //？？？下面的方法有什么用？
            copyUsersWithZeroAssignmentPlusSOScoreToHashMapOfArrayList(allSilentUserAndTheirChances, allSilentUsersWithZeroAssignmentPlusSOScore, 0, Constants.TOTAL_NUMBER_OF_METRICS-1);
            //决定真实修复者的最终排名的方法，此处的allPreviousAssigneesAndTheirChances.size()值为0
            determineRankOfRealAssignee_FirstStaticallyThenRandomly_BasedOnScores(allSilentUserAndTheirChances, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, allPreviousAssigneesAndTheirChances.size(), 0, Constants.TOTAL_NUMBER_OF_METRICS-1);
    }

    public static void determineRankOfRealAssigneeCompletelyRandomly(
            ArrayList<String> users,
            CommunityMember communityMemberThatHasBeenAssignedToThisIssue,
            CommunityMembersRankingResult assigneeRank,
            int numberOfRanksConsideredBefore, int startingScoreIndex,
            int endingScoreIndex) {
        for (int i = startingScoreIndex; i <= endingScoreIndex; i++)
            if (assigneeRank.differentRankings[i] == -1) {
                // 随机打乱用户的顺序
                // 对于第一条缺陷报告，所有的differentRankings都等于-1，且该方法被调用了4次，从for循环内容可以看出排序后的前四名是随机的
                Collections.shuffle(users);
                for (int j = 0; j < users.size(); j++)
                    if (users
                            .get(j)
                            .equals(communityMemberThatHasBeenAssignedToThisIssue.ghLogin)) {
                        assigneeRank.differentRankings[i] = numberOfRanksConsideredBefore
                                + j + 1;
                        break;
                    }// if (commu....
            }// if (assig....
    }
    public static void determineRankOfRealAssignee_FirstStaticallyThenRandomly_BasedOnScores(HashMap<String, Scores> usersAndScores, CommunityMember communityMemberThatHasBeenAssignedToThisIssue, CommunityMembersRankingResult assigneeRank, int numberOfRanksConsideredBefore, int startingScoreIndex, int endingScoreIndex) {
        //判断usersAndScores中是否包含真实的修复者，如果是则进行下面的操作
        if (usersAndScores.containsKey(communityMemberThatHasBeenAssignedToThisIssue.ghLogin)) {
            for (int i = startingScoreIndex; i <= endingScoreIndex; i++) {
                Random random = new Random();
                int numberOfMembersWithGreaterScore = 0;
                int numberOfMembersWithEqualScore = 0;
                //获取真实修复者当前的分数
                Scores scoresOfTheRealAssignee = usersAndScores.get(communityMemberThatHasBeenAssignedToThisIssue.ghLogin);
                for (String aLogin : usersAndScores.keySet()) {
                    //依次获取候选者的分数
                    Scores scoresOfALogin = usersAndScores.get(aLogin);
                    //此处统计比真实修复者的分数大的有多少个用户
                    if (scoresOfALogin.differentScores[i] > scoresOfTheRealAssignee.differentScores[i]) {
                        numberOfMembersWithGreaterScore++;
                    } else {
                        if (scoresOfALogin.differentScores[i] == scoresOfTheRealAssignee.differentScores[i]) {
                            numberOfMembersWithEqualScore++;
                        }
                    }
                }
                //决定真实修复者的排名
                //对于第一条缺陷报告，numberOfRanksConsideredBefore=0（不明白为什么要加上该值）
                assigneeRank.differentRankings[i] = numberOfRanksConsideredBefore + numberOfMembersWithGreaterScore + random.nextInt(numberOfMembersWithEqualScore) + 1;
            }
        }
    }

    public static void copyUsersWithZeroAssignmentPlusSOScoreToHashMapOfArrayList(HashMap<String, Scores> usersAndScores, HashMap<Integer, ArrayList<String>> usersWithZeroAssignmentPlusSOScore, int startingScoreIndex, int endingScoreIndex) {
        for (String aLogin : usersAndScores.keySet()) {
            Scores rs = usersAndScores.get(aLogin);
            for (int i = startingScoreIndex; i <= endingScoreIndex; i++) {
                if (rs.differentScores[i] <= 0) {
                    if (usersWithZeroAssignmentPlusSOScore.containsKey(i)) {
                        usersWithZeroAssignmentPlusSOScore.get(i).add(aLogin);
                    } else {
                        ArrayList<String> anArrayList = new ArrayList<String>();
                        anArrayList.add(aLogin);
                        usersWithZeroAssignmentPlusSOScore.put(i, anArrayList);
                    }
                }
            }
        }
    }

    public static void calculateSums(HashMap<String, Scores> usersAndScores, double[] sumOfScores, int startingScoreIndex, int endingScoreIndex) {
        for (int i = startingScoreIndex; i < endingScoreIndex; i++) {
            sumOfScores[i] = 0;
        }
        for (Scores rs : usersAndScores.values()) {
            for (int i = startingScoreIndex; i < endingScoreIndex; i++) {
                sumOfScores[i] = sumOfScores[i] + rs.differentScores[i];
            }
        }
    }

    public static void mergeWith_z_score(ArrayList<CommunityMember> cmAL, HashMap<String, Scores> usersAndScores, int startingScoreIndex, int endingScoreIndex) {
        double[] minimumScore = new double[Constants.TOTAL_NUMBER_OF_METRICS];
        for (int i = startingScoreIndex; i <= endingScoreIndex; i++) {
            minimumScore[i] = Constants.A_FAR_LARGESCORE;//从index4开始给数组minimumScore设置最大的初始值为100，0000，此最小值不是某人特有的，而是共同的
        }
        /*
        下面依次循环该项目的每个成员，首先判断该成员是否没有分派到任何缺陷报告，即是否是usersAndScores中的一员，如果是，则从usersAndScores中提取该成员的分数存放在rs
         */
        for (int k = 0; k < cmAL.size(); k++) {
            CommunityMember cm = cmAL.get(k);
            //如果是第一条缺陷报告，则usersAndScores表示allSilentUserAndTheirChances
            if (usersAndScores.containsKey(cm.ghLogin)) {
                //如果是第一条缺陷报告，则所有成员的rs为0.0
                Scores rs = usersAndScores.get(cm.ghLogin);
                for (int i = startingScoreIndex; i <= endingScoreIndex; i++) {
                    rs.differentScores[i] = rs.differentScores[i] + Constants.Z_SCORE_COEFFICIENTS[i] * cm.intersection_z_score;
                    if (rs.differentScores[i] < minimumScore[i]) {
                        minimumScore[i] = rs.differentScores[i];//此处得到当前调参对应的最小值
                    }
                }
                usersAndScores.put(cm.ghLogin, rs);
            }
        }
        //如果所有候选者的专业知识和时效性合并之后的最小值是负数，那么就用每个人的合并后的分数减去该最小值，得到最终的分数
        // （不明白为什么这么操作，可能是为了归一化吧，保证所有分数都大于0，方便处理）
        for (int i = startingScoreIndex; i < endingScoreIndex; i++) {
            if (minimumScore[i] < 0) {
                for (String aLogin : usersAndScores.keySet()) {
                    Scores rs = usersAndScores.get(aLogin);
                    rs.differentScores[i] = rs.differentScores[i] - minimumScore[i];
                    usersAndScores.put(aLogin, rs);
                }
            }

        }
    }

    public static void calculateScores(IssueTextualInformation issTI, String issueDate, ArrayList<SOPost> soPostsOfThisUserAL, TreeMap<String, String[]> allPosts1ById, CommunityMember cm, int indexOfTheCurrentIssue, int maximumNumberOfPreviousAssignmentsForAMemberInThisProject, double NF) {
        if (soPostsOfThisUserAL != null) {
            //依次循环该候选者的每一条post
            for (int i = 0; i < soPostsOfThisUserAL.size(); i++) {
                SOPost post_onlyLimitedInfo = soPostsOfThisUserAL.get(i);
                //该判断条件是确保post的创建时间在待修复缺陷报告的创建时间之前
                if (issueDate.compareTo(post_onlyLimitedInfo.creationDate) > 0) {

                    String originalPostTypeId = post_onlyLimitedInfo.postTypeId;
                    //变量post_completeInfo存放所有的post的posts1ById信息，包括post的id，PostTypeId，OwnerUserId，ParentId，Score，Tags
                    SOPost post_completeInfo = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(allPosts1ById.get(post_onlyLimitedInfo.id));
                    //变量scoreOfThePost表示点赞数
                    int scoreOfThePost = Integer.parseInt(post_completeInfo.Score);
                    //postTypeId等于1表示提问，等于2表示回答
                    //下方的if语句表示，如果post是回答，那么就找到其父post，即对应的问题，然后获取父post的posts1ById信息
                    if (post_completeInfo.postTypeId.equals("2")) {
                        post_completeInfo = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(allPosts1ById.get(post_onlyLimitedInfo.parentId));
                    }
                    String[] tags = splitAnStringIncludingAllTagsToStringArrayOfTags(post_completeInfo.tags);
                    //*****************************此处增加对tag的权值计算***************************
//                    HashMap<String, Double> numOfTags = calculateNumOfTagInIssueText(tags, issTI);
//                    double wOfTags = 0;
//                    for (int j = 0; j < tags.length; j++) {
//                        if (textIsMatchedWithTagOrNot(issTI.projectLanguage, tags[j]) ||
//                                textIsMatchedWithTagOrNot(issTI.projectDescription, tags[j]) ||
//                                textIsMatchedWithTagOrNot(issTI.issueTitle, tags[j]) ||
//                                textIsMatchedWithTagOrNot(issTI.issueBody, tags[j])) {
//                            if (originalPostTypeId.equals("2")) {
//                                cm.intersection_A++;
//                                cm.intersection_A_score = cm.intersection_A_score + (scoreOfThePost + 1) * numOfTags.get(tags[j]);
//                            } else if (originalPostTypeId.equals("1")) {
//                                cm.intersection_Q++;
//                                wOfTags = wOfTags + numOfTags.get(tags[j]);
//                            }
//                        }
//                    }
//                    int tempScoreOfThePost;
//                    if (scoreOfThePost < 0) {
//                        tempScoreOfThePost = 0;
//                    } else {
//                        tempScoreOfThePost = scoreOfThePost;
//                    }
//                    cm.intersection_Q_score = cm.intersection_Q_score + wOfTags / (tempScoreOfThePost + 1.0);
                    //****************************************注释开始************************************
                    int numberOfMatchedTags = 0;
                    for (int j = 0; j < tags.length; j++) {
                        if (textIsMatchedWithTagOrNot(issTI.projectLanguage, tags[j]) ||
                                textIsMatchedWithTagOrNot(issTI.projectDescription, tags[j]) ||
                                textIsMatchedWithTagOrNot(issTI.issueTitle, tags[j]) ||
                                textIsMatchedWithTagOrNot(issTI.issueBody, tags[j])) {
                            //这里表示无论post是提问还是回答，只要成功匹配一次tag，intersection_AQ则加1
                            cm.intersection_AQ++;
                            cm.intersection_AQ_score = cm.intersection_AQ_score + scoreOfThePost + 1;
                            //下面分别计算A_score和Q_score
                            //？？？此处如果是提问，则会统计tag的匹配数，但是如果是回答，则不会统计匹配数，是否是因为post里面所有的回答的tag都为空，所以后续计算时，直接统一默认为1？
                            if (originalPostTypeId.equals("2")) {
                                cm.intersection_A++;
                                cm.intersection_A_score = cm.intersection_A_score + scoreOfThePost + 1;
                            } else {
                                if (originalPostTypeId.equals("1")) {
                                    cm.intersection_Q++;
                                    numberOfMatchedTags++;
                                }
                            }
                        }
                    }
                    int tempScoreOfThePost;
                    if (scoreOfThePost < 0) {
                        tempScoreOfThePost = 0;
                    } else {
                        tempScoreOfThePost = scoreOfThePost;
                    }
                    cm.intersection_Q_score = cm.intersection_Q_score + numberOfMatchedTags / (tempScoreOfThePost + 1.0);
                    //****************************************注释结束******************************************
                }
            }//循环完了每一条post
            cm.intersection_Q_score = NF * cm.intersection_Q_score;
            if (cm.intersection_A_score + cm.intersection_Q_score > 0) {
                cm.intersection_z_score = (cm.intersection_A_score - cm.intersection_Q_score) / (cm.intersection_A_score + cm.intersection_Q_score);
            } else {
                cm.intersection_z_score = 0;
            }
        }
        //indexOfTheCurrentIssue的初始值为0，表示第一条缺陷报告，此处要求大于0，表示非第一条缺陷报告
        //对于第一条缺陷报告，不会执行下面的代码；从第二条缺陷报告开始执行
        if (indexOfTheCurrentIssue > 0) {
            //numberOfAssignmentsUpToNow表示该成员的真实修复的缺陷报告是否已经被分派
            if (cm.numberOfAssignmentsUpToNow > 0) {
                cm.randomScore_zeroOrOne = 1;//当该成员分派的缺陷报告数量大于0时，该值才能等于1，因此该值是判断该成员在此缺陷报告之前是否已经分派到缺陷报告
            }
        }
    }
//关键字加权
    public static HashMap<String, Double> calculateNumOfTagInIssueText(String[] tags, IssueTextualInformation issTI) {
        HashMap<String, Double> hmI = new HashMap<String, Double>();
        HashMap<String, Double> hmD = new HashMap<String, Double>();
        //将需要比对的BR文本合成一个string并用空格替换标点符号
        String issText = (issTI.projectLanguage + issTI.projectDescription + issTI.issueLabels + issTI.issueTitle + issTI.issueBody).replaceAll("[,\\.，。\\!！《》、;\\:；：\\s]", " ");
        //空格分词法
        String[] issWord = issText.split(" ");
        double countTotalTag = 0;
        for (int i = 0; i < tags.length; i++) {
            double count = 0;
            for (int j = 0; j < issWord.length; j++) {
                if (tags[i].equals(issWord[j])) {
                    count++;
                    countTotalTag++;
                }
            }
            hmI.put(tags[i], count);
        }
        //计算tag的权值
        for (Map.Entry<String, Double> entry : hmI.entrySet()) {
            hmD.put(entry.getKey(), entry.getValue() / countTotalTag);
        }
        return hmD;
    }

    public static double calculateHarmonicMeanOfUpVotesForQuestionsTaggedWithAtLeastOneOfTagsInMatchedTags_SO_b(IssueTextualInformation issTI, TreeMap<String, String[]> posts1ById) {
        double sumOfInvertedUpVotes = 0;
        int count = 0;
        for (Map.Entry<String, String[]> entry : posts1ById.entrySet()) {
            SOPost post_completeInfo = null;
            post_completeInfo = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(entry.getValue());
            //计算NF时只使用Q信息，不使用A信息，可能作者考虑到A信息没有tag吧
            if (post_completeInfo.postTypeId.equals("1")) {
                String[] tags = splitAnStringIncludingAllTagsToStringArrayOfTags(post_completeInfo.tags);
                for (int j = 0; j < tags.length; j++) {
                    if (textIsMatchedWithTagOrNot(issTI.projectLanguage, tags[j]) ||
                            textIsMatchedWithTagOrNot(issTI.projectDescription, tags[j]) ||
                            textIsMatchedWithTagOrNot(issTI.issueTitle, tags[j]) ||
                            textIsMatchedWithTagOrNot(issTI.issueBody, tags[j])) {
                        int scoreOfThePostPlusOne = Integer.parseInt(post_completeInfo.Score) + 1;
                        if (scoreOfThePostPlusOne < 1) {
                            scoreOfThePostPlusOne = 1;
                        }
                        sumOfInvertedUpVotes = sumOfInvertedUpVotes + 1.0 / scoreOfThePostPlusOne;
                        count++;
                        break;
                    }
                }
            }
        }
        double result;
        if (count > 0) {
            if (sumOfInvertedUpVotes > 0) {
                result = count / (2 * sumOfInvertedUpVotes);
                if (result < 1) {
                    result = 1;
                }
            } else {
                result = 1;
            }
        } else {
            System.out.println("        !! Warning in calculating NF (No question is tagged with the textual info of this bug report) !!");
            result = 1;
        }
        return result;
    }

    public static boolean textIsMatchedWithTagOrNot(String str, String tag) {
        if (str.contains(" " + tag + " ") || str.equals(tag) || str.contains(" " + tag + ".")) {
            return true;
        } else {
            return false;
        }
    }

    public static String[] splitAnStringIncludingAllTagsToStringArrayOfTags(String tagsString) {
        tagsString = tagsString.replaceFirst("\\[", "");
        tagsString = tagsString.replaceFirst("\\]", "");
        tagsString = tagsString.toLowerCase();
        String[] result = tagsString.split(Constants.SEPARATOR_FOR_ARRAY_ITEMS);
        return result;
    }

    public static CommunityMember getPointerToCommunityMemberOfGHLogin(ArrayList<CommunityMember> communityMembers, String assigneeLogin) {
        int resultIndex = -1;
        for (int i = 0; i < communityMembers.size(); i++) {
            //如果候选的修复者当中有一个人的ghLogin和待修复缺陷报告的assigneeLogin相等，resultIndex就为该候选修复者的索引值
            if (communityMembers.get(i).ghLogin.equals(assigneeLogin)) {
                resultIndex = i;
                break;
            }
        }
        //不等于-1表示在候选修复者当中找到了真实的修复者，那么cm则是该真实的候选修复者的信息，包括mySQLProjectId,ownerLogin/projName,SOId,ghLogin,ghMySQLId,ghMongoDBId
        if (resultIndex != -1) {
            CommunityMember cm = communityMembers.get(resultIndex);
            return cm;
        } else {
            return null;
        }
    }

    public static void extractAndCopyAssigneeValues(ArrayList<Issue> issAL, ArrayList<String> allAssignees) {
        for (int p = 0; p < issAL.size(); p++) {
            Issue iss = issAL.get(p);
            allAssignees.add(iss.assigneeLogin);
        }
    }
}

