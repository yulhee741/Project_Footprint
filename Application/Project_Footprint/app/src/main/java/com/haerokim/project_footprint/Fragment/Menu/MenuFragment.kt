package com.haerokim.project_footprint.Fragment.Menu

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.haerokim.project_footprint.DataClass.User
import com.haerokim.project_footprint.R
import io.paperdb.Paper
import kotlinx.android.synthetic.main.fragment_menu.*

/**  사용자 설정 및 앱 사용 관련 메뉴 제공 UI  **/

class MenuFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_menu, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // User 정보 로드 필요 (닉네임 등)
        val user: User = Paper.book().read("user_profile")
        text_menu_user_nickname.text = user.nickname

        image_user_profile.setBackground(ShapeDrawable(OvalShape()))
        image_user_profile.setClipToOutline(true)

        // 사용자 프로필 이미지 로드
        val pref: SharedPreferences? = context?.getSharedPreferences("profile_image", Activity.MODE_PRIVATE)
        val profileImageUri = Uri.parse(pref?.getString("profile_image", ""))

        if(profileImageUri.toString() != ""){
            image_user_profile.setImageURI(profileImageUri)
        }else{
            image_user_profile.setImageResource(R.drawable.basic_profile)
        }

        image_user_profile.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_menu_to_navigation_edit_profile)
        }

        button_change_profile.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_menu_to_navigation_edit_profile)
        }

        card_notice.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_menu_to_navigation_notice)
        }
    }
}