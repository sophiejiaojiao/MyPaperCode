package bugAssignment;

import data.ProvideData;
import data.SOPost;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CountTagNum {

    static HashMap<String, Integer> map = new HashMap();

    public static void main(String[] args) {
        countTagNumber(Constants.DATASET_DIRECTORY_DATASETS_FOR_RECOMMEND,
                "Posts-madeByCommunityMembers-top20Projects.tsv",
                Constants.DATASET_DIRECTORY_RECOMMEND_RESULTS,
                "countTagNumber", 100000, Constants.THIS_IS_REAL);
    }

    public static void countTagNumber(String postsOfCommunityMembersInputPath, String postsOfCommunityMembersInputTSV,
                                      String countResultOutputPath, String countResultOutputFileName,
                                      int showProgressInterval, long testOrReal) {
        TreeMap<String, String[]> posts1ById = TSVManipulations
                .readUniqueKeyAndItsValueFromTSV(
                        postsOfCommunityMembersInputPath,
                        postsOfCommunityMembersInputTSV, null, 0, 9,
                        "0$1$2$3$4$5", Constants.ConditionType.NO_CONDITION, 0, "",
                        0, "", showProgressInterval * 10, testOrReal, 3);

        try {
            FileWriter writer = new FileWriter(countResultOutputPath + "\\" + countResultOutputFileName);
            FileWriter writer2 = new FileWriter(countResultOutputPath + "\\" + "num");

            writer.append("tags\n");

            StringBuilder builder = new StringBuilder();
            StringBuilder builder2 = new StringBuilder();

            String[] tags = null;
            for (Map.Entry<String, String[]> entry : posts1ById.entrySet()) {
                SOPost postInfo = ProvideData.copySOPostFromStringArray_OnlyThereAreSomeFields1(entry.getValue());
                tags = splitTags(postInfo.tags);
                for (int j = 0; j < tags.length; j++) {
                    if (tags[j] != "") {
                        if (map.containsKey(tags[j])) {
                            map.put(tags[j], map.get(tags[j]) + 1);
                        } else {
                            map.put(tags[j], 1);
                        }
                        builder.append(tags[j] + "\t");
                    }

                }

                    builder.append("\n");

            }

            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                builder2.append(key + ":" + "\t" + value + "\n");

            }

            writer.write(builder.toString());
            writer.flush();
            writer.close();

            writer2.write(builder2.toString());
            writer2.flush();
            writer2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] splitTags(String tags) {
        tags = tags.replaceFirst("\\[", "");
        tags = tags.replaceFirst("\\]", "");
        tags = tags.toLowerCase();
        String[] result = tags.split(Constants.SEPARATOR_FOR_ARRAY_ITEMS);// 按照;;进行分割
        return result;
    }
}
