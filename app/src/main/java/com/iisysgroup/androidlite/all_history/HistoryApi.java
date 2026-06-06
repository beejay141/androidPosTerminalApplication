package com.iisysgroup.androidlite.all_history;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface HistoryApi {
    @POST("/api/account/transaction-history")

//    Call fetchHistoty(@Body HistoryParam paramModel);
    Call<TransactionHistory> fetchHistoty(@Body HistoryParam paramModel);
    //Call<TransactionHistory> fetchHistoty( @Path("wallet") String walletId);
//    Call<TransactionSummary> fetchHistoty(@Body HistoryParam paramModel);

//            @Field("startDate")String startDate,
//            @Field("endDate")String endDate,
//            @Field("limit")String limit,
//            @Field ("currentPage")String currrentPage




}
