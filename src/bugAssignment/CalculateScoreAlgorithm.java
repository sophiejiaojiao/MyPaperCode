package bugAssignment;

import bugAssignment.Constants.ConditionType;
import bugAssignment.Constants.SortOrder;
import data.*;

import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalculateScoreAlgorithm {
    // 调和平均数=每个数的倒数和的算术平均数的倒数-------------------------------------------------------------------------------------------------
    private static double calculateHarmonicMeanOfUpVotesForQuestionsTaggedWithAtLeastOneOfTagsInMatchedTags_SO_b(
            IssueTextualInformation issTI, TreeMap<String, String[]> posts1ById) {
        double sumOfInvertedUpVotes = 0;// 反向的赞
        int count = 0;
        for (Map.Entry<String, String[]> entry : posts1ById.entrySet()) {
            // post_completeInfo存放该项目所有相关的Post的信息
            SOPost post_completeInfo = ProvideData
                    .copySOPostFromStringArray_OnlyThereAreSomeFields1(entry
                            .getValue());
            if (post_completeInfo.postTypeId.equals("1")) { // i.e., this post
                // is a question
                // 将文档posts中的tags信息按照;;进行分割后，转换成字符串数组
                String[] tags = splitAnStringIncludingAllTagsToStringArrayOfTags(post_completeInfo.tags);
                // 将tags依次与projectLanguage等进行匹配，如果匹配成功，则score加1
                // 匹配的时候一条Q可能有多个tag，但是代码只判断这条Q的tag成功匹配一个就行，为什么？
                for (int j = 0; j < tags.length; j++) {
                    if (textIsMatchedWithTagOrNot(issTI.projectLanguage,
                            tags[j])
                            || textIsMatchedWithTagOrNot(
                            issTI.projectDescription, tags[j])
                            || textIsMatchedWithTagOrNot(issTI.issueTitle,
                            tags[j])
                            || textIsMatchedWithTagOrNot(issTI.issueBody,
                            tags[j])) {
                        //
                        int scoreOfThePostPlusOne = Integer.parseInt(post_completeInfo.Score) + 1;
                        if (scoreOfThePostPlusOne < 1)
                            // 保证每个question至少有一个匹配的tag（论文有描述，第7页记号笔）
                            scoreOfThePostPlusOne = 1;
                        // 计算每个数的倒数和
                        sumOfInvertedUpVotes = sumOfInvertedUpVotes + 1.0 / scoreOfThePostPlusOne;
                        count++;
                        break;
                    }
                }// for.
            }// if (post_com....
        }// for(Map....
        double result;

        if (count > 0) {
            if (sumOfInvertedUpVotes > 0) {
                // 计算倒数和的倒数，result即为调和平均值
                // 为什么要乘以2？
                // 原始公式
                result = count / (2 * sumOfInvertedUpVotes);
                // 改动1公式，即计算其平方，分派率没变化
                // result = Math.pow(count/(2*sumOfInvertedUpVotes), 2);
                // 改动2公式，分母不乘2，分派率没变化
                // result = count/(sumOfInvertedUpVotes);
                // 改动3公式，result乘以1000
                // result = (count/(2*sumOfInvertedUpVotes))*1000;
                if (result < 1)
                    result = 1;
            } else
                result = 1;
        }// if (count....
        else {// count小于或等于0时
            System.out
                    .println("        !! Warning in calculating NF (No question is tagged with the textual info of this bug report) !!");
            result = 1;
        }
        return result;
    }// findMatchedTags_SO_b().
    // 判断该项目成员的ghLogin是否等于该项目的缺陷报告中的assigneeLogin，如果相等则返回该ghLogin-----------------------------------------------------------------------------------

    private static CommunityMember getPointerToCommunityMemberOfGHLogin(
            ArrayList<CommunityMember> communityMembers, String assigneeLogin) {
        int resultIndex = -1;
        for (int i = 0; i < communityMembers.size(); i++)
            if (communityMembers.get(i).ghLogin.equals(assigneeLogin)) {
                resultIndex = i;
                break;
            }// if (comm....
        if (resultIndex != -1) {// Found this assigneeLogin in the
            // communityMembers. So return a (new) copy of
            // that member:
            CommunityMember cm = communityMembers.get(resultIndex); // Return
            // the
            // reference
            // to the
            // same
            // class.
            return cm;
        }// if (resu....
        else
            // this assigneeLogin does not exist in the communityMembers, so
            // return null:
            return null;
    }// getCommunityMemberOfGHLogin().
    // ---------------------------------------------------------------------------------------------------------------------------------------

    public static boolean textIsMatchedWithTagOrNot(String str, String tag) {
        // if (str.equals(tag) || str.startsWith(tag + " ") || str.endsWith(" "
        // + tag) || str.contains(" " + tag + " ") || str.contains(" " + tag +
        // ".")) //2222222222 needs to be fixed: "lucene-4870" and "open source"
        // ("open-source" in SO)
        // We don't need the first three conditions because we added space
        // before and after str in the calling function.
        if (str.contains(" " + tag + " ") || str.equals(tag)
                || str.contains(" " + tag + ".")) // 2222222222 needs to be
            // fixed: "lucene-4870" and
            // "open source"
            // ("open-source" in SO)
            return true;
        else
            return false;
    }// textIsMatchedWithTagOrNot().
    // ---------------------------------------------------------------------------------------------------------------------------------------

    public static String[] splitAnStringIncludingAllTagsToStringArrayOfTags(
            String tagsString) {
        tagsString = tagsString.replaceFirst("\\[", "");// replaceFirst()替换第一个匹配
        tagsString = tagsString.replaceFirst("\\]", "");
        tagsString = tagsString.toLowerCase();
        String[] result = tagsString.split(Constants.SEPARATOR_FOR_ARRAY_ITEMS);// 按照;;进行分割
        return result;
    }// convertAnStringIncludingAllTagsToStringArrayOfTags
    // ---------------------------------------------------------------------------------------------------------------------------------------

    public static void updateScoreOfCommunityMemberComparingTheTag(String str,
                                                                   String originalPostTypeId, String tag, int scoreOfThePost,
                                                                   CommunityMember cm) {
        str = str.toLowerCase();
        if (textIsMatchedWithTagOrNot(str, tag)) {
            cm.intersection_AQ++;
            cm.intersection_AQ_score = cm.intersection_AQ_score
                    + scoreOfThePost + 1; // considering the answer a score,
            // then each upvote adds an score to
            // that.
            if (originalPostTypeId.equals("2")) {// i.e., this post is an
                // answer:
                cm.intersection_A++;
                cm.intersection_A_score = cm.intersection_A_score
                        + scoreOfThePost + 1; // considering the answer a score,
                // then each upvote adds an
                // score to that.
            }// if (origi....
            else if (originalPostTypeId.equals("1")) { // i.e., this post is an
                // question
                cm.intersection_Q++;
                cm.intersection_Q_score = cm.intersection_Q_score
                        + scoreOfThePost + 1;
            }
        }// if (issTI... issTI.issueTitle, originalPostTypeId, tags[j],
        // scoreOfThePost, cm
    }// updateScoreOfCommunityMemberComparingTheTag().
    // ---------------------------------------------------------------------------------------------------------------------------------------

    // This method calculates the scores of a user considering an issue textual
    // information and his posts. The results are put in cm.
    public static void calculateScores(IssueTextualInformation issTI,
                                       String issueDate,
                                       ArrayList<SOPost> soPostsOfThisUserAL,
                                       TreeMap<String, String[]> allPosts1ById,
                                       CommunityMember cm, // ArrayList<Issue> issAL,
                                       int indexOfTheCurrentIssue,
                                       int maximumNumberOfPreviousAssignmentsForAMemberInThisProject,
                                       double NF) {// NF: Normalization Factor =
        // averageOfUpVotesForQuestionsTaggedWithAtLeastOneOfTagsInMatchedTags_SO_b
        // if (cm.SOId.equals("900911"))
        // System.out.println("ssss");
        // soPostsOfThisUserAL表示该成员的所有post
        if (soPostsOfThisUserAL != null) {
            // 依次读取该成员的每一条post
            for (int i = 0; i < soPostsOfThisUserAL.size(); i++) {// For all SO
                // posts of
                // this
                // user:
                SOPost post_onlyLimitedInfo = soPostsOfThisUserAL.get(i);
                // means that the issueDate > postDate即在缺陷报告之前有相关QA互动信息
                if (issueDate.compareTo(post_onlyLimitedInfo.creationDate) > 0) {
                    String originalPostTypeId = post_onlyLimitedInfo.postTypeId;
                    // System.out.println(post_onlyLimitedInfo.id + "\t" +
                    // post_onlyLimitedInfo.parentId);
                    SOPost post_completeInfo = ProvideData
                            .copySOPostFromStringArray_OnlyThereAreSomeFields1(allPosts1ById
                                    .get(post_onlyLimitedInfo.id));
                    // int scoreOfThePost = 0;
                    // scoreOfThePost表示vote数，即posts文档中的score
                    int scoreOfThePost = Integer
                            .parseInt(post_completeInfo.Score); // 333333333333
                    // i.e., it is an answer, so fetch its related
                    // question:parentId表示该answer对应的question的id
                    if (post_onlyLimitedInfo.postTypeId.equals("2"))
                        // 将related
                        // question的信息替换post_completeInfo原有的post信息，该信息中有tag
                        post_completeInfo = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(allPosts1ById.get(post_onlyLimitedInfo.parentId));
                    String[] tags = splitAnStringIncludingAllTagsToStringArrayOfTags(post_completeInfo.tags);

                    // *********************************开始加权********************************
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
                            HashMap<String, Double> numOfTags = calculateNumOfTagInIssueText(
                                    tags, issTI);
                            if (originalPostTypeId.equals("2")) {
                                cm.intersection_A++;
                                //点赞数加1
                                cm.intersection_A_score = cm.intersection_A_score + (scoreOfThePost + 1.0) * (numOfTags.get(tags[j]) * Constants.NumberForWeightTag + 1 - Constants.NumberForWeightTag);
                                //点赞数不加1
//                                cm.intersection_A_score = cm.intersection_A_score + (scoreOfThePost) * (numOfTags.get(tags[j]) * Constants.NumberForWeightTag + 1 - Constants.NumberForWeightTag);
                            } else if (originalPostTypeId.equals("1")) {
                                cm.intersection_Q++;
                                wOfTags = wOfTags + numOfTags.get(tags[j]) * Constants.NumberForWeightTag + 1 - Constants.NumberForWeightTag;
                            }
                        }
                    }
                    int tempScoreOfThePost;
                    if (scoreOfThePost < 0) {
                        tempScoreOfThePost = 0;
                    } else {
                        tempScoreOfThePost = scoreOfThePost;
                    }
                    cm.intersection_Q_score = cm.intersection_Q_score + wOfTags / (tempScoreOfThePost + 1.0);
                    // ********************************结束加权*********************************
/*
                    // ********************************未加权开始*********************************
                    int numberOfMatchedTags = 0;
                    for (int j = 0; j < tags.length; j++) {//For all tags
                        //只要tags中的某个tag与缺陷报告文本信息成功匹配一次，分数就加1，然后匹配下一个tag
                        if (textIsMatchedWithTagOrNot(issTI.projectLanguage,
                                tags[j]) ||
                                textIsMatchedWithTagOrNot(issTI.projectDescription,
                                        tags[j]) ||
                                textIsMatchedWithTagOrNot(issTI.issueTitle, tags[j]) ||
                                textIsMatchedWithTagOrNot(issTI.issueBody, tags[j])
                                ) {
                            cm.intersection_AQ++;
                            cm.intersection_AQ_score = cm.intersection_AQ_score +
                                    scoreOfThePost + 1; //considering the answer a score,then each upvote adds an score to that.
                            if (originalPostTypeId.equals("2")) {//i.e., this post isan answer:
                                cm.intersection_A++;
                                cm.intersection_A_score = cm.intersection_A_score + scoreOfThePost + 1;
                            }//if (origi....
                            else if (originalPostTypeId.equals("1")) { //i.e., this post is a question
                                cm.intersection_Q++;
                                numberOfMatchedTags++;
                            }
                        }//if (textIs....
                    }//for (j.
                    int tempScoreOfThePost;
                    if (scoreOfThePost < 0)//如果vote<0,则将vote置为0；否则计算Q_score
                        tempScoreOfThePost = 0;
                    else
                        tempScoreOfThePost = scoreOfThePost;
                    //计算Q_score的公式:Q_score=u*Q_score+(match_tag)/(upVote+1)
                    cm.intersection_Q_score = cm.intersection_Q_score + numberOfMatchedTags / (tempScoreOfThePost + 1.0);
                    // ************************************未加权结束**********************************************
*/

                }// if issueD....
            }// for (i.
            // z-score that considers votes of Q/A:
            cm.intersection_Q_score = NF * cm.intersection_Q_score;
            //***************************尝试设置NF=1，10，20即不要参数u开始****************************
//            cm.intersection_Q_score = 10 * cm.intersection_Q_score;
            //***************************************结束**************************************
            if (cm.intersection_A_score + cm.intersection_Q_score > 0) {
                //该方法分值很高
                cm.intersection_z_score = Math.pow((cm.intersection_A_score - cm.intersection_Q_score), 1 / 2.0) / (Math.pow((cm.intersection_A_score + cm.intersection_Q_score), 2));
                //直接(a-q) / (a+q)
//                cm.intersection_z_score = (cm.intersection_A_score - cm.intersection_Q_score) / (cm.intersection_A_score + cm.intersection_Q_score);
                //作者的方法
//                cm.intersection_z_score = (cm.intersection_A_score - cm.intersection_Q_score) / Math.sqrt(cm.intersection_A_score + cm.intersection_Q_score);
            } else {
                cm.intersection_z_score = 0;
            }


        }// if.
        // indexOfTheCurrentIssue表示该缺陷报告是该项目的第几个缺陷报告
        // For the first issue, we don't have these scores (randomScore,
        // randomWeightedScore and zeroRScore). So all users are the same
        // regarding these scores.
        if (indexOfTheCurrentIssue > 0) {
            // 指在分派该缺陷报告之前，某个成员被分派的缺陷报告数量，对于分派第一份缺陷报告来说，此时numberOfAssignmentsUpToNow=0
            if (cm.numberOfAssignmentsUpToNow > 0) {
                cm.randomScore_zeroOrOne = 1;
                cm.weightedRandomScore_count = cm.numberOfAssignmentsUpToNow;
                if (cm.numberOfAssignmentsUpToNow == maximumNumberOfPreviousAssignmentsForAMemberInThisProject)
                    cm.zeroRScore_zeroOrOne = 1;
                else
                    cm.zeroRScore_zeroOrOne = 0;
            }// if (cm.numb....
        }// if (indexO...
        // cm.combinedSore代表什么意思？如果是RA_SSA_Z_score，那么为什么alpha的值为0.02和0.002，且为什么有两个combinedScore?
        cm.combinedScore1 = cm.weightedRandomScore_count + 0.02
                * cm.intersection_z_score;
        cm.combinedScore2 = cm.weightedRandomScore_count + 0.002
                * cm.intersection_z_score;
    }// calculateScores().

    public static int calTimeOfIssue(String issueDate, String postDate) throws ParseException {
        String issueStart = issueDate.replaceAll("[T,Z]", "");
        String postEnd = postDate.replaceAll("[T,Z]", "");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddhh:mm:ss");
        Date iStart = sdf.parse(issueStart);
        Date pEnd = sdf.parse(postEnd);
        long diffSeconds = (iStart.getTime() - pEnd.getTime()) / 1000;
        double aDouble = Double.parseDouble(diffSeconds + "");
        int day = (int) (aDouble / (24 * 3600));
        return day;
    }

    public static HashMap<String, Double> calculateNumOfTagInIssueText(
            String[] tags, IssueTextualInformation issTI) {
        HashMap<String, Double> hmI = new HashMap<String, Double>();
        HashMap<String, Double> hmD = new HashMap<String, Double>();
        // 将需要比对的BR文本合成一个string并用空格替换标点符号
        String issText = (issTI.projectLanguage + issTI.projectDescription + issTI.issueLabels + issTI.issueTitle + issTI.issueBody).replaceAll("[,\\.，。\\!！《》、;\\:；：\\s]", " ");
        // 空格分词法
        String[] issWord = issText.split(" ");

        //统计iss所有的单词个数
        int issCount = issWord.length;

        double countTotalTag = 0;

        /*定义匹配成功的标签的平均数，即匹配的总数/标签类别数，如Post1与BR匹配的有2个A，3个B，0个C，则标签平均数=5/3
        这个数等价于作者的匹配一次，分数加1
         */
        double meanTagNumOfEveryPost;

        for (int i = 0; i < tags.length; i++) {
            double count = 0;
            for (int j = 0; j < issWord.length; j++) {
                if (tags[i].equals(issWord[j])) {
                    count++;
                    countTotalTag++;
                }
            }
            //方法四：直接用标签数量作为权值
            hmI.put(tags[i], count);
        }
        meanTagNumOfEveryPost = countTotalTag / tags.length;
        // 计算tag的权值
        double weightOfTag;
        for (Map.Entry<String, Double> entry : hmI.entrySet()) {
            //方法1 算标签占改条Post的标签在BR中的总数的权值
//            hmD.put(entry.getKey(), entry.getValue() / countTotalTag);
            //方法2 算标签占BR总数的权值
//            hmD.put(entry.getKey(), entry.getValue() / issCount);
            //方法3
            //以平均次数等价作者的1次，然后看每个标签与平均次数的离散程度
//            weightOfTag = ((entry.getValue() - meanTagNumOfEveryPost) / meanTagNumOfEveryPost) + 1;
//            hmD.put(entry.getKey(), weightOfTag);
        }
//        测试方法2在BR中的词频
//        return hmD;
//        方法3直接用标签的数量做权值
        //测试方法3
//        return hmD;
        //测试方法4
        return hmI;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------------------------------------------------
    public static void extractAndCopyAssigneeValues(ArrayList<Issue> issAL,
                                                    ArrayList<String> allAssignees) {
        // initialize the treeSet with the descending sorting as default
        // ordering (this object is not needed in this method, but will be used
        // in the caller method):
        // allPreviousAssignees_sortedByNumerOfAssignments_Descending = new
        // TreeMap<Integer, String>(new Comparator<Integer>(){
        // public int compare(Integer s1, Integer s2){//We want the descending
        // order of number:
        // if (s1 < s2)
        // return 1;
        // else
        // if (s1 > s2)
        // return -1;
        // else
        // return 0;
        // }//compare().
        // });//new TreeMap<....

        // allAssignees = new ArrayList<String>();
        for (int p = 0; p < issAL.size(); p++) {
            Issue iss = issAL.get(p);
            allAssignees.add(iss.assigneeLogin);
        }
    }// extractAndCopyAssigneeValues().
    // -----------------------------------------------------------------------------------------------------------------------------------------------------
    // This method gives you rank of real assignee "randomly" with chances
    // based on different scores for different users:

    public static void determineRankOfRealAssigneeRandomlyBasedOnScores(
            HashMap<String, Scores> usersAndScores,
            CommunityMember communityMemberThatHasBeenAssignedToThisIssue,
            CommunityMembersRankingResult assigneeRank, double[] sumOfScores,
            int numberOfRanksConsideredBefore, int startingScoreIndex,
            int endingScoreIndex) {
        for (int i = startingScoreIndex; i <= endingScoreIndex; i++) {
            int aRank = 0;
            Random random = new Random();
            for (int j = 0; j < usersAndScores.size()
                    && assigneeRank.differentRankings[i] == -1; j++) {
                double rand = (sumOfScores[i]) * random.nextDouble();
                for (String aLogin : usersAndScores.keySet()) {
                    Scores rs = usersAndScores.get(aLogin);
                    rand = rand - rs.differentScores[i];
                    if (rand < 0) {
                        aRank++;
                        if (communityMemberThatHasBeenAssignedToThisIssue.ghLogin
                                .equals(aLogin))
                            assigneeRank.differentRankings[i] = numberOfRanksConsideredBefore
                                    + aRank;
                        else {
                            sumOfScores[i] = sumOfScores[i]
                                    - rs.differentScores[i];
                            rs.differentScores[i] = 0;
                            usersAndScores.put(aLogin, rs);
                        }
                        break;
                    }// if (randomV....
                }// for (String aLogi....
            }// for (j.
        }// for (i.
    }// determineRankOfRealAssignee().
    // ---------------------------------------------------------------------------------------------------------------------------------------
    // This method gives you rank of real assignee "randomly" with chances
    // based on different scores for different users:

    public static void determineRankOfRealAssignee_FirstStaticallyThenRandomly_BasedOnScores(
            HashMap<String, Scores> usersAndScores,
            CommunityMember communityMemberThatHasBeenAssignedToThisIssue,
            CommunityMembersRankingResult assigneeRank,
            int numberOfRanksConsideredBefore, int startingScoreIndex,
            int endingScoreIndex) {
        if (usersAndScores
                .containsKey(communityMemberThatHasBeenAssignedToThisIssue.ghLogin)) { // Means
            // that
            // the
            // real
            // assignee
            // is
            // in
            // usersAndScores
            // object
            // --that
            // contains
            // the
            // scores
            // of
            // previous
            // assignees--,
            // otherwise
            // we
            // don't
            // assign
            // any
            // rank
            // to
            // the
            // real
            // assignee
            // (should
            // remain
            // the
            // same
            // '-1')
            for (int i = startingScoreIndex; i <= endingScoreIndex; i++) {
                Random random = new Random();
                int numberOfMembersWithGreaterScore = 0;
                int numberOfMembersWithEqualScore = 0;
                Scores scoresOfTheRealAssignee = usersAndScores
                        .get(communityMemberThatHasBeenAssignedToThisIssue.ghLogin);
                for (String aLogin : usersAndScores.keySet()) {
                    Scores scoresOfALogin = usersAndScores.get(aLogin);
                    if (scoresOfALogin.differentScores[i] > scoresOfTheRealAssignee.differentScores[i])
                        numberOfMembersWithGreaterScore++;
                    else
                        // if ((scoresOfALogin.differentScores[i] ==
                        // scoresOfTheRealAssignee.differentScores[i]) &&
                        // (scoresOfALogin.differentScores[i] > 0))
                        if (scoresOfALogin.differentScores[i] == scoresOfTheRealAssignee.differentScores[i])
                            numberOfMembersWithEqualScore++;
                }// for (aLogin.
                //随机排名
                if (numberOfMembersWithEqualScore > 0) {
                    assigneeRank.differentRankings[i] = numberOfRanksConsideredBefore
                            + numberOfMembersWithGreaterScore
                            + random.nextInt(numberOfMembersWithEqualScore) + 1;
                } else if (numberOfMembersWithEqualScore == 0) {
                    assigneeRank.differentRankings[i] = numberOfRanksConsideredBefore
                            + numberOfMembersWithGreaterScore
                            + 1;
                }
//  取并列排名
//                assigneeRank.differentRankings[i] = numberOfRanksConsideredBefore
//                            + numberOfMembersWithGreaterScore
//                            + 1;


            }// for (i.
        }// if (users....
    }// determineRankOfRealAssignee().
    // ---------------------------------------------------------------------------------------------------------------------------------------

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

    // ---------------------------------------------------------------------------------------------------------------------------------------
    public static void mergeWith_z_score(ArrayList<CommunityMember> cmAL,
                                         HashMap<String, Scores> usersAndScores, int startingScoreIndex,
                                         int endingScoreIndex) {
        // Adding z-scores to scores of my metrics (from index 4 to higher):
        double[] minimumScore = new double[Constants.TOTAL_NUMBER_OF_METRICS];
        for (int i = startingScoreIndex; i <= endingScoreIndex; i++)
            minimumScore[i] = Constants.A_FAR_LARGESCORE;// 从index=4开始设置初始值100,0000
        /*
         * 循环该项目中的所有成员，如果该成员已分派了缺陷报告，那么得到该成员的score，并计算其RA_SSA_Z_score，然后更新该成员的分数
		 * 那对于之前没有分派到缺陷报告的成员，就没有分数？
		 */
        for (int k = 0; k < cmAL.size(); k++) {
            CommunityMember cm = cmAL.get(k);
            if (usersAndScores.containsKey(cm.ghLogin)) {
                Scores rs = usersAndScores.get(cm.ghLogin);
                for (int i = startingScoreIndex; i <= endingScoreIndex; i++) {
                    // Social
                    // Z_Score:计算RA_SSA_Z_score，此处Z_SCORE_COEFFICIENTS表示alpha，beta为1，对于第一条缺陷报告来说，所有的成员都没有分派缺陷报告，因此为0
                    //不加beta
//                    rs.differentScores[i] = rs.differentScores[i] + Constants.Z_SCORE_COEFFICIENTS[i] * cm.intersection_z_score;
                    //加beta
                    rs.differentScores[i] = (1 - Constants.Z_SCORE_COEFFICIENTS[i]) * rs.differentScores[i] + Constants.Z_SCORE_COEFFICIENTS[i] * cm.intersection_z_score;
                    // Traditional Z_Score: 33333333333
                    // rs.differentScores[i] = rs.differentScores[i] +
                    // Constants.Z_SCORE_COEFFICIENTS[i] *
                    // cm.traditional_z_score;

                    if (rs.differentScores[i] < minimumScore[i])
                        minimumScore[i] = rs.differentScores[i];
                }// for (i.
                usersAndScores.put(cm.ghLogin, rs);
            }// if (allPr....
        }// for (k
        // Because of z-score, some of these scores can be negative. In
        // these cases, we'll subtract the minimum of them [that is
        // negative] from all the scores (that is actually adding a positive
        // value to all):
        for (int i = startingScoreIndex; i <= endingScoreIndex; i++)
            if (minimumScore[i] < 0) {// Add minimumScores to minimumScore2:
                for (String aLogin : usersAndScores.keySet()) {
                    Scores rs = usersAndScores.get(aLogin);
                    rs.differentScores[i] = rs.differentScores[i] - minimumScore[i];
                    usersAndScores.put(aLogin, rs);
                }// for (k
            }// if (minim....
    }// mergeWith_z_score().
    // ---------------------------------------------------------------------------------------------------------------------------------------

    public static void calculateSums(HashMap<String, Scores> usersAndScores,
                                     double[] sumOfScores, int startingScoreIndex, int endingScoreIndex) {
        for (int i = startingScoreIndex; i <= endingScoreIndex; i++)
            sumOfScores[i] = 0;
        for (Scores rs : usersAndScores.values())
            for (int i = startingScoreIndex; i <= endingScoreIndex; i++)
                sumOfScores[i] = sumOfScores[i] + rs.differentScores[i];
    }// calculateSums().
    // ---------------------------------------------------------------------------------------------------------------------------------------

    public static void copyUsersWithZeroAssignmentPlusSOScoreToHashMapOfArrayList(
            HashMap<String, Scores> usersAndScores,
            HashMap<Integer, ArrayList<String>> usersWithZeroAssignmentPlusSOScore,
            int startingScoreIndex, int endingScoreIndex) {
        for (String aLogin : usersAndScores.keySet()) {
            Scores rs = usersAndScores.get(aLogin);
            for (int i = startingScoreIndex; i <= endingScoreIndex; i++)
                if (rs.differentScores[i] <= 0) {
                    if (usersWithZeroAssignmentPlusSOScore.containsKey(i)) {
                        usersWithZeroAssignmentPlusSOScore.get(i).add(aLogin);
                    } else {
                        ArrayList<String> anArrayList = new ArrayList<String>();
                        anArrayList.add(aLogin);
                        usersWithZeroAssignmentPlusSOScore.put(i, anArrayList);
                    }// else.
                }
        }// for aLogin.
    }// copyUsersWithZeroAssignmentPlusSOScoreToHashMapOfArrayList().

    // ---------------------------------------------------------------------------------------------------------------------------------------
//************************************缺陷修复工作时效性开始*****************************************
    public static void testForBRTime(ArrayList<CommunityMember> cmAL, ArrayList<String> allAssignees,
                                     int indexOfTheCurrentIssue, HashMap<String, Integer> allPreviousAssignees_unique,
                                     CommunityMember communityMemberThatHasBeenAssignedToThisIssue,
                                     CommunityMembersRankingResult assigneeRank, ArrayList<String> createdDateOfBR) {
        HashMap<String, Scores> allPreviousAssigneesAndTheirChances = new HashMap<String, Scores>();
        ArrayList<String> silentUsers = new ArrayList<String>();

        ArrayList<String> silentUsers2_forZeroR = new ArrayList<String>();
        int numberOfUsersWithZeroREqualToOne = 0;
        for (int i = 0; i < Constants.TOTAL_NUMBER_OF_METRICS; i++) {
            assigneeRank.differentRankings[i] = -1;
        }

        double[] sumOfScores = new double[Constants.TOTAL_NUMBER_OF_METRICS];
        for (int i = 0; i < Constants.TOTAL_NUMBER_OF_METRICS; i++) {
            sumOfScores[i] = 0;
        }
        //将第一条缺陷报告的处理方式和其他缺陷报告的处理方式分开，因为第一条缺陷报告对应的候选修复者都没有缺陷修复工作时效性分数
        if (indexOfTheCurrentIssue > 0) {
            int numberOfUsersWithZeroRValueEqualToOne = 0;
            for (int k = 0; k < cmAL.size(); k++) {
                if (cmAL.get(k).zeroRScore_zeroOrOne == 1) {
                    numberOfUsersWithZeroRValueEqualToOne++;
                }
            }
            //该for循环将当前BR的所有候选者分成了有修复经验的和无修复经验的
            for (int k = 0; k < cmAL.size(); k++) {
                CommunityMember cm = cmAL.get(k);
                if (cm.randomScore_zeroOrOne == 1) {
                    Scores rs = new Scores();
                    rs.differentScores[0] = (double) 1 / allPreviousAssignees_unique.size();
                    sumOfScores[0] = sumOfScores[0] + rs.differentScores[0];
                    if (cm.zeroRScore_zeroOrOne == 1) {
                        rs.differentScores[2] = (double) 1 / numberOfUsersWithZeroRValueEqualToOne;
                        sumOfScores[2] = sumOfScores[2] + rs.differentScores[2];
                        numberOfUsersWithZeroREqualToOne++;
                    } else {
                        silentUsers2_forZeroR.add(cm.ghLogin);
                    }
                    rs.differentScores[1] = (double) cm.weightedRandomScore_count / indexOfTheCurrentIssue;
                    sumOfScores[1] = sumOfScores[1] + rs.differentScores[1];
                    allPreviousAssigneesAndTheirChances.put(cm.ghLogin, rs);
                } else {
                    silentUsers.add(cm.ghLogin);
                    silentUsers2_forZeroR.add(cm.ghLogin);
                }

            }
            //计算缺陷修复工作时效性
            String aTempLogin;
            for (int p = 0; p < indexOfTheCurrentIssue; p++) {
                aTempLogin = allAssignees.get(p);
                //获取其SSA_Z_score
                Scores rs = allPreviousAssigneesAndTheirChances.get(aTempLogin);
                //计算已修复过的缺陷报告和待修复缺陷报告的时间差
                int timeDay = 0;
                try {
                    timeDay = calTimeOfIssue(createdDateOfBR.get(indexOfTheCurrentIssue), createdDateOfBR.get(p));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //以平均相差天数作为衡量分数的指标
//                double recencyScore = (double) 1 / (1 + (timeDay / 365));
//                double recencyScore = (double) 1 / (1 + (timeDay / 90));
//                double recencyScore = (double) 1 / (1 + (timeDay / 180));
//                double recencyScore = (double) 1 / (1 + (timeDay / 60));
//                double recencyScore = (double) 1 / (1 + (timeDay / 30));
//                double recencyScore = (double) 1 / (1 + (timeDay / 15));
//                double recencyScore = (double) 1 / (1 + (timeDay / 7));
                double recencyScore = (double) 1 / (1 + timeDay);

                for (int i = 3; i < Constants.TOTAL_NUMBER_OF_METRICS; i++) {
                    rs.differentScores[i] = rs.differentScores[i] + recencyScore;
                }
                allPreviousAssigneesAndTheirChances.put(aTempLogin, rs);
            }
            mergeWith_timeAndSSA_Z_score(cmAL, allPreviousAssigneesAndTheirChances, 4, Constants.TOTAL_NUMBER_OF_METRICS - 1);
            calculateSums(allPreviousAssigneesAndTheirChances, sumOfScores, 3, Constants.TOTAL_NUMBER_OF_METRICS - 1);
            HashMap<Integer, ArrayList<String>> allPreviousAssigneesWithZeroAssignmentPlusSOScore = new HashMap<Integer, ArrayList<String>>();
            copyUsersWithZeroAssignmentPlusSOScoreToHashMapOfArrayList(
                    allPreviousAssigneesAndTheirChances,
                    allPreviousAssigneesWithZeroAssignmentPlusSOScore, 4,
                    Constants.TOTAL_NUMBER_OF_METRICS - 1);
            determineRankOfRealAssigneeRandomlyBasedOnScores(
                    allPreviousAssigneesAndTheirChances,
                    communityMemberThatHasBeenAssignedToThisIssue,
                    assigneeRank, sumOfScores, 0, 0, 0);
            determineRankOfRealAssigneeRandomlyBasedOnScores(
                    allPreviousAssigneesAndTheirChances,
                    communityMemberThatHasBeenAssignedToThisIssue,
                    assigneeRank, sumOfScores, 0, 1, 1);
            determineRankOfRealAssigneeRandomlyBasedOnScores(
                    allPreviousAssigneesAndTheirChances,
                    communityMemberThatHasBeenAssignedToThisIssue,
                    assigneeRank, sumOfScores, 0, 2, 2);
            determineRankOfRealAssigneeRandomlyBasedOnScores(
                    allPreviousAssigneesAndTheirChances,
                    communityMemberThatHasBeenAssignedToThisIssue,
                    assigneeRank, sumOfScores, 0, 3, 3);
            determineRankOfRealAssignee_FirstStaticallyThenRandomly_BasedOnScores(
                    allPreviousAssigneesAndTheirChances,
                    communityMemberThatHasBeenAssignedToThisIssue,
                    assigneeRank, 0, 4, Constants.TOTAL_NUMBER_OF_METRICS - 1);
            for (int i = 4; i < Constants.TOTAL_NUMBER_OF_METRICS; i++) {
                ArrayList<String> usersWithZeroAssignmentPlusSOScore_accordingToAScoringType = allPreviousAssigneesWithZeroAssignmentPlusSOScore.get(i);
                if ((usersWithZeroAssignmentPlusSOScore_accordingToAScoringType != null)
                        && (usersWithZeroAssignmentPlusSOScore_accordingToAScoringType.size() > 0)) {
                    determineRankOfRealAssigneeCompletelyRandomly(
                            usersWithZeroAssignmentPlusSOScore_accordingToAScoringType, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank,
                            allPreviousAssigneesAndTheirChances.size() - usersWithZeroAssignmentPlusSOScore_accordingToAScoringType.size(), i, i);
                }
            }
        } else {
            for (int k = 0; k < cmAL.size(); k++) {
                CommunityMember cm = cmAL.get(k);
                silentUsers.add(cm.ghLogin);
                silentUsers2_forZeroR.add(cm.ghLogin);
            }
        }
        determineRankOfRealAssigneeCompletelyRandomly(silentUsers,
                communityMemberThatHasBeenAssignedToThisIssue, assigneeRank,
                allPreviousAssigneesAndTheirChances.size(), 0, 0);
        determineRankOfRealAssigneeCompletelyRandomly(silentUsers,
                communityMemberThatHasBeenAssignedToThisIssue, assigneeRank,
                allPreviousAssigneesAndTheirChances.size(), 1, 1);
        determineRankOfRealAssigneeCompletelyRandomly(silentUsers2_forZeroR,
                communityMemberThatHasBeenAssignedToThisIssue, assigneeRank,
                numberOfUsersWithZeroREqualToOne, 2, 2);
        determineRankOfRealAssigneeCompletelyRandomly(silentUsers,
                communityMemberThatHasBeenAssignedToThisIssue, assigneeRank,
                allPreviousAssigneesAndTheirChances.size(), 3, 3);

        HashMap<String, Scores> allSilentUsersAndTheirChances = new HashMap<String, Scores>();
        for (String aLogin : silentUsers) {
            Scores rs = new Scores();
            allSilentUsersAndTheirChances.put(aLogin, rs);
        }
        mergeWith_timeAndSSA_Z_score(cmAL, allSilentUsersAndTheirChances, 4, Constants.TOTAL_NUMBER_OF_METRICS - 1);
        //计算前四种分数的时候有用
        calculateSums(allSilentUsersAndTheirChances, sumOfScores, 4, Constants.TOTAL_NUMBER_OF_METRICS - 1);
        HashMap<Integer, ArrayList<String>> allSilentUsersWithZeroAssignmentPlusSOScore = new HashMap<Integer, ArrayList<String>>();
        copyUsersWithZeroAssignmentPlusSOScoreToHashMapOfArrayList(
                allSilentUsersAndTheirChances,
                allSilentUsersWithZeroAssignmentPlusSOScore, 4,
                Constants.TOTAL_NUMBER_OF_METRICS - 1);
        determineRankOfRealAssignee_FirstStaticallyThenRandomly_BasedOnScores(
                allSilentUsersAndTheirChances,
                communityMemberThatHasBeenAssignedToThisIssue, assigneeRank,
                allPreviousAssigneesAndTheirChances.size(), 4,
                Constants.TOTAL_NUMBER_OF_METRICS - 1);
    }

    public static void mergeWith_timeAndSSA_Z_score(ArrayList<CommunityMember> cmAL, HashMap<String, Scores> usersAndScores, int startingScoreIndex, int endingScoreIndex) {
        double[] minimumScore = new double[Constants.TOTAL_NUMBER_OF_METRICS];
        for (int i = startingScoreIndex; i <= endingScoreIndex; i++) {
            minimumScore[i] = Constants.A_FAR_LARGESCORE;
        }

        for (int k = 0; k < cmAL.size(); k++) {
            CommunityMember cm = cmAL.get(k);
            if (usersAndScores.containsKey(cm.ghLogin)) {
                Scores rs = usersAndScores.get(cm.ghLogin);
                for (int i = startingScoreIndex; i <= endingScoreIndex; i++) {
//                    rs.differentScores[i] = rs.differentScores[i] + Constants.Z_SCORE_COEFFICIENTS[i] * cm.intersection_z_score;
                    //加上beta
                    rs.differentScores[i] = (1 - Constants.Z_SCORE_COEFFICIENTS[i]) * rs.differentScores[i] + Constants.Z_SCORE_COEFFICIENTS[i] * cm.intersection_z_score;
                    if (rs.differentScores[i] < minimumScore[i]) {
                        minimumScore[i] = rs.differentScores[i];
                    }
                }
                //更新分数
                usersAndScores.put(cm.ghLogin, rs);
            }
        }
        for (int i = startingScoreIndex; i <= endingScoreIndex; i++) {
            if (minimumScore[i] < 0) {
                for (String aLogin : usersAndScores.keySet()) {
                    Scores rs = usersAndScores.get(aLogin);
                    rs.differentScores[i] = rs.differentScores[i] - minimumScore[i];
                    usersAndScores.put(aLogin, rs);
                }
            }
        }
    }

    //************************************缺陷修复工作时效性结束*****************************************
    public static void determineAssigneeRanksRandomly(
            ArrayList<CommunityMember> cmAL, ArrayList<String> allAssignees,
            int indexOfTheCurrentIssue,
            HashMap<String, Integer> allPreviousAssignees_unique,
            CommunityMember communityMemberThatHasBeenAssignedToThisIssue,
            CommunityMembersRankingResult assigneeRank) {
        // First, give the chance to the previous assignees, randomly:
        HashMap<String, Scores> allPreviousAssigneesAndTheirChances = new HashMap<String, Scores>();
        // silentUsers表示在此条缺陷报告分派之前，并没有被分派到任何缺陷报告
        ArrayList<String> silentUsers = new ArrayList<String>();
        ArrayList<String> silentUsers2_forZeroR = new ArrayList<String>();
        int numberOfUsersWithZeroREqualToOne = 0;
        // int[] numberOfRanksConsideredBefore = new
        // int[Constants.TOTAL_NUMBER_OF_METRICS];
        // 将assigneeRank的differentRankings均初始化值为-1
        for (int i = 0; i < Constants.TOTAL_NUMBER_OF_METRICS; i++) {
            assigneeRank.differentRankings[i] = -1; // To check (later, in the
            // next loop) if the user
            // gets his rank in the
            // first loop (considering
            // previous assignees).
            // numberOfRanksConsideredBefore[i] = 0;
        }
        // 定义sumOfScores数组，长度为18，初始化为0
        double[] sumOfScores = new double[Constants.TOTAL_NUMBER_OF_METRICS];
        for (int i = 0; i < Constants.TOTAL_NUMBER_OF_METRICS; i++)
            sumOfScores[i] = 0;
        /**
         * start取消注释
         */
        // indexOfTheCurrentIssue>0表示这不是第一条缺陷报告
        if (indexOfTheCurrentIssue > 0) {
            // Determine Scores.randomScore, Scores.weightedRandomScore and
            // Scores.zeroRScore:
            int numberOfUsersWithZeroRValueEqualToOne = 0;// 表示ZeroR的值等于1的成员个数
            // 该for循环的作用是为了统计该项目成员中ZeroRValue==1的成员个数
            for (int k = 0; k < cmAL.size(); k++)
                if (cmAL.get(k).zeroRScore_zeroOrOne == 1)// 表示该项目某个成员分派到的缺陷报告数量=该项目已分派的缺陷报告数量
                    numberOfUsersWithZeroRValueEqualToOne++;
            /*
             * 该for循环的作用是为了将该项目所有成员划分为silentUsers和allPreviousAssigneesAndTheirChances
			 * 如果是allPreviousAssigneesAndTheirChances
			 * ，则计算该成员的random\wRandom\zeroR值
			 */

            for (int k = 0; k < cmAL.size(); k++) {
                CommunityMember cm = cmAL.get(k);
                if (cm.randomScore_zeroOrOne == 1) {// 表示在排序时，该成员所在项目已分派的缺陷报告数量大于0
                    Scores rs = new Scores();
                    rs.differentScores[0] = (double) 1
                            / allPreviousAssignees_unique.size();// 好像是该成员的random分数
                    sumOfScores[0] = sumOfScores[0] + rs.differentScores[0];
                    if (cm.zeroRScore_zeroOrOne == 1) {
                        rs.differentScores[2] = (double) 1
                                / numberOfUsersWithZeroRValueEqualToOne;// 好像是该成员的zeroR分数
                        sumOfScores[2] = sumOfScores[2] + rs.differentScores[2];
                        numberOfUsersWithZeroREqualToOne++;
                    }// if (cm.zeroR....
                    else
                        silentUsers2_forZeroR.add(cm.ghLogin);
                    rs.differentScores[1] = (double) cm.weightedRandomScore_count
                            / indexOfTheCurrentIssue;// 好像是该成员的wRandom分数
                    sumOfScores[1] = sumOfScores[1] + rs.differentScores[1];
                    allPreviousAssigneesAndTheirChances.put(cm.ghLogin, rs);
                }// if (cm.rand....
                else {// 如何在这份缺陷报告之前，该成员未被分派到缺陷报告，则将该成员归为silentUsers
                    silentUsers.add(cm.ghLogin);
                    silentUsers2_forZeroR.add(cm.ghLogin);
                }// else.
            }// for (k.
            // Determining Scores.weightedRandomScore2_myMetric (//Recency
            // metric?:):
            String aTempLogin;
            for (int p = 0; p < indexOfTheCurrentIssue; p++) {
                // 依次获取该项目之前已经分派过的缺陷报告对应的assigneeLogin信息
                aTempLogin = allAssignees.get(p);
                // 既然是分派过的缺陷报告，那么该缺陷报告对应的assigneeLogin对应的ghLogin就存放在allPreviousAssigneesAndTheirChances
                Scores rs = allPreviousAssigneesAndTheirChances.get(aTempLogin);
                double recencyScore = (double) 1
                        / (indexOfTheCurrentIssue - p + Constants.UNIMPORTANCE_OF_RECENT_ASSIGNMENTS);
                /*
                 * 将3-17的differentScores均赋值为recencyScore
				 * 分派第二份缺陷报告时，indexOfTheCurrentIssue
				 * =1,differentScores=1/(1+0.0)=1.0
				 * 分派第三份缺陷报告时，indexOfTheCurrentIssue
				 * =2,differentScores=1/(2+1+0.0)=1/(2+1)
				 * 分派第三份缺陷报告时，indexOfTheCurrentIssue
				 * =3,differentScores=(1/(3+2+1+0.0)=1/(3+2+1)
				 */

                // 因为每个候选修复者都有17个分数，此处是表明从第四个分数开始，该候选者的分数在原有分数基础上全部加上recencyScore
                for (int i = 3; i < Constants.TOTAL_NUMBER_OF_METRICS; i++)
                    rs.differentScores[i] = rs.differentScores[i]
                            + recencyScore;
                allPreviousAssigneesAndTheirChances.put(aTempLogin, rs);
            }// for (p.
            // (该项目的所有成员，已分派到缺陷报告的成员的ghLogin和分数，4起始索引，17截止索引)
            mergeWith_z_score(cmAL, allPreviousAssigneesAndTheirChances, 4,
                    Constants.TOTAL_NUMBER_OF_METRICS - 1);
            // （已分派到缺陷报告的成员的ghLogin和分数，sumOfScores，3起始索引，17截止索引）
            calculateSums(allPreviousAssigneesAndTheirChances, sumOfScores, 3,
                    Constants.TOTAL_NUMBER_OF_METRICS - 1);// For indices 0, 1
            // and 2, we've
            // already
            // calculated the
            // sums before.

            HashMap<Integer, ArrayList<String>> allPreviousAssigneesWithZeroAssignmentPlusSOScore = new HashMap<Integer, ArrayList<String>>();
            copyUsersWithZeroAssignmentPlusSOScoreToHashMapOfArrayList(
                    allPreviousAssigneesAndTheirChances,
                    allPreviousAssigneesWithZeroAssignmentPlusSOScore, 4,
                    Constants.TOTAL_NUMBER_OF_METRICS - 1);

            // Ranking based on random generated double number (until passing
            // over all candidates, or, choosing the currently assigned user):
            // Part 1/5, determining assigneeRank1_random2:
            determineRankOfRealAssigneeRandomlyBasedOnScores(
                    allPreviousAssigneesAndTheirChances,
                    communityMemberThatHasBeenAssignedToThisIssue,
                    assigneeRank, sumOfScores, 0, 0, 0);
            // Part 2/5, determining assigneeRank1_weightedRandom2:
            determineRankOfRealAssigneeRandomlyBasedOnScores(
                    allPreviousAssigneesAndTheirChances,
                    communityMemberThatHasBeenAssignedToThisIssue,
                    assigneeRank, sumOfScores, 0, 1, 1);
            // Part 3/5, determining assigneeRank1_zeroR2:
            determineRankOfRealAssigneeRandomlyBasedOnScores(
                    allPreviousAssigneesAndTheirChances,
                    communityMemberThatHasBeenAssignedToThisIssue,
                    assigneeRank, sumOfScores, 0, 2, 2);
            // Part 4/5, determining
            // assigneeRank1_combinedRank3_weightedRandom_Recency:
            determineRankOfRealAssigneeRandomlyBasedOnScores(
                    allPreviousAssigneesAndTheirChances,
                    communityMemberThatHasBeenAssignedToThisIssue,
                    assigneeRank, sumOfScores, 0, 3, 3);
            // Part 5/5, determining
            // assigneeRank1_combinedRank4_weightedRandom_Recency_zScore:
            // determineRankOfRealAssigneeRandomlyBasedOnScores(allPreviousAssigneesAndTheirChances,
            // communityMemberThatHasBeenAssignedToThisIssue, assigneeRank,
            // sumOfScores, 0, 4, Constants.TOTAL_NUMBER_OF_METRICS-1);
            determineRankOfRealAssignee_FirstStaticallyThenRandomly_BasedOnScores(
                    allPreviousAssigneesAndTheirChances,
                    communityMemberThatHasBeenAssignedToThisIssue,
                    assigneeRank, 0, 4, Constants.TOTAL_NUMBER_OF_METRICS - 1);
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
            }// for (i.
        }// if (indexO....
        else
        // Means that this is the first issue that is being
        // triaged:如果是第一条缺陷报告，则执行下面的代码
        /**
         * end取消注释
         */

        /**
         * 此处原本没有注释；for循环表示，这是第一条缺陷报告，那么之前所有的项目成员都没有分派到缺陷报告，
         * 所以这些成员都是silentUser
         */
            for (int k = 0; k < cmAL.size(); k++) {
                CommunityMember cm = cmAL.get(k);
                silentUsers.add(cm.ghLogin);
                silentUsers2_forZeroR.add(cm.ghLogin);
            }// for (k.

        // Now if the real assignee was not in the previous assignees (this is
        // checked in the following method), give the chance to the others
        // ("silent users") randomly with the same chance:
        // Step 1/5: determining assigneeRank1_random2:
        /**
         * start取消注释
         */
        determineRankOfRealAssigneeCompletelyRandomly(silentUsers,
                communityMemberThatHasBeenAssignedToThisIssue, assigneeRank,
                allPreviousAssigneesAndTheirChances.size(), 0, 0);
        // Step 2/5: determining assigneeRank1_weightedRandom2:
        determineRankOfRealAssigneeCompletelyRandomly(silentUsers,
                communityMemberThatHasBeenAssignedToThisIssue, assigneeRank,
                allPreviousAssigneesAndTheirChances.size(), 1, 1);
        // Step 3/5: determining assigneeRank1_zeroR2:
        determineRankOfRealAssigneeCompletelyRandomly(silentUsers2_forZeroR,
                communityMemberThatHasBeenAssignedToThisIssue, assigneeRank,
                numberOfUsersWithZeroREqualToOne, 2, 2);
        // Step 4/5: determining assigneeRank1_weightedRandom2_myMetric1:
        determineRankOfRealAssigneeCompletelyRandomly(silentUsers,
                communityMemberThatHasBeenAssignedToThisIssue, assigneeRank,
                allPreviousAssigneesAndTheirChances.size(), 3, 3);
        /**
         * end取消注释
         */
        // Step 5/5: determining assigneeRank1_weightedRandom2_myMetric2:
        HashMap<String, Scores> allSilentUsersAndTheirChances = new HashMap<String, Scores>();
        // ArrayList<String> allSilentUsers_zeroSOScore = new
        // ArrayList<String>();
        for (String aLogin : silentUsers) {
            Scores rs = new Scores();
            allSilentUsersAndTheirChances.put(aLogin, rs);
        }// for (String aLogin.
        mergeWith_z_score(cmAL, allSilentUsersAndTheirChances, 4,
                Constants.TOTAL_NUMBER_OF_METRICS - 1);
        calculateSums(allSilentUsersAndTheirChances, sumOfScores, 4,
                Constants.TOTAL_NUMBER_OF_METRICS - 1);

        HashMap<Integer, ArrayList<String>> allSilentUsersWithZeroAssignmentPlusSOScore = new HashMap<Integer, ArrayList<String>>();
        copyUsersWithZeroAssignmentPlusSOScoreToHashMapOfArrayList(
                allSilentUsersAndTheirChances,
                allSilentUsersWithZeroAssignmentPlusSOScore, 4,
                Constants.TOTAL_NUMBER_OF_METRICS - 1);
        // determineRankOfRealAssigneeRandomlyBasedOnScores(allSilentUsersAndTheirChances,
        // communityMemberThatHasBeenAssignedToThisIssue, assigneeRank,
        // sumOfScores, allPreviousAssigneesAndTheirChances.size(), 4,
        // Constants.TOTAL_NUMBER_OF_METRICS-1);
        determineRankOfRealAssignee_FirstStaticallyThenRandomly_BasedOnScores(
                allSilentUsersAndTheirChances,
                communityMemberThatHasBeenAssignedToThisIssue, assigneeRank,
                allPreviousAssigneesAndTheirChances.size(), 4,
                Constants.TOTAL_NUMBER_OF_METRICS - 1);
    }// determineAssigneeRanksRandomly().
    // -----------------------------------------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------------------------------------------

    public static void triage2_basedOnMultipleCriteria(
            String communityInputPath, String communityInputTSVFileName,
            String communitiesSummaryInputTSV, String projectsInputPath,
            String projectsInputTSV, String assignedIssuesInputPath,
            String assignedIssuesInputTSV,
            String postsOfCommunityMembersInputPath,
            String postsOfCommunityMembersInputTSV, int topXProjectsToTriage,
            String triageResultsOututPath, String triageOutputFileName,
            int showProgressInterval, long testOrReal) {// This method
        try {
            // Read all communities' projects from summary file (maybe just to
            // double check with the community file):
            // 返回的是tsvfieldHashSet，其值为项目名；保证从summary file中的内容有三列，且项目名是主键
            HashSet<String> ownerLoginAndProjectNames = TSVManipulations
                    .readUniqueFieldFromTSV(communityInputPath,
                            communitiesSummaryInputTSV, 1, 3,
                            ConditionType.NO_CONDITION, 0, "", 0, "",
                            showProgressInterval, Constants.THIS_IS_REAL, 1);
            // Now, read all columns of projects and their members:
            // 返回的是tsvRecordsHashMap，其值为file中的所有值，主键为项目名
            TreeMap<String, String[]> projects = TSVManipulations
                    .readUniqueKeyAndItsValueFromTSV(projectsInputPath,
                            projectsInputTSV, null, 2, 9, Constants.ALL,
                            ConditionType.NO_CONDITION, 0, "", 0, "",
                            showProgressInterval * 20, Constants.THIS_IS_REAL,
                            2);
            // Read info of all posts made by all community members by id:
            // (total: ?)
            // 返回的是tsvRecordsHashMap，其值为post文件中的前六列值，主键为Id
            TreeMap<String, String[]> posts1ById = TSVManipulations
                    .readUniqueKeyAndItsValueFromTSV(
                            postsOfCommunityMembersInputPath,
                            postsOfCommunityMembersInputTSV, null, 0, 9,
                            "0$1$2$3$4$5", ConditionType.NO_CONDITION, 0, "",
                            0, "", showProgressInterval * 10, testOrReal, 3);
            // Read info of all posts made by all community members (by field
            // ownerUserId): (total: 3,642,245)
            // 返回的tsvRecordsHashMap，其值为posts的0$1$2$3$6数据，保证了不存在完全相同的两行数据，主键为OwnerUserId并唯一
            // 问题：那具有相同OwnerUserId的其它数据，不统计？
            TreeMap<String, ArrayList<String[]>> posts2ByOwnerId = TSVManipulations
                    .readNonUniqueKeyAndItsValueFromTSV(
                            postsOfCommunityMembersInputPath,
                            postsOfCommunityMembersInputTSV, null, 2,
                            SortOrder.DEFAULT_FOR_STRING, 9, "0$1$2$3$6",
                            ConditionType.NO_CONDITION, 0, "", 0, "",
                            showProgressInterval * 10, testOrReal, 4);
            // Reading assigned issues in all communities (projects):
            // 返回的tsvRecordsHashMap，其值为issues的"0$5$6$8$9$10"数据，保证了不存在完全相同的两行数据，主键为owner/repo并唯一
            // 问题：具有相同owner/repo的其它数据，不统计？
            TreeMap<String, ArrayList<String[]>> ownerLoginAndProjectName_and_allAssignedIssuesInThisProject = TSVManipulations
                    .readNonUniqueKeyAndItsValueFromTSV(
                            assignedIssuesInputPath, assignedIssuesInputTSV,
                            null, 1, SortOrder.DEFAULT_FOR_STRING, 11,
                            "0$5$6$8$9$10", ConditionType.NO_CONDITION, 0, "",
                            0, "", showProgressInterval * 5, testOrReal, 5);
            // fillStopWords();
            // Reading the community file:
            // 返回的tsvRecordsHashMap，其值为communitiesOf20/3Projects.tsv全部数据，保证了不存在完全相同的两行数据，主键为ownerLogin/projName并唯一
            // 问题：具有相同ownerLogin/projName的其它数据，不统计？
            TreeMap<String, ArrayList<String[]>> ownerLoginAndProjectName_and_ItsCommunityMembers = TSVManipulations
                    .readNonUniqueKeyAndItsValueFromTSV(communityInputPath,
                            communityInputTSVFileName, null, 1,
                            SortOrder.DEFAULT_FOR_STRING, 6, Constants.ALL,
                            ConditionType.NO_CONDITION, 0, "", 0, "",
                            showProgressInterval, testOrReal, 6);

            // Starting from the top numberOfProjectmembers, and writing triage
            // results in output files:
            System.out
                    .println("7- Triaging! And writing the precision in output file ("
                            + triageOutputFileName + "):");
            FileWriter writer = new FileWriter(triageResultsOututPath + "\\"
                    + triageOutputFileName);

            String output = "mySQLProjectId\townerLogin/projName\t#ofCommunityMembers\tissueId\t"
                    + "assigneeSOId\tassigneeGHLogin\tassigneeGHMySQLId\tassigneeGHMongoDBId\t"
                    + "aR0:random\taR1:wRandom\taR2:zeroR\taR3:wR+Recency\taR4:Recency+zScore\t";
            for (int j = 5; j < Constants.TOTAL_NUMBER_OF_METRICS; j++)
                output = output
                        + "aR"
                        + j
                        + ":tuning("
                        + Constants.highPrecisionFloatFormatter
                        .format(Constants.Z_SCORE_COEFFICIENTS[j])
                        + ")\t";
            output = output
                    + "aRR0:random\taRR1:wRandom\taRR2:zeroR\taRR3:wR+Recency\taRR4:Recency+zScore\t";
            for (int j = 5; j < Constants.TOTAL_NUMBER_OF_METRICS; j++)
                output = output
                        + "aRR"
                        + j
                        + ":tunning("
                        + Constants.highPrecisionFloatFormatter
                        .format(Constants.Z_SCORE_COEFFICIENTS[j])
                        + ")\t";
            output = output
                    + "projectLanguage\tprojectDescription\tissueTitle\tissueLabels\tissueBody\n";
            writer.append(output);

            Date d0 = new Date();
            int i = 0, projectsTriaged = 0;
            // For all projects (chronologically按时间前后顺序 based on the projects
            // that are in the summary file):
            // 针对ownerLoginAndProjectNames中的每一个项目进行循环操作
            for (String anOwnerLoginAndProjectName : ownerLoginAndProjectNames) {
                if (ownerLoginAndProjectName_and_ItsCommunityMembers
                        .containsKey(anOwnerLoginAndProjectName)) {// This
                    // condition
                    // is just
                    // for
                    // double
                    // checking
                    // the
                    // existence
                    // of the
                    // records
                    // of this
                    // project
                    // (that has
                    // been read
                    // from
                    // communitiesSummary)
                    // with the
                    // info in
                    // the
                    // community
                    // file, and
                    // then
                    // triaging
                    // them:
                    System.out.println("    - Proj " + (i + 1)
                            + ") Started triaging in project \""
                            + anOwnerLoginAndProjectName + "\"");
                    Date d1 = new Date();
                    // Reading community members:
                    // cmAL中保存的是来自communitiesOf20/3Projects.tsv文件中的某个项目的所有行信息，每行代表一个用户
                    ArrayList<CommunityMember> cmAL = ProvideData
                            .copyCommunityFromArrayListOfStringArray(ownerLoginAndProjectName_and_ItsCommunityMembers
                                    .get(anOwnerLoginAndProjectName));
                    // 表示cmAL中的每个用户的相关分数值均初始化为0
                    ProvideData.initializeRandomScores(cmAL);
                    // 计算z_score，可以看出github和so平台是通过SOId和OwnerUserId进行的关联
                    ProvideData.calculateTraditionalZ_ScoreParameters(cmAL,
                            posts2ByOwnerId);
                    int maximumNumberOfPreviousAssignmentsForAMemberInThisProject = 0;
                    System.out.println("        Total # of community members: "
                            + cmAL.size());
                    // aProject表示该项目的一行信息
                    Project aProject = ProvideData
                            .copyProjectFromStringArray(projects
                                    .get(anOwnerLoginAndProjectName));
                    // Triage all assigned issues in this
                    // project:issAL存放该项目的所有缺陷报告信息
                    ArrayList<Issue> issAL = ProvideData
                            .copyIssuesFromArrayListOfStringArray_OnlyThereAreSomeFields3(ownerLoginAndProjectName_and_allAssignedIssuesInThisProject
                                    .get(anOwnerLoginAndProjectName));
                    // 按缺陷报告的时间大小排序
                    Collections.sort(issAL);
                    System.out.println("        Total # of issues: " + issAL.size());

                    ArrayList<String> allAssignees = new ArrayList<String>();
                    // TreeMap<Integer, String>
                    // allPreviousAssignees_sortedByAssignmentTimes_Descending =
                    // null;
                    // allAssignees存放issAL中的每条iss的assigneeLogin信息
                    extractAndCopyAssigneeValues(issAL, allAssignees);
                    HashMap<String, Integer> allPreviousAssignees_unique = new HashMap<String, Integer>();
                    //**************************创建一个存放处理过的缺陷报告的提出时间*******************************
                    ArrayList<String> createdDateOfBR = new ArrayList<>();
                    //*****************************************************************************************
                    // 循环该项目的每一条缺陷报告
                    for (int p = 0; p < issAL.size(); p++) {
                        Issue iss = issAL.get(p);
                        //***********************存入当前处理的BR的提出时间***********************
                        createdDateOfBR.add(iss.createdAt);
                        //********************************************************************

                        // issTI表示该条缺陷报告的文本信息，包括项目language、description和缺陷报告label、title、body
                        IssueTextualInformation issTI = new IssueTextualInformation(
                                aProject.language, aProject.description,
                                iss.labels, iss.title, iss.body);
                        // 存放现有数据集中，这条缺陷报告的assigneeLogin=该项目某成员的ghLogin时，返回该ghLogin对应的成员的信息
                        CommunityMember communityMemberThatHasBeenAssignedToThisIssue = getPointerToCommunityMemberOfGHLogin(
                                cmAL, iss.assigneeLogin);
                        // Checking if the assignee is from the community (the
                        // only case that triaging means. Otherwise triaging is
                        // useless because the issue is assigned to neither of
                        // the community members who we are to rank and
                        // recommend them):
                        // 排除ghLogin为空值的情况，确保推荐的修复者有ghLogin值
                        if (communityMemberThatHasBeenAssignedToThisIssue != null) {// Triage:
                            ProvideData.initializeIntersectionWithBugsScores(cmAL);

                            // Finding NF (Normalization Factor归一化因子);
                            // <averageOfUpVotesForQuestionsTaggedWithAtLeastOneOfTagsInMatchedTags_SO_b>:
                            // 333333333333333:
                            // double NF = 1; NF至少保证为1；Harmonic
                            // Mean调和平均值；每一份缺陷报告一个NF，根据缺陷报告和该项目所有的Post来计算NF
                            double NF = calculateHarmonicMeanOfUpVotesForQuestionsTaggedWithAtLeastOneOfTagsInMatchedTags_SO_b(
                                    issTI, posts1ById);

                            // Iterating over all community members, and calculating their scores for this issue:
                            // 循环该项目的每一个成员
                            for (int k = 0; k < cmAL.size(); k++) {
                                // Identifying the posts of this community
                                // member:
                                CommunityMember cm = cmAL.get(k);
                                ArrayList<SOPost> soPostsOfThisCommunityMemberAL = null;
                                // soPostsOfThisCommunityMemberAL存放该成员的所有Post
                                if (posts2ByOwnerId.containsKey(cm.SOId))
                                    soPostsOfThisCommunityMemberAL = ProvideData
                                            .copyPostContentsFromArrayListOfStringArray_OnlyThereAreSomeFields1(posts2ByOwnerId
                                                    .get(cm.SOId));
                                // 第一个方法调用，就缺陷报告的文本信息与某个项目成员的QA信息，计算该成员的分数
                                // （缺陷报告文本信息，缺陷报告创建时间，某项目成员的所有post，所有post信息，某项目成员的信息，第几条缺陷报告，到目前为止该成员所获的已分派缺陷报告数量，NF）
                                calculateScores(
                                        issTI,
                                        iss.createdAt,
                                        soPostsOfThisCommunityMemberAL,
                                        posts1ById,
                                        cm,
                                        p,
                                        maximumNumberOfPreviousAssignmentsForAMemberInThisProject,
                                        NF);
                            }// for (i.
/*
                            //*******************添加calculateScore的排名***********************************
                            TreeMap<String, Integer> scoreRank = new TreeMap<String, Integer>();
                            determineRankBasedOnCalculateScore(communityMemberThatHasBeenAssignedToThisIssue, cmAL, scoreRank);
                            //****************************************************************
*/
                            // System.out.println();
                            CommunityMembersRankingResult assigneeRank = new CommunityMembersRankingResult();
                            // determineAssigneeRanksStatically(communityMemberThatHasBeenAssignedToThisIssue,
                            // cmAL, assigneeRank);

                            // 第二个方法
                            // （该项目的所有成员信息，该项目的所有缺陷报告对应的assigneeLogin，第几条缺陷报告，“之前所有的唯一修复者”，该项目的ghLogin=assigneeLogin，assigneeRank）
//                            determineAssigneeRanksRandomly(cmAL, allAssignees, p, allPreviousAssignees_unique, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank);
                            //*********************************测试缺陷修复工作时效性开始*********************************
                            testForBRTime(cmAL, allAssignees, p, allPreviousAssignees_unique, communityMemberThatHasBeenAssignedToThisIssue, assigneeRank, createdDateOfBR);
                            //*********************************测试缺陷修复工作时效性结束*********************************
                            // containsKey()是map集合中判断是否包含指定键名的方法
                            if (allPreviousAssignees_unique
                                    .containsKey(communityMemberThatHasBeenAssignedToThisIssue.ghLogin)) {
                                int tempNumber = allPreviousAssignees_unique
                                        .get(communityMemberThatHasBeenAssignedToThisIssue.ghLogin);
                                allPreviousAssignees_unique
                                        .put(communityMemberThatHasBeenAssignedToThisIssue.ghLogin,
                                                tempNumber + 1);
                            }// if (allP....
                            else
                                allPreviousAssignees_unique
                                        .put(communityMemberThatHasBeenAssignedToThisIssue.ghLogin,
                                                1);
                            // Creating output string and writing it:
                            // 此处将计算的结果写入tsv文档，从此处可以看出，assigneeRank.differentRankings是待分派缺陷报告的真实修复者对应的排名
                            output = aProject.projectMySQLId + "\t" + aProject.ownerLoginAndProjectName + "\t" + cmAL.size() + "\t" + iss.id + "\t" + communityMemberThatHasBeenAssignedToThisIssue.SOId + "\t" + communityMemberThatHasBeenAssignedToThisIssue.ghLogin + "\t" + communityMemberThatHasBeenAssignedToThisIssue.ghMySQLId + "\t" + communityMemberThatHasBeenAssignedToThisIssue.ghMongoDBId + "\t";
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
                    }// for (int p ...
                    projectsTriaged++;
                    Date d2 = new Date();
                    System.out.println("    Finished in " + Constants.integerFormatter.format((d2.getTime() - d1.getTime()) / 1000) + " seconds (" + Constants.integerFormatter.format(((d2.getTime() - d1.getTime()) / 1000) / 60) + " minutes and " + Constants.integerFormatter.format(((d2.getTime() - d1.getTime()) / 1000) % 60) + " seconds).");
                    System.out.println("-------------------------------------------");
                    if (projectsTriaged >= topXProjectsToTriage)
                        break;
                } else {
                    System.out.println("            Weird error: The communitesSummary file contains project \"" + anOwnerLoginAndProjectName + "\", but the community file does not! (maybe due to \"testOrReal\"=THIS_IS_A_TEST):");
                    break;
                }// else.
                i++;
                if (i % showProgressInterval == 0)
                    System.out.println("    number of numberOfMembers (including all projects in that range) examined: " + Constants.integerFormatter.format(i));
                if (testOrReal > Constants.THIS_IS_REAL)
                    if (i >= testOrReal) // instead of i >= testOrReal (because
                        // we want to assess just the top
                        // project's members for testing)
                        break;
                if (projectsTriaged >= topXProjectsToTriage)
                    break;
            }// for (String anOwn...
            writer.flush();
            writer.close();
            Date d3 = new Date();
            System.out.println("    Finished in " + Constants.integerFormatter.format((d3.getTime() - d0.getTime()) / 1000) + " seconds (" + Constants.integerFormatter.format(((d3.getTime() - d0.getTime()) / 1000) / 3600) + " hours and " + Constants.integerFormatter.format((((d3.getTime() - d0.getTime()) / 1000) % 3600) / 60) + " minutes and " + Constants.integerFormatter.format(((d3.getTime() - d0.getTime()) / 1000) % 60) + " seconds).");
            System.out.println("===========================================");
            // break;
            System.out.println("End of triaging.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }// triage1_basedOnSimpleIntersectionOfIssueTextualFieldsInGHAndTagsInSO().

    public static void determineRankBasedOnCalculateScore(CommunityMember communityMemberThatHasBeenAssignedToThisIssue, ArrayList<CommunityMember> cmAL, TreeMap<String, Integer> scoreRank) {
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
        scoreRank.put(communityMemberThatHasBeenAssignedToThisIssue.ghLogin, rank);
    }
    // -----------------------------------------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------------------------------------------

    public static void main(String[] args) {
        // Test Run (3 projects):
        for (int i = 0; i < 1; i++)
            triage2_basedOnMultipleCriteria(
                    Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
                    "communitiesOf3Projects.tsv",//"communitiesOf3Projects.tsv"
                    "communitiesSummary(3Projects).tsv",// communitiesOf3Projects.tsv","communitiesSummary(3Projects).tsv"
                    Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
                    "projects-top20.tsv",
                    Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
                    "issues2-forTop20Projects.tsv", // “issues2-forTop20Projects.tsv"
                    Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
                    "Posts-madeByCommunityMembers-top20Projects.tsv", 20,
                    Constants.DATASET_DIRECTORY_RECOMMEND_RESULTS,
                    "testMyMethodFor3Projects.tsv",
                    100000, Constants.THIS_IS_REAL);


        //17p
//        for (int i = 0; i < 1; i++)
//            triage2_basedOnMultipleCriteria(
//                    Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                    "communitiesOf17Projects.tsv",
//                    "communitiesSummary(17Projects).tsv",// communitiesOf3Projects.tsv","communitiesSummary(3Projects).tsv"
//                    Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                    "projects-top20.tsv",
//                    Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                    "issues2-forTop20Projects.tsv", // “issues2-forTop20Projects.tsv"
//                    Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                    "Posts-madeByCommunityMembers-top20Projects.tsv", 20,
//                    Constants.DATASET_DIRECTORY_RECOMMEND_RESULTS,
//                    "testMyMethodFor17Projects.tsv",
//                    100000, Constants.THIS_IS_REAL);
//        //20p
//        for (int i = 0; i < 1; i++)
//            triage2_basedOnMultipleCriteria(
//                    Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                    "communitiesOf20Projects.tsv",
//                    "communitiesSummary(20Projects).tsv",// communitiesOf3Projects.tsv","communitiesSummary(3Projects).tsv"
//                    Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                    "projects-top20.tsv",
//                    Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                    "issues2-forTop20Projects.tsv", // “issues2-forTop20Projects.tsv"
//                    Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
//                    "Posts-madeByCommunityMembers-top20Projects.tsv", 20,
//                    Constants.DATASET_DIRECTORY_RECOMMEND_RESULTS,
//                    "testFor20Projects.tsv",
//                    100000, Constants.THIS_IS_REAL);

    }// main().

}
