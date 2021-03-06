package data;
/*
 * 将输入的tsv文件中的字符串复制到给定类的字段中，然后返回对象
 * 其中ProjectAndItsNumberOfMemberships类
 */

import java.util.ArrayList;
import java.util.TreeMap;

//This class is used for readability. In other words, the values in String[] are copied into an object of a plain class.
public class ProvideData {//This class provides different static methods.

    //Each method creates an object (with respect to one of the TSV file), copies the string[] values of the input parameter to the fields of a given class in order and then returns the object.
    /*
        此处删掉copyProjectAndItsNumberOfMembershipsFromStringArray()
        copyProjectAndItsCommittersFromArrayListOfStringArray()
        copyProjectAndItsCommittersFromArrayListOfStringArray_OnlyThereAreSomeFields1()
        copyCommonUserFromStringArray()
        copyCommonUserFromStringArray_OnlyThereAreSomeFields1()
        未使用，现有数据集无该tsv文件
     */
    //----------------------------------------------------------------------------------------------------------------
    //This method is used for numberOfMembersInEachProjectForCommonUsers2-withTotalMembers.tsv:
    public static MySQLProject copyMySQLProjectFromStringArray(String[] fields) {
        MySQLProject p = new MySQLProject();
        if (fields.length < 9)
            System.out.println("Error in number of fields.");
        p.projectMySQLId = fields[0];
        p.url = fields[1];
        p.ownerLoginAndProjectName = fields[2];
        p.description = fields[3];
        p.language = fields[4];
        p.createdAt = fields[5];
        p.extRefId = fields[6];
        p.forkedFrom = fields[7];
        p.deleted = fields[8];
        return p;
    }//copyMySQLProjectFromStringArray().

    //----------------------------------------------------------------------------------------------------------------
    public static MySQLProject copyMySQLProjectFromStringArray_OnlyThereAreSomeFields1(String[] fields) {
        MySQLProject p = new MySQLProject();
        if (fields.length < 1)
            System.out.println("Error in number of fields.");
        p.ownerLoginAndProjectName = fields[0];
        return p;
    }//copyMySQLProjectFromStringArray_OnlyThereAreSomeFields1().
    /*
        此处删掉copyMongoDBProjectFromStringArray()和copyProjectsAndItsMembersFromArrayListOfStringArray()
     */

    //----------------------------------------------------------------------------------------------------------------
    //This method is used for ...
    public static ArrayList<ProjectAndItsMembers> copyProjectsAndItsMembersFromArrayListOfStringArray_OnlyThereAreSomeFields1(ArrayList<String[]> arrayListOfFields) {
        ArrayList<ProjectAndItsMembers> pmAL = new ArrayList<ProjectAndItsMembers>();
        for (int j = 0; j < arrayListOfFields.size(); j++) {
            if (arrayListOfFields.get(j).length < 1)
                System.out.println("Error in number of fields (ProvideData.copyProjectsAndItsMembersFromArrayListOfStringArray.)");
            else {
                ProjectAndItsMembers pm = new ProjectAndItsMembers();
                pm.mySQLUserId = arrayListOfFields.get(j)[0];
                pmAL.add(pm);
            }//else.
        }//for.
        return pmAL;
    }//copyProjectsAndItsMembersFromArrayListOfStringArray_OnlyThereAreSomeFields1().

    //----------------------------------------------------------------------------------------------------------------
    //This method is used for    .tsv:
    public static ArrayList<Issue> copyIssuesFromArrayListOfStringArray_OnlyThereAreSomeFields1(ArrayList<String[]> arrayListOfFields) {
        ArrayList<Issue> issAL = new ArrayList<Issue>();
        for (int j = 0; j < arrayListOfFields.size(); j++) {
            if (arrayListOfFields.get(j).length != 2)
                System.out.println("Error in number of fields (ProvideData.copyIssuesFromArrayListOfStringArray.)");
            else {
                Issue iss = new Issue();
                iss.reporterLogin = arrayListOfFields.get(j)[0];
                iss.assigneeLogin = arrayListOfFields.get(j)[1];
                issAL.add(iss);
            }//else.
        }//for.
        return issAL;
    }//copyIssuesFromArrayListOfStringArray_OnlyThereAreSomeFields1().

