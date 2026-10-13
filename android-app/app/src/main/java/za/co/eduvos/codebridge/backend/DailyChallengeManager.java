package za.co.eduvos.codebridge.backend;

import za.co.eduvos.codebridge.data.ProblemDetail;
import za.co.eduvos.codebridge.data.ProblemRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Java Backend Daily Challenge Selection Manager (Requirement 1).
 * Rotates the daily challenge based on the calendar date (consistent for all users on the same day).
 * Tracks completion using the user's unique Hash ID.
 */
public class DailyChallengeManager {

    public static ProblemDetail getDailyChallenge() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
        String dateStr = sdf.format(new Date());

        int dateHash = Math.abs(dateStr.hashCode());
        int total = ProblemRepository.INSTANCE.getProblems().size();

        int dailyIndex = dateHash % total;
        return ProblemRepository.INSTANCE.getProblems().get(dailyIndex);
    }

    public static boolean isDailyChallengeCompleted(String userHashId) {
        ProblemDetail daily = getDailyChallenge();
        return ProgressRepository.isProblemCompleted(userHashId, daily.getId());
    }
}
