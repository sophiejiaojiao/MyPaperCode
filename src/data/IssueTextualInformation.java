package data;

import bugAssignment.Constants;

public class IssueTextualInformation {
    public String projectLanguage;
    public String projectDescription;
    public String[] issueLabels;
    public String issueTitle;
    public String issueBody;
    /*
     * toLowerCase()将字符串转换成小写，构造函数IssueTextualInformation()将缺陷报告文本信息转换为小写
     */
    public IssueTextualInformation(String projectLanguage, String projectDescription, String issueLabels_separatedByDollar, String issueTitle, String issueBody){
        this.projectLanguage = projectLanguage.toLowerCase();
        this.projectDescription = projectDescription.toLowerCase();
        if (issueLabels_separatedByDollar.equals("[]"))
            this.issueLabels = null;
        else
            this.issueLabels = issueLabels_separatedByDollar.toLowerCase().split(Constants.SEPARATOR_FOR_ARRAY_ITEMS);//按照;;进行划分
        this.issueTitle = issueTitle.toLowerCase();
        this.issueBody = issueBody.toLowerCase();
    }//IssueTextualInformation().
}
