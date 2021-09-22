package com.example.http;

import com.example.albaayo.chat.ResponseChatMessage;
import com.example.albaayo.location.LocationDto;
import com.example.albaayo.location.LocationSaveDto;
import com.example.albaayo.personalchat.ResponsePersonalChatMessage;
import com.example.http.dto.Id;
import com.example.http.dto.RequestCommuteDto;
import com.example.http.dto.RequestCompanyDto;
import com.example.http.dto.RequestInviteWorkerDto;
import com.example.http.dto.RequestLoginDto;
import com.example.http.dto.RequestNoticeDto;
import com.example.http.dto.RequestNoticeUpdateDto;
import com.example.http.dto.RequestScheduleDto;
import com.example.http.dto.RequestSignupDto;
import com.example.http.dto.CompanyDto;
import com.example.http.dto.ResponseCommuteListDto;
import com.example.http.dto.ResponseCompanyWorkerListDto;
import com.example.http.dto.ResponseFindWorkerDto;
import com.example.http.dto.ResponseLoginDto;
import com.example.http.dto.ResponseNoticeDto;
import com.example.http.dto.ResponseNoticeListDto;
import com.example.http.dto.ResponsePayInformationDto;
import com.example.http.dto.ResponseScheduleDto;
import com.example.http.dto.ResponseSignupDto;
import com.example.http.dto.Result;
import com.example.http.dto.ValidateDuplicateCheckMessage;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("/duplicate/{userId}")
    Call<ValidateDuplicateCheckMessage> duplicateCheckApi(@Path("userId") String userId);

    @POST("/employer/signup")
    Call<ResponseSignupDto> employerSignup(@Body RequestSignupDto requestSignupDto);

    @POST("/worker/signup")
    Call<ResponseSignupDto> workerSignup(@Body RequestSignupDto requestSignupDto);

    @POST("/login")
    Call<ResponseLoginDto> loginApi(@Body RequestLoginDto requestLoginDto);

    @GET("/logout/{memberId}")
    Call<Void> logout(@Path("memberId") Long memberId);

    @GET("/employer/{employerId}/company")
    Call<List<CompanyDto>> companies(@Header("Authorization") String accessToken, @Path("employerId") Long employerId);

    @GET("/worker/{workerId}/company")
    Call<Result<List<CompanyDto>>> acceptCompanyList(@Header("Authorization") String accessToken, @Path("workerId") Long workerId);

    @GET("/worker/{workerId}/company/invite")
    Call<List<CompanyDto>> notAcceptCompanyList(@Header("Authorization") String accessToken, @Path("workerId") Long workerId);

    @POST("/worker/{workerId}/{companyId}/invite")
    Call<Void> acceptCompany(@Header("Authorization") String accessToken, @Path("workerId") Long workerId, @Path("companyId") Long companyId);

    @DELETE("/worker/{workerId}/{companyId}/invite")
    Call<Void> notAcceptCompany(@Header("Authorization") String accessToken, @Path("workerId") Long workerId, @Path("companyId") Long companyId);

    @GET("{memberId}/company/{companyId}")
    Call<Result<List<ResponseCompanyWorkerListDto>>> companyMain(@Header("Authorization") String accessToken,
                                                         @Path("memberId") Long memberId, @Path("companyId") Long companyId);

    @Multipart
    @POST("/employer/{employerId}/company")
    Call<CompanyDto> createCompany(@Header("Authorization") String accessToken, @Path("employerId") Long employerId, @Part List<MultipartBody.Part> request);

//    @Multipart
//    @POST("/employer/{employerId}/company")
//    Call<ResponseBody> createCompany(@Header("Authorization") String accessToken, @Path("employerId") Long employerId, @Part List<MultipartBody.Part> request);

    @GET("/company/worker/{workerId}")
    Call<ResponseFindWorkerDto> workerFind(@Header("Authorization") String accessToken, @Path("workerId") String workerId);

    @POST("/company/invite/{companyId}")
    Call<ResponseFindWorkerDto> workerInvite(@Header("Authorization") String accessToken, @Path("companyId") Long companyId, @Body RequestInviteWorkerDto requestInviteWorkerDto);

    @POST("/commute/go-to-work")
    Call<Void> goToWork(@Header("Authorization") String accessToken, @Body RequestCommuteDto requestCommuteDto);

    @POST("/commute/off-work")
    Call<Void> offWork(@Header("Authorization") String accessToken, @Body RequestCommuteDto requestCommuteDto);

    @GET("/notice/{companyId}/{page}")
    Call<List<ResponseNoticeListDto>> noticeList(@Header("Authorization") String accessToken, @Path("companyId") Long companyId, @Path("page") int page);

    @POST("/notice/{memberId}/{companyId}")
    Call<Void> noticeRegister(@Header("Authorization") String accessToken, @Path("memberId") Long memberId, @Path("companyId") Long companyId, @Body RequestNoticeDto requestNoticeDto);

    @GET("/notice/{noticeId}")
    Call<ResponseNoticeDto> noticeContent(@Header("Authorization") String accessToken, @Path("noticeId") Long noticeId);

    @PATCH("/notice")
    Call<Void> noticeUpdate(@Header("Authorization") String accessToken, @Body RequestNoticeUpdateDto requestNoticeUpdateDto);

    @DELETE("/notice/{noticeId}")
    Call<Void> removeNotice(@Header("Authorization") String accessToken, @Path("noticeId") Long noticeId);

    @GET("/commute/{workerId}/{companyId}")
    Call<List<ResponseCommuteListDto>> commuteList(@Header("Authorization") String accessToken, @Path("workerId") Long workerId, @Path("companyId") Long companyId);

    @GET("/schedule/{companyId}/{date}")
    Call<ResponseScheduleDto> schedule(@Header("Authorization") String accessToken, @Path("companyId") Long companyId, @Path("date") String date);

    @POST("/schedule")
    Call<Void> registerSchedule(@Header("Authorization") String accessToken, @Body RequestScheduleDto requestScheduleDto);

    @GET("/chat/{companyId}")
    Call<List<ResponseChatMessage>> companyChatContents(@Header("Authorization") String accessToken, @Path("companyId") Long companyId);

    @GET("/chat/{myMemberId}/{memberId}")
    Call<List<ResponsePersonalChatMessage>> personalChatContents(@Header("Authorization") String accessToken, @Path("myMemberId") Long myMemberId
            , @Path("memberId") Long memberId);

    @DELETE("/company/{companyId}")
    Call<Void> removeCompany(@Header("Authorization") String accessToken, @Path("companyId") Long companyId);

    @DELETE("/company/{workerId}/{companyId}")
    Call<Void> companyExit(@Header("Authorization") String accessToken, @Path("workerId") Long workerId, @Path("companyId") Long companyId);

    @GET("/location/{workerId}/{companyId}")
    Call<LocationDto> location(@Header("Authorization") String accessToken, @Path("workerId") Long workerId, @Path("companyId") Long companyId);

    @POST("/location")
    Call<Void> saveLocation(@Header("Authorization") String accessToken, @Body LocationSaveDto locationSaveDto);

    @DELETE("/location/{workerId}")
    Call<Void> deleteLocation(@Header("Authorization") String accessToken, @Path("workerId") Long workerId);

    @GET("/pay/{workerId}/{companyId}")
    Call<ResponsePayInformationDto> monthPayInfo(@Header("Authorization") String accessToken,
                                                 @Path("workerId") Long workerId, @Path("companyId") Long companyId,
                                                 @Query("date") String date);
}