    //无该tsv文件--------------------------------------------------------------------------------------------------------
    //This method is used for issues-Assigned-mergedTwoColumns.tsv:
    public static ArrayList<Issue> copyIssuesFromArrayListOfStringArray(ArrayList<String[]> arrayListOfFields) {
        ArrayList<Issue> issAL = new ArrayList<Issue>();
        for (int j = 0; j < arrayListOfFields.size(); j++) {
            if (arrayListOfFields.get(j).length != 11)
                System.out.println("Error in number of fields (ProvideData.copyIssuesFromArrayListOfStringArray.)");
            else {
                Issue iss = new Issue();
                iss.id = arrayListOfFields.get(j)[0];
                iss.ownerLoginAndProjectName = arrayListOfFields.get(j)[1];
                iss.reporterId = arrayListOfFields.get(j)[2];
                iss.reporterLogin = arrayListOfFields.get(j)[3];
                iss.assigneeId = arrayListOfFields.get(j)[4];
                iss.assigneeLogin = arrayListOfFields.get(j)[5];
                iss.createdAt = arrayListOfFields.get(j)[6];
                iss.numberOfComments = arrayListOfFields.get(j)[7];
                iss.labels = arrayListOfFields.get(j)[8];
                iss.title = arrayListOfFields.get(j)[9];
                iss.body = arrayListOfFields.get(j)[10];
                issAL.add(iss);
            }//else.
        }//for.
        return issAL;
    }//copyIssuesFromArrayListOfStringArray().
    /*
        此处删掉copyIssuesFromArrayListOfStringArray_OnlyThereAreSomeFields2()
    */

    //----------------------------------------------------------------------------------------------------------------
    public static ArrayList<Issue> copyIssuesFromArrayListOfStringArray_OnlyThereAreSomeFields3(ArrayList<String[]> arrayListOfFields) {
        ArrayList<Issue> issAL = new ArrayList<Issue>();
        for (int j = 0; j < arrayListOfFields.size(); j++) {
            if (arrayListOfFields.get(j).length != 6)
                System.out.println("Error in number of fields (ProvideData.copyIssuesFromArrayListOfStringArray.)");
            else {//0-5-6-8-9-10
                Issue iss = new Issue();
                iss.id = arrayListOfFields.get(j)[0];
                iss.assigneeLogin = arrayListOfFields.get(j)[1];
                iss.createdAt = arrayListOfFields.get(j)[2].substring(0, 19); //Get up to seconds from for example this date-time: "2008-08-02T17:26:39.793"
                iss.labels = arrayListOfFields.get(j)[3];
                iss.title = arrayListOfFields.get(j)[4];
                iss.body = arrayListOfFields.get(j)[5];
                issAL.add(iss);
            }//else.
        }//for.
        return issAL;
    }//copyIssuesFromArrayListOfStringArray_OnlyThereAreSomeFields3().

    //----------------------------------------------------------------------------------------------------------------
    public static ArrayList<CommunitiesSummary> copyCommunitiesSummaryFromArrayListOfStringArray_OnlyThereAreSomeFields1(ArrayList<String[]> arrayListOfFields) {
        ArrayList<CommunitiesSummary> csAL = new ArrayList<CommunitiesSummary>();
        for (int j = 0; j < arrayListOfFields.size(); j++) {
            if (arrayListOfFields.get(j).length != 2)
                System.out.println("Error in number of fields (ProvideData.copyCommunitiesSummaryFromArrayListOfStringArray_OnlyThereAreSomeFields1.)");
            else {
                CommunitiesSummary cs = new CommunitiesSummary();
                cs.mySQLProjectId = arrayListOfFields.get(j)[0];
                cs.ownerLoginAndProjectName = arrayListOfFields.get(j)[1];
                csAL.add(cs);
            }//else.
        }//for.
        return csAL;
    }//copyCommunitiesSummaryFromArrayListOfStringArray_OnlyThereAreSomeFields1().

    //---------------------------------------------------------------------------------------------------------
    public static ArrayList<CommunityMember> copyCommunityFromArrayListOfStringArray(ArrayList<String[]> arrayListOfFields) {
        ArrayList<CommunityMember> comAL = new ArrayList<CommunityMember>();
        for (int j = 0; j < arrayListOfFields.size(); j++) {
            if (arrayListOfFields.get(j).length != 6)
                System.out.println("Error in number of fields (ProvideData.copyCommunityFromArrayListOfStringArray.)");
            else {
                CommunityMember com = new CommunityMember();
                com.mySQLProjectId = arrayListOfFields.get(j)[0];
                com.ownerLoginAndProjectName = arrayListOfFields.get(j)[1];
                com.SOId = arrayListOfFields.get(j)[2];
                com.ghLogin = arrayListOfFields.get(j)[3];
                com.ghMySQLId = arrayListOfFields.get(j)[4];
                com.ghMongoDBId = arrayListOfFields.get(j)[5];
                comAL.add(com);
            }//else.
        }//for.
        return comAL;//针对每个项目，comAL中添加了arrayListOfFields.size()条数据
    }//copyCommunityFromArrayListOfStringArray().

