package za.co.eduvos.codebridge.backend;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Java Backend Progress Engine (Requirement 5).
 * Calculates exact, verified user progress using actual completed problem IDs.
 * New users start at 0% progress. Failed or unverified attempts are excluded.
 */
public class ProgressRepository {

    // Store completed problem IDs per user Hash ID
    private static final Map<String, Set<Integer>> userCompletions = new HashMap<>();

    // Total applicable challenges per topic (denominator)
    private static final Map<String, Integer> topicTotals = new HashMap<>();

    static {
        topicTotals.put("Data Structures & Algorithms", 12);
        topicTotals.put("Algorithms", 10);
        topicTotals.put("Programming Concepts", 8);
        topicTotals.put("Database Systems", 8);
        topicTotals.put("Web Development", 10);
        topicTotals.put("Mobile Development", 8);
        topicTotals.put("Cloud Computing", 8);
        topicTotals.put("Git & GitHub", 6);
    }

    // Map problem ID to topic category
    private static String getTopicForProblem(int problemId) {
        if (problemId <= 10) return "Data Structures & Algorithms";
        if (problemId <= 20) return "Algorithms";
        if (problemId <= 30) return "Database Systems";
        if (problemId <= 40) return "Cloud Computing";
        return "Git & GitHub";
    }

    public static synchronized void recordVerifiedCompletion(String userHashId, int problemId) {
        if (userHashId == null || userHashId.isEmpty()) return;

        Set<Integer> completed = userCompletions.computeIfAbsent(userHashId, k -> new HashSet<>());
        completed.add(problemId); // Set guarantees no duplicate counting
    }

    public static synchronized boolean isProblemCompleted(String userHashId, int problemId) {
        if (userHashId == null || userHashId.isEmpty()) return false;
        Set<Integer> completed = userCompletions.get(userHashId);
        return completed != null && completed.contains(problemId);
    }

    public static synchronized int getCompletedCount(String userHashId) {
        if (userHashId == null || userHashId.isEmpty()) return 0;
        Set<Integer> completed = userCompletions.get(userHashId);
        return completed != null ? completed.size() : 0;
    }

    public static synchronized int getTotalApplicableChallenges() {
        int total = 0;
        for (int count : topicTotals.values()) {
            total += count;
        }
        return total;
    }

    public static synchronized int getOverallProgressPercent(String userHashId) {
        int completed = getCompletedCount(userHashId);
        int total = getTotalApplicableChallenges();
        if (total == 0) return 0;
        return (int) Math.round(((double) completed / total) * 100.0);
    }

    public static synchronized int getTopicProgressPercent(String userHashId, String topicName) {
        if (userHashId == null || topicName == null) return 0;

        Set<Integer> completed = userCompletions.get(userHashId);
        if (completed == null || completed.isEmpty()) return 0;

        int topicCompleted = 0;
        for (int problemId : completed) {
            if (topicName.equalsIgnoreCase(getTopicForProblem(problemId))) {
                topicCompleted++;
            }
        }

        int totalInTopic = topicTotals.getOrDefault(topicName, 10);
        if (totalInTopic == 0) return 0;

        return (int) Math.round(((double) topicCompleted / totalInTopic) * 100.0);
    }
}
