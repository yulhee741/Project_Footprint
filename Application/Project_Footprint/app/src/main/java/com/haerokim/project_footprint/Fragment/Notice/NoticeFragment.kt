package com.haerokim.project_footprint.Fragment.Notice

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.haerokim.project_footprint.Adapter.NoticeListAdapter
import com.haerokim.project_footprint.DataClass.Notice
import com.haerokim.project_footprint.Network.RetrofitService
import com.haerokim.project_footprint.Network.Website
import com.haerokim.project_footprint.R
import kotlinx.android.synthetic.main.fragment_notice.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 *  공지사항 조회 기능 제공
 *  - NoticeListAdapter 를 통해 RecyclerView 구현
 **/

class NoticeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lateinit var recyclerView: RecyclerView
        lateinit var viewAdapter: RecyclerView.Adapter<*>
        lateinit var viewManager: RecyclerView.LayoutManager

        var noticeList: ArrayList<Notice>

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm")
            .create()

        // API 호출을 위한 Retrofit 객체 생성
        var retrofit = Retrofit.Builder()
            .baseUrl(Website.BASE_URL) //사이트 Base URL을 갖고있는 Companion Obejct
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        var getNoticeList: RetrofitService = retrofit.create(RetrofitService::class.java)

        text_notice_no_data.visibility = View.GONE
        loading_notice.visibility = View.VISIBLE

        // 공지사항 리스트를 가져오기 위해 API 호출
        getNoticeList.requestNoticeList().enqueue(object : Callback<ArrayList<Notice>> {
            override fun onFailure(call: Call<ArrayList<Notice>>, t: Throwable) {
                Log.e("Notice Loading Error", t.message)
            }

            override fun onResponse(
                call: Call<ArrayList<Notice>>,
                response: Response<ArrayList<Notice>>
            ) {
                // 공지사항 없을 때 진입
                if (response.body()?.size == 0) {
                    notice_list.visibility = View.GONE
                    text_notice_no_data.visibility = View.VISIBLE
                    loading_notice.visibility = View.GONE
                    text_notice_no_data.text = "공지사항이 없습니다"
                } else {  // 공지사항 있을 때 진입
                    text_notice_no_data.visibility = View.GONE
                    loading_notice.visibility = View.GONE

                    noticeList = response.body()!!

                    viewManager = LinearLayoutManager(context)
                    viewAdapter = NoticeListAdapter(
                        noticeList,
                        requireContext()
                    )

                    recyclerView =
                        view.findViewById<RecyclerView>(R.id.notice_list).apply {
                            setHasFixedSize(true)
                            layoutManager = viewManager
                            adapter = viewAdapter
                        }
                }
            }
        })
    }
}