    //----------------------------------------------------------------------------------------------------------------
    public static CommunitiesSummary copyCommunitiesSummaryFromStringArray_OnlyThereAreSomeFields1(String[] fields) {
        CommunitiesSummary cs = new CommunitiesSummary();
        if (fields.length != 30)
            System.out.println("Error in number of fields.");
        cs.mySQLProjectId = fields[0];
        //cs.ownerLoginAndProjectName = fields[1];
        return cs;
    }//copyCommunitiesSummaryFromStringArray_OnlyThereAreSomeFields1().

    //----------------------------------------------------------------------------------------------------------------
    public static Issue copyIssueFromStringArray(String[] fields) {
        Issue iss = new Issue();
        if (fields.length != 11)
            System.out.println("Error in number of fields (ProvideData.copyIssueFromStringArray.)");
        else {
            iss.id = fields[0];
            iss.ownerLoginAndProjectName = fields[1];
            iss.reporterId = fields[2];
            iss.reporterLogin = fields[3];
            iss.assigneeId = fields[4];
            iss.assigneeLogin = fields[5];
            iss.createdAt = fields[6];
            iss.numberOfComments = fields[7];
            iss.labels = fields[8];
            iss.title = fields[9];
            iss.body = fields[10];
        }//else.
        return iss;
    }//copyIssueFromStringArray().

    //----------------------------------------------------------------------------------------------------------------
    public static ArrayList<SOPost> copypostIdAndPostContentsFromArrayListOfStringArray(ArrayList<String[]> arrayListOfFields) {
        ArrayList<SOPost> soPAL = new ArrayList<SOPost>();
        for (int j = 0; j < arrayListOfFields.size(); j++) {
            if (arrayListOfFields.get(j).length != 9)
                System.out.println("Error in number of fields (ProvideData.copypostIdAndPostContentsFromArrayListOfStringArray.)");
            else {
                SOPost soP = new SOPost();
                soP.id = arrayListOfFields.get(j)[0];
                soP.postTypeId = arrayListOfFields.get(j)[1];
                soP.ownerUserId = arrayListOfFields.get(j)[2];
                soP.parentId = arrayListOfFields.get(j)[3];
                soP.Score = arrayListOfFields.get(j)[4];
                soP.tags = arrayListOfFields.get(j)[5];
                soP.creationDate = arrayListOfFields.get(j)[6];
                soP.answerCount = arrayListOfFields.get(j)[7];
                soP.title = arrayListOfFields.get(j)[8];
                soPAL.add(soP);
            }//else.
        }//for.
        return soPAL;
    }//copypostIdAndPostContentsFromArrayListOfStringArray().

    //---------------------------------------------------------------------
    public static ArrayList<SOPost> copyPostContentsFromArrayListOfStringArray_OnlyThereAreSomeFields1(ArrayList<String[]> arrayListOfFields) {
        //arrayListOfFields是以OwnerUserId为键值的一组长为5的值
        ArrayList<SOPost> soPAL = new ArrayList<SOPost>();
        for (int j = 0; j < arrayListOfFields.size(); j++) {//从这个循环可以看出arrayListOfFields里面有至少一组键值相同的值
            if (arrayListOfFields.get(j).length != 5)
                System.out.println("Error in number of fields (ProvideData.copypostIdAndPostContentsFromArrayListOfStringArray_OnlyThereAreSomeFields1.)");
            else {
                SOPost soP = new SOPost();
                soP.id = arrayListOfFields.get(j)[0];
                soP.postTypeId = arrayListOfFields.get(j)[1];
                soP.ownerUserId = arrayListOfFields.get(j)[2];
                soP.parentId = arrayListOfFields.get(j)[3];
                soP.creationDate = arrayListOfFields.get(j)[4].substring(0, 19);//Get up to seconds from for example this date-time: "2008-08-02T17:26:39.793"
                soPAL.add(soP);
            }//else.
        }//for.
        return soPAL;
    }//copypostIdAndPostContentsFromArrayListOfStringArray_OnlyThereAreSomeFields1().

    //代码中调用该方法是为了后续计算关联缺陷报告之后的每个成员的分数------------------------------------------------------------------------
    public static void initializeIntersectionWithBugsScores(ArrayList<CommunityMember> cmAL) {
        for (int i = 0; i < cmAL.size(); i++) {
            cmAL.get(i).intersection_A = 0;
            cmAL.get(i).intersection_AQ = 0;
            cmAL.get(i).intersection_A_score = 0;
            cmAL.get(i).intersection_AQ_score = 0;

            cmAL.get(i).intersection_Q = 0;
            cmAL.get(i).intersection_Q_score = 0; //Alaki.
            cmAL.get(i).intersection_z_score = 0;
            //*********************设置计算answerNum和Z-score时需要的变量******************************
            cmAL.get(i).answerNum = 0;
            cmAL.get(i).totalAnswers = 0;
            cmAL.get(i).totalAnswers = 0;
            cmAL.get(i).traditional_z_score = 0;
            //*************************************************************************************

//			cm.get(i).numberOfAssignmentsUpToNow = 0;
//			cm.get(i).randomScore = 0;
//			cm.get(i).weightedRandomScore = 0;
//			cm.get(i).zeroRScore = 0;
        }
    }//initializeIntersectionWithBugsScores().

