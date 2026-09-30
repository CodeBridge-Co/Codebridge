package za.co.eduvos.codebridge.core.network;
import za.co.eduvos.codebridge.core.network.dto.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    @POST("/auth/register")
    Call<ApiResponse<LoginRequest>> register(@Body RegisterRequest request);

    @POST("/auth/login")
    Call<ApiResponse<LoginRequest>> login(@Body LoginRequest request);

    @GET("/problems")
    Call<ApiResponse<List<ProblemDto>>> getProblems();

    @GET("/problems/{problem_id}")
    Call<ApiResponse<ProblemDto>> getProblem(@Path("problem_id") int problemId);

    @POST("/sessions")
    Call<ApiResponse<SessionDto>> createSession(@Body SessionDto session);

    @POST("/sync")
    Call<ApiResponse<Map<String, Integer>>> syncData(@Body SyncPayload payload);

    @GET("/leaderboard")
    Call<ApiResponse<List<LeaderboardDto>>> getLeaderboard();

    @GET("/events")
    Call<ApiResponse<List<EventDto>>> getEvents();

    @POST("/speech/transcribe")
    Call<ApiResponse<SpeechResultDto>> transcribeSpeech();

    @POST("/ai-assist")
    Call<ApiResponse<Map<String, Object>>> aiAssist();

    @GET("/health")
    Call<ApiResponse<String>> healthCheck();
}
