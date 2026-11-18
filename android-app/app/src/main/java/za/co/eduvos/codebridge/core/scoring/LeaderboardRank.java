package za.co.eduvos.codebridge.core.scoring;

import java.util.List;
import java.util.Locale;

import za.co.eduvos.codebridge.core.network.dto.LeaderboardDto;

/** Works out rank and level from the leaderboard list. */
public final class LeaderboardRank {

    /** XP needed per level. */
    public static final int XP_PER_LEVEL = 100;

    private LeaderboardRank() {}

    public static int levelFor(int xp) {
        return Math.max(0, xp) / XP_PER_LEVEL + 1;
    }

    /** True if this entry is the logged-in student (full hash or the short 8-letter form). */
    public static boolean isMe(String entryHashId, String myHashId, String myDisplayId) {
        if (entryHashId == null) return false;
        if (myHashId != null && entryHashId.equals(myHashId)) return true;
        return myDisplayId != null && !myDisplayId.equals("GUEST") && entryHashId.equalsIgnoreCase(myDisplayId);
    }

    /** Rank = 1 + students with more XP, so ties share a rank. Returns -1 if not in the list. */
    public static int rankOf(List<LeaderboardDto> entries, String myHashId, String myDisplayId) {
        if (entries == null) return -1;
        for (LeaderboardDto e : entries) {
            if (isMe(e.getHashId(), myHashId, myDisplayId)) {
                int ahead = 0;
                for (LeaderboardDto o : entries) if (o.getXp() > e.getXp()) ahead++;
                return ahead + 1;
            }
        }
        return -1;
    }

    /** Text like "#3 of 50", or "Unranked" if not in the list. */
    public static String label(List<LeaderboardDto> entries, String myHashId, String myDisplayId) {
        int rank = rankOf(entries, myHashId, myDisplayId);
        if (rank < 0) return "Unranked";
        return String.format(Locale.ROOT, "#%d of %d", rank, entries.size());
    }
}