    //将cmAL中的每个用户的相关值都初始化为0，代码中调用了该方法之后，接下来是计算每个成员的传统Z-Score--------------------------------------------------------------------------
    public static void initializeRandomScores(ArrayList<CommunityMember> cmAL) {
        for (int i = 0; i < cmAL.size(); i++) {
            cmAL.get(i).numberOfAssignmentsUpToNow = 0;
            cmAL.get(i).randomScore_zeroOrOne = 0;
            cmAL.get(i).weightedRandomScore_count = 0;
            cmAL.get(i).zeroRScore_zeroOrOne = 0;

            cmAL.get(i).combinedScore1 = 0;
            cmAL.get(i).combinedScore2 = 0;

            cmAL.get(i).intersection_A_score = 0;
            cmAL.get(i).intersection_Q_score = 0;
            cmAL.get(i).intersection_z_score = 0;
        }
    }//initializeRandomScores().

    //----------------------------------------------------------------------------------------------------------------
    public static void calculateTraditionalZ_ScoreParameters(ArrayList<CommunityMember> cmAL, TreeMap<String, ArrayList<String[]>> posts2ByOwnerId) {
        for (int i = 0; i < cmAL.size(); i++) {
            cmAL.get(i).totalAnswers = 0;
            cmAL.get(i).totalQuestions = 0;
            cmAL.get(i).traditional_z_score = 0;
            /*
             * 如果posts2ByOwnerId中的OwnerUserId包含CommunityMember的SOId，则找出该成员的所有Post，并通过判断postTypeId
			 * 计算totalAnswers、totalQuestions、traditional_z_score
			 */
            ArrayList<SOPost> soPostsOfThisUserAL = null;
            if (posts2ByOwnerId.containsKey(cmAL.get(i).SOId)) {//如果posts的OwnerUserId中有cmAL的SOId
                //读取该用户的所有posts信息存入soPostsOfThisUserAL
                soPostsOfThisUserAL = copyPostContentsFromArrayListOfStringArray_OnlyThereAreSomeFields1(posts2ByOwnerId.get(cmAL.get(i).SOId));
                for (int j = 0; j < soPostsOfThisUserAL.size(); j++) {//For all SO posts of this user:
                    SOPost aPost = soPostsOfThisUserAL.get(j);
                    if (aPost.postTypeId.equals("2"))//i.e., it is an answer，具体1,2这样定义的原因是依据input的posts表
                        cmAL.get(i).totalAnswers++;
                    else if (aPost.postTypeId.equals("1"))//i.e., it is an question
                        cmAL.get(i).totalQuestions++;
                }//for (j.
                if (cmAL.get(i).totalAnswers + cmAL.get(i).totalQuestions > 0)//计算z_score分数
                    cmAL.get(i).traditional_z_score = (cmAL.get(i).totalAnswers - cmAL.get(i).totalQuestions) / Math.sqrt(cmAL.get(i).totalAnswers + cmAL.get(i).totalQuestions);
            }//if (posts....
        }//for (i.
    }//initializeRandomScores().

    //----------------------------------------------------------------------------------------------------------------
    public static SOPost copySOPostFromStringArray_OnlyThereAreSomeFields1(String[] fields) {
        SOPost p = new SOPost();

        if (fields.length != 6)
            System.out.println("Error in number of fields.");
        p.id = fields[0];
        p.postTypeId = fields[1];
        p.ownerUserId = fields[2];
        p.parentId = fields[3];
        p.Score = fields[4];
        p.tags = fields[5];
        return p;
    }//copySOPostFromStringArray_OnlyThereAreSomeFields1().

    //----------------------------------------------------------------------------------------------------------------
    public static Project copyProjectFromStringArray(String[] fields) {
        Project p = new Project();
        if (fields.length != 9)
            System.out.println("Error in number of fields.");
        p.projectMySQLId = fields[0];
        p.url = fields[1];
        p.ownerLoginAndProjectName = fields[2];
        p.description = fields[3];
        p.language = fields[4];
        p.createdAt = fields[5];
        p.extRefId = fields[6];
        p.forkedFrom = fields[7];
        p.deleted = fields[8];
        return p;
    }//copyProjectAndItsNumberOfMembershipsFromStringArray().
}

