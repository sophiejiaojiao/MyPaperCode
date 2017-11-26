package data;

import java.util.ArrayList;
import java.util.Collections;

/*
 * Comparable位于java.lang下，该接口强行对实现它的每个类的对象进行自然排序（java.lang包默认导入，不用再手动导入）
 * Issue类的成员变量同issue2-forTop20Projects.tsv中的属性
 * 问题：1.为什么要比较两个缺陷报告的createdAt值？
 */
public class Issue implements Comparable<Issue> {
    public String id;
    public String ownerLoginAndProjectName;
    public String reporterId;
    public String reporterLogin;
    public String assigneeId;
    public String assigneeLogin;
    public String createdAt;
    public String numberOfComments;
    public String labels;
    public String title;
    public String body;

    /*
     * 重写compareTo方法
     * createdAt均不为空，则比较两个createdAt的大小，如果this.createdAt>iss.createdAt，则返回1
     * createdAt均为null时返回0
     * this.createdAt != null，iss.createdAt==null，则返回1
     * this.createdAt == null，iss.createdAt！=null，则返回-1
     */
    @Override
    public int compareTo(Issue iss) {
        if (this.createdAt != null && iss.createdAt != null)
            return this.createdAt.compareTo(iss.createdAt);
        else if (this.createdAt == null && iss.createdAt == null)
            return 0;
        else if (this.createdAt != null)
            return 1;
        else
            return -1;
    }

    /*
     * 猜想：main方法是用来测试compareTo方法是否正确
     */
    public static void main(String args[]) {
        ArrayList<Issue> issAL = new ArrayList<Issue>();

        Issue i1 = new Issue();
        i1.createdAt = "b";
        issAL.add(i1);

        Issue i2 = new Issue();
        i2.createdAt = "a";
        issAL.add(i2);

        Issue i3 = new Issue();
        i3.createdAt = "0";
        issAL.add(i3);

        Collections.sort(issAL);
        for (Issue i : issAL)
            System.out.println(i.createdAt);
        i1.createdAt = "a";

    }
}
