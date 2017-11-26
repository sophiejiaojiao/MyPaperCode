package bugAssignment;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class Constants {

    public static final String DATASET_DIRECTORY_GH_MongoDB_TSV = "D:\\Program Files\\IntelliJ IDEA Community Edition 2017.2.5\\Data\\Data Sets1\\1-GH\\GH-GHTorrent-Mongodb\\3-TSV";

    //GH - MySQL:
    public static final String DATASET_DIRECTORY_GH_MySQL_TSV = "D:\\Program Files\\IntelliJ IDEA Community Edition 2017.2.5\\Data\\Data Sets1\\1- GH\\GH-MySQL-2014-08-06\\TSV";

    //SO:
    public static final String DATASET_DIRECTORY_SO_WITHOUT_EMAIL_HASH = "D:\\Program Files\\IntelliJ IDEA Community Edition 2017.2.5\\Data\\Data Sets1\\2-SO\\SO-20140502-WithoutEmailHash";

    //GH and SO (merged; common users):
    public static final String DATASET_DIRECTORY_COMMUNITY = "D:\\Program Files\\IntelliJ IDEA Community Edition 2017.2.5\\Data\\Data Sets1\\3- GH and SO (Merged)\\Communities";
    public static final String DATASET_DIRECTORY_RECOMMEND_RESULTS = "D:\\Program Files\\IntelliJ IDEA Community Edition 2017.2.5\\Data\\Data Sets1\\3- GH and SO (Merged)\\TriageResults";
    public static final String DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND = "D:\\Program Files\\IntelliJ IDEA Community Edition 2017.2.5\\Data\\Data Sets1\\3- GH and SO (Merged)\\DataSetsForAssignment";

    //[] means array. {} means an object (In these cases, first, the name of the object comes. Then the fields come delimited by $).
    public static final Map<String, List<String>> USEFUL_FIELDS_IN_JSON_FILES = ImmutableMap.<String, List<String>>builder()
            .put("users", Arrays.asList(new String[]{"id", "login", "email"}))
            .put("users:labels", Arrays.asList(new String[]{"id", "login", "email"}))
            .put("users:FieldsToRemoveInvalidCharacters", Arrays.asList(new String[]{}))

            .put("issues", Arrays.asList(new String[]{"id", "owner", "repo", "user{}id$login", "assignee{}id$login", "created_at", "comments", "[]labels", "title", "body"}))
            .put("issues:labels", Arrays.asList(new String[]{"id", "owner", "repo", "reporterId", "reporterLogin", "assigneeId", "assigneeLogin", "created_at", "numberOfComments", "labels", "title", "body"}))
            .put("issues:FieldsToRemoveInvalidCharacters", Arrays.asList(new String[]{"title", "body"}))

            .put("repos", Arrays.asList(new String[]{"url", "owner{}login", "name", "language", "description", "created_at"}))
            .put("repos:labels", Arrays.asList(new String[]{"url", "login", "name", "language", "description", "created_at"}))
            .put("repos:FieldsToRemoveInvalidCharacters", Arrays.asList(new String[]{"description"}))

            .put("commits", Arrays.asList(new String[]{"committer{}id$login"}))

            .put("issue_comments", Arrays.asList(new String[]{"issue_id", "repo", "user{}id$login", "body"}))
            .put("pull_request_comments", Arrays.asList(new String[]{"id", "user{}id$login", "repo", "body", "diff_hunk"}))
            .put("repo", Arrays.asList(new String[]{"id", "name", "language", "description", "open_issues", "svn_url"}))
            .put("commit_comments", Arrays.asList(new String[]{"id", "repo", "user", "body"})).build();
    //	public static final String validSOTagCharacters = "#*\\-+"; //It is used for regex. So it means: # * - +
    public static final String allValidCharactersInSOTags = "a-zA-Z0-9.#+-"; //Obtained from file "allValidCharactersInSOTags.txt" after running the XMLParser.java.
    public static final String allValidCharactersInSOTags_ForRegEx = "a-zA-Z0-9\\.\\#\\+\\-";  //The same as the above line, but altered for regEx usage.
    public static final String allValidCharactersInSOQUESTION_AND_ANSWER_ForRegEx = "a-zA-Z0-9\\.\\#\\+\\-\\(\\)\\[\\]\\{\\}\\~\\!\\$\\%\\^\\&\\*\\_\\:\\;\\<\\>\\,\\.\\?\\/\\|\\=\\\"\\'\\`\\\\";  //The same as the above line, but for the question / answer text.
    public static final String allValidCharactersInGH_PROJECT_NAMES_ForRegEx = "a-zA-Z0-9\\.\\#\\+\\-\\_";  //The same as the above line, but altered for regEx usage.
    public static final String SEPARATOR_FOR_ARRAY_ITEMS = ";;";
    public static final DecimalFormat integerFormatter = new DecimalFormat("###,###");//"#"表示一个数字，不包括0;","表示分组分隔符的占位符;"."小数分隔符占位符
    public static final DecimalFormat floatFormatter = new DecimalFormat("###,###.#");
    public static final DecimalFormat highPrecisionFloatFormatter = new DecimalFormat("###,###.######");

    //Community of users:
    public static final String COMMUNITY_11_WITH_AT_LEAST_ONE_SO_A_JUST_All_ISSUE_ASSIGNEES = "11"; //"SO activity: at least one answer. Only project members and committers and issue assignees and issue reporters.";
    public static final String COMMUNITY_12_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS = "12"; //"SO activity: at least one answer. Only project members.";
    public static final String COMMUNITY_13_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS = "13"; //"SO activity: at least one answer. Only project members and committers.";
    public static final String COMMUNITY_14_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES = "14"; //"SO activity: at least one answer. Only project members and committers and issue assignees.";
    public static final String COMMUNITY_15_WITH_AT_LEAST_ONE_SO_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS = "15"; //"SO activity: at least one answer. Only project members and committers and issue assignees and issue reporters.";

    public static final String COMMUNITY_21_WITH_AT_LEAST_ONE_SO_Q_OR_A_JUST_All_ISSUE_ASSIGNEES = "21"; //"SO activity: at least one answer. Only project members and committers and issue assignees and issue reporters.";
    public static final String COMMUNITY_22_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS = "22"; //"SO activity: at least one answer. Only project members.";
    public static final String COMMUNITY_23_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS = "23"; //"SO activity: at least one answer. Only project members and committers.";
    public static final String COMMUNITY_24_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES = "24"; //"SO activity: at least one answer. Only project members and committers and issue assignees.";
    public static final String COMMUNITY_25_WITH_AT_LEAST_ONE_SO_Q_OR_A_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS = "25"; //"SO activity: at least one answer. Only project members and committers and issue assignees and issue reporters.";

    public static final String COMMUNITY_31_NO_CHECKING_FOR_SO_ACTIVITY_JUST_All_ISSUE_ASSIGNEES = "31"; //"SO activity is not checked. Only project members and committers and issue assignees and issue reporters.";
    public static final String COMMUNITY_32_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS = "32"; //"SO activity is not checked. Only project members.";
    public static final String COMMUNITY_33_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS = "33"; //"SO activity is not checked. Only project members and committers.";
    public static final String COMMUNITY_34_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES = "34"; //"SO activity is not checked. Only project members and committers and issue assignees.";
    public static final String COMMUNITY_35_NO_CHECKING_FOR_SO_ACTIVITY_JUST_PROJECT_MEMBERS_AND_COMMITTERS_AND_ISSUE_ASSIGNEES_AND_ISSUE_REPORTERS = "35"; //"SO activity is not checked. Only project members and committers and issue assignees and issue reporters.";

    public static final String ALL = "ALL";

    public static final long THIS_IS_A_TEST = 10000;
    public static final long THIS_IS_REAL = -1;//unlimited!
    public static final double UNIMPORTANCE_OF_RECENT_ASSIGNMENTS = 0.0; //The less amounts, the more important; if it equals 2, the previous assignments has 1/(1+2) score, the one before that has 1/(1+3), and so on.
    //public static final double[] Z_SCORE_COEFFICIENTS = {0, 0, 0, 0, 0.0001, 0.0001, 0.001, 0.002, 0.003, 0.005, 0.007, 0.01, 0.05, 0.1, 0.5, 1, 5, 10};
//   public static final double[] Z_SCORE_COEFFICIENTS = {0.004, 0.005, 0.006, 0.007, 0.008, 0.009, 0.01, 0.015, 0.02, 0.025, 0.03, 0.035, 0.04, 0.045, 0.05, 0.055};//16
    public static final double[] Z_SCORE_COEFFICIENTS = {0, 0, 0, 0, 0.004, 0.005, 0.006, 0.007, 0.008, 0.009, 0.01, 0.015, 0.02, 0.03, 0.04, 0.045, 0.05, 0.055};
    public static final int TOTAL_NUMBER_OF_METRICS = 18;
//    public static final int TOTAL_NUMBER_OF_METRICS = 16;
    public static final double A_FAR_LARGESCORE = 1000000.0;

    //Sort order:
    public enum SortOrder {
        ASCENDING_INTEGER, DESCENDING_INTEGER, DEFAULT_FOR_STRING
    }

    public enum ConditionType {
        NO_CONDITION, AND, OR
    }

    public static void changeStr(String s) {
        s = s + "sss";
    }

    public static void main(String[] args) {
        System.out.println("---------------- Constants.java: ----------------");
        String s1 = "2013-05-25T21:50:02Z";
        String s2 = "2011-07-25T21:50:02Z";
        if (s1.compareTo(s2) > 0) //if s1 > s2 ==> returns positive value.
            System.out.println("1111");
        else
            System.out.println("2222");

        Date d1 = new Date();
        String s = "";
        for (int i = 0; i < 7000; i++)
            s = s + "a";
        Date d2 = new Date();
        System.out.println((d2.getTime() - d1.getTime()) / 1000);

        double a = 0.0000004;
        System.out.println(highPrecisionFloatFormatter.format(0.0000004));
        System.out.println(Constants.highPrecisionFloatFormatter.format(a));
    }

}


