package za.co.eduvos.codebridge.core.network;

import za.co.eduvos.codebridge.core.network.dto.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    @POST("/auth/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    @POST("/auth/register")
    Call<LoginResponse> registerStudent(@Body RegisterStudentRequest request);

    @POST("/auth/register-company")
    Call<LoginResponse> registerCompany(@Body RegisterCompanyRequest request);

    @GET("/sessions/{student_hash_id}")
    Call<List<za.co.eduvos.codebridge.core.network.dto.SessionHistoryDto>> getStudentSessions(@Path("student_hash_id") String hashId);

    @POST("/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("/problems")
    Call<List<ProblemDto>> getProblems();

    @GET("/companies")
    Call<List<CompanyDto>> getCompanies();

    @GET("/problems/{problem_id}")
    Call<ProblemDto> getProblem(@Path("problem_id") int problemId);

    @POST("/sessions")
    Call<okhttp3.ResponseBody> createSession(@Body CreateSessionRequest session);

    @POST("/sync")
    Call<Map<String, Object>> syncData(@Body SyncPayload payload);

    @GET("/leaderboard")
    Call<List<LeaderboardDto>> getLeaderboard(); // Raw List

    @GET("/events")
    Call<List<EventDto>> getEvents(); // Raw List

    @POST("/speech/transcribe")
    Call<ApiResponse<SpeechResultDto>> transcribeSpeech();

    @POST("/ai-assist")
    Call<AiAssistResponse> askAi(@Body AiAssistRequest request);

    @GET("/health")
    Call<ApiResponse<String>> healthCheck();

    @POST("/rewards/complete-challenge")
    Call<okhttp3.ResponseBody> completeChallenge(@Body ChallengeCompleteRequest request);

    @POST("/ai-feedback")
    Call<FeedbackResponse> getAiFeedback(@Body FeedbackRequest request);

    @POST("/problems")
    Call<ProblemDto> createProblem(@Body ProblemCreate request);

    @POST("/events")
    Call<EventDto> createEvent(@Body EventCreate request);
}