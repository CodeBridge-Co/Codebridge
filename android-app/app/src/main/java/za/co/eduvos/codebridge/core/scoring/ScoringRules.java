package za.co.eduvos.codebridge.core.scoring;

public final class ScoringRules {

    public static final int WEIGHT_CODE = 60;
    public static final int WEIGHT_GIT = 15;
    public static final int WEIGHT_SPEECH = 25;
    public static final int MAX_TIME_BONUS = 5;

    public static final double TARGET_TECH_DENSITY = 0.10;

    private ScoringRules() {}

    public static final class Input {
        public int testsPassed;
        public int testsTotal;
        public boolean timedOut;
        public long timeUsedMs;
        public long timeLimitMs;
        public boolean gitCompleted;
        /** Interview answers saved, questions asked and the sum of their technical-word densities. */
        public int speechAnswers;
        public int speechQuestions;
        public double speechDensitySum;
    }

    public static final class Result {
        public final int total;
        public final int code;
        public final int git;
        /** Null when the interview stage was not taken. */
        public final Integer speech;
        public final int timeBonus;

        Result(int total, int code, int git, Integer speech, int timeBonus) {
            this.total = total;
            this.code = code;
            this.git = git;
            this.speech = speech;
            this.timeBonus = timeBonus;
        }
    }

    public static Result compute(Input in) {
        int code = percent(in.testsPassed, in.testsTotal);
        int git = in.gitCompleted ? 100 : 0;
        Integer speech = speechScore(in.speechAnswers, in.speechQuestions, in.speechDensitySum);

        int bonus = 0;
        boolean allPassed = in.testsTotal > 0 && in.testsPassed == in.testsTotal;
        if (allPassed && !in.timedOut && in.timeLimitMs > 0) {
            double left = 1.0 - Math.min(1.0, Math.max(0, in.timeUsedMs) / (double) in.timeLimitMs);
            bonus = (int) Math.round(MAX_TIME_BONUS * left);
        }

        int weighted = Math.round((WEIGHT_CODE * code + WEIGHT_GIT * git
                + WEIGHT_SPEECH * (speech == null ? 0 : speech)) / 100f);
        int total = Math.min(100, weighted + bonus);
        return new Result(total, code, git, speech, bonus);
    }

    /** 0..100, or null when no answer was recorded. Answers count in proportion to the questions asked. */
    public static Integer speechScore(int answers, int questions, double densitySum) {
        if (answers <= 0 || questions <= 0) return null;
        double coverage = Math.min(1.0, answers / (double) questions);
        double avgDensity = densitySum / answers;
        double quality = Math.min(1.0, Math.max(0, avgDensity) / TARGET_TECH_DENSITY);
        return (int) Math.round(100 * coverage * quality);
    }

    private static int percent(int part, int whole) {
        if (whole <= 0) return 0;
        return Math.min(100, Math.max(0, Math.round(100f * part / whole)));
    }
}
