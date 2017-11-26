package data;

public class CommunityMember {
    /*
     * 构造函数初始化某社区成员的基本信息，同communitiesOf20/3Projects.tsv的字段
     * 包括所在的项目Id、项目名称、SOId、ghLogin、ghMySQLId、ghMongoDBId
     * 问题：1.从intersection_A往后所有的成员变量代表什么意思？
     */
    public CommunityMember(CommunityMember otherCommunityMember){
        this.mySQLProjectId = otherCommunityMember.mySQLProjectId;
        this.ownerLoginAndProjectName = otherCommunityMember.ownerLoginAndProjectName;
        this.SOId = otherCommunityMember.SOId;
        this.ghLogin = otherCommunityMember.ghLogin;
        this.ghMySQLId = otherCommunityMember.ghMySQLId;
        this.ghMongoDBId = otherCommunityMember.ghMongoDBId;
    }//end of constructor.
    public CommunityMember(){
        super();
    }
    public String mySQLProjectId;
    public String ownerLoginAndProjectName;
    public String SOId;
    public String ghLogin;
    public String ghMySQLId;
    public String ghMongoDBId;

    public int intersection_A;//某社区成员的回答数？
    public int intersection_AQ;//某社区成员的回答和提问数？
    public double intersection_A_score;//某社区成员的回答分数，对应公式A_score
    public int intersection_AQ_score;

    public int intersection_Q;//某社区成员的提问数
    public double intersection_Q_score; //Alaki.某社区成员的提问分数，对应公式Q_score
    public double intersection_z_score;//

    public int totalAnswers;
    public int totalQuestions;
    public double traditional_z_score;

    //For greedy approaches, based on only previous assignments:
    public int numberOfAssignmentsUpToNow;
    public int randomScore_zeroOrOne;
    public int weightedRandomScore_count;
    public int zeroRScore_zeroOrOne;

    public double combinedScore1;
    public double combinedScore2;
}
