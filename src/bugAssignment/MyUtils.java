package bugAssignment;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bugAssignment.Constants.ConditionType;

public class MyUtils {
    public static String applyRegexOnString(String regex, String value) {
        Pattern pattern = Pattern.compile("[^" + regex + "]+");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find())
            value = value.replaceAll("[^" + regex + "]+", " ");
        if (value.equals(""))
            value = " ";
        return value;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------
    public static boolean runLogicalComparison(ConditionType conditionType, String value1A, String value1B, String value2A, String value2B) {
        boolean result = false;
        //runLogicalComparison方法本来传递的就是NO_CONDITION，为何还要写后面的else代码？
        if (conditionType == ConditionType.NO_CONDITION)
            result = true;
        else if (value1A.equals(value1B)) {
            if (conditionType == ConditionType.OR)
                result = true;
            else //conditionType == ConditionType.AND
                if (value2A.equals(value2B))
                    result = true;
        }//if (fiel....
        else if (conditionType == ConditionType.OR)
            if (value2A.equals(value2B))
                result = true;
        return result;
    }//runLogicalComparison().

    //------------------------------------------------------------------------------------------------------------------------------------------------
    public static boolean compareTwoStringArrays(String[] s1, String[] s2) {
        boolean result = true;
        if (s1.length != s2.length)
            result = false;
        else
            for (int i = 0; i < s1.length; i++)
                if (!s1[i].equals(s2[i])) {
                    result = false;
                    break;
                }
        return result;
    }//compareTwoStringArrays().


    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}

