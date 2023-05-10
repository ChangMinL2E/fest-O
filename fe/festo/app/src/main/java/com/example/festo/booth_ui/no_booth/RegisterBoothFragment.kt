package com.example.festo.booth_ui.no_booth


import RetrofitClient
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.database.Cursor
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.festo.booth_ui.BoothMainActivity
import com.example.festo.customer_ui.home.HomeActivity
import com.example.festo.data.API.BoothAPI
import com.example.festo.data.req.RegiBoothRequest
import com.example.festo.data.req.RegisterBoothReq
import com.example.festo.databinding.FragmentRegisterboothBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RegisterBoothFragment : Fragment() {
    private var mBinding: FragmentRegisterboothBinding? = null
    private var retrofit = RetrofitClient.client
    private lateinit var imagePart: MultipartBody.Part
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var binding = FragmentRegisterboothBinding.inflate(inflater, container, false)
        mBinding = binding

        binding.ivBoothImage.setOnClickListener {
            startActivityForResult(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI
                ), 0
            )
        }

        binding.tvStartTime.setOnClickListener {
            var calendar = Calendar.getInstance()
            var hour = calendar.get(Calendar.HOUR)
            var minute = calendar.get(Calendar.MINUTE)

            var listener = TimePickerDialog.OnTimeSetListener { timePicker, i, i2 ->
                binding.tvStartTime.text = String.format("%02d:%02d", i, i2)
            }

            var picker = TimePickerDialog(
                requireContext(),
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                listener,
                hour,
                minute,
                true
            ) // true하면 24시간 제
            picker.show()
        }
        binding.tvEndTime.setOnClickListener {
            var calendar = Calendar.getInstance()
            var hour = calendar.get(Calendar.HOUR)
            var minute = calendar.get(Calendar.MINUTE)

            var listener = TimePickerDialog.OnTimeSetListener { timePicker, i, i2 ->
                binding.tvEndTime.text = String.format("%02d:%02d", i, i2)
            }

            var picker = TimePickerDialog(
                requireContext(),
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                listener,
                hour,
                minute,
                true
            ) // true하면 24시간 제
            picker.show()
        }

        // 부스 등록 버튼 누르면
        binding.registerBooth.setOnClickListener {

            if (binding.etBoothName.text.toString().isEmpty()) {
                Toast.makeText(requireActivity(), "부스 이름을 입력해 주세요", Toast.LENGTH_SHORT).show()
            } else if (binding.tvStartTime.text.toString() == "00 : 00 " && binding.tvEndTime.text.toString() == " 00 : 00") {
                Toast.makeText(requireActivity(), "운영시간을 입력해 주세요", Toast.LENGTH_SHORT).show()
            } else if (binding.etBoothLocation.text.toString().isEmpty()) {
                Toast.makeText(requireActivity(), "부스 위치를 입력해 주세요", Toast.LENGTH_SHORT).show()
            } else if (binding.etBoothDescription.text.toString().isEmpty()) {
                Toast.makeText(requireActivity(), "부스 설명을 입력해 주세요", Toast.LENGTH_SHORT).show()
            } else {

                val request = RegiBoothRequest(
                    binding.etBoothName.text.toString(),
                    binding.etBoothLocation.text.toString(),
                    binding.etBoothDescription.text.toString(),
                    binding.tvStartTime.text.toString(),
                    binding.tvEndTime.text.toString()

                )
                val data = RegisterBoothReq(request, imagePart)
                Log.d("제발", "${data}")
                fun postRegisterBooth() {
                    val postApi = retrofit?.create(BoothAPI::class.java)
                    postApi!!.registerBooth(
                        "1", request, imagePart
                    )
                        .enqueue(object : Callback<Long> {
                            override fun onResponse(
                                call: Call<Long>,
                                response: Response<Long>
                            ) {
                                Log.d(
                                    "부스테스트트",
                                    "${response.isSuccessful()}, ${response.code()}, ${response}"
                                )
                            }

                            override fun onFailure(call: Call<Long>, t: Throwable) {
                                t.printStackTrace()
                                Log.d("테스트트트트트", "시래패패패패패패패패패패패퍂패패패")
                            }
                        })
                }
                postRegisterBooth()
                //메인페이지 이동
                val intent = Intent(requireContext(), BoothMainActivity::class.java)
                startActivity(intent)
            }
        }

        // 일반 사용자 마이페이지로 이동
        binding.goCustomer.setOnClickListener {
            val intent = Intent(requireContext(), HomeActivity::class.java)
            intent.putExtra("fragment", "MypageFragment")
            startActivity(intent)
        }
        return mBinding?.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

    private fun getImageBody(uri: Uri): MultipartBody.Part {
        val filePath = getRealPathFromURI(uri)
        val file = File(filePath)
        val requestFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
        return MultipartBody.Part.createFormData("festivalImg", file.name, requestFile)
    }

    fun getRealPathFromURI(path: Uri): String {

        var proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        var c: Cursor? = requireActivity().contentResolver.query(path, proj, null, null, null)
        var index = c?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c?.moveToFirst()
        var result = c?.getString(index!!)

        return result!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            imagePart = getImageBody(data.data!!)
            mBinding?.ivBoothImage?.setImageURI(data?.data)
        }
    }
}