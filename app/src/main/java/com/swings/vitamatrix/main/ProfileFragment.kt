package com.swings.vitamatrix.main

import androidx.navigation.fragment.findNavController
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.app.imagepickerlibrary.ImagePicker
import com.app.imagepickerlibrary.ImagePicker.Companion.registerImagePicker
import com.app.imagepickerlibrary.listener.ImagePickerResultListener
import com.app.imagepickerlibrary.model.PickerType
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.swings.vitamatrix.R
import com.swings.vitamatrix.databinding.FragmentProfileBinding
import com.swings.vitamatrix.db.AppDatabase
import com.swings.vitamatrix.db.DatabaseProvider
import com.swings.vitamatrix.device.ConnectionFragment
import com.swings.vitamatrix.models.QAData
import com.swings.vitamatrix.questions.UpdateAnswersActivity
import com.swings.vitamatrix.utils.Constants
import com.swings.vitamatrix.utils.ToastType
import com.swings.vitamatrix.utils.UserData
import com.swings.vitamatrix.utils.Util
import com.swings.vitamatrix.utils.wheelpicker.WheelPicker
import com.swings.vitamatrix.utils.wheelpicker.WheelPicker.OnItemSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class ProfileFragment : Fragment(), ImagePickerResultListener {

    private var questionAnswers: String? = null
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val imagePicker: ImagePicker by lazy {
        registerImagePicker(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadQA()
//        initLoopView(binding.loopview)
        initWheelPicker(binding.wheelPicker)


        binding.personalData.setOnClickListener {
            var intent = Intent(requireContext(), UpdateAnswersActivity::class.java)
            intent.putExtra("qa", questionAnswers)
            startActivity(intent)
        }

        binding.resetData.setOnClickListener {
//            startActivity(Intent(requireContext(), WizardActivity::class.java))
            showConfirmationDialog()
        }

        binding.avatar.setOnClickListener {
            imagePicker.open(PickerType.GALLERY)
        }



    }



    private fun initWheelPicker(wheelPicker: WheelPicker) {
        wheelPicker.setOnItemSelectedListener(object : OnItemSelectedListener {

            override fun onItemSelected(
                picker: WheelPicker?,
                data: Any?,
                position: Int
            ) {
                updateGoalInJson(data.toString())
                val qaDao = getDB().qaDao()
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        qaDao.deleteHealthJson()
                    }
                }
            }
        })
        wheelPicker.data = goalList
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Reset Data?")
        builder.setMessage("Are you sure you want to reset all data?")
        builder.setPositiveButton("Yes") { _, _ ->
            val qaDao = getDB().qaDao()
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    qaDao.deleteQAData()
                }
            }
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }


    private fun getDB(): AppDatabase {
        return DatabaseProvider.getDatabase(requireContext())
    }


    private fun loadQA() {
        val qaDao = getDB().qaDao()
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                qaDao.getLiveQAData()
                    .observe(viewLifecycleOwner) { qa ->
                        questionAnswers = qa
                        Util.log(questionAnswers)
                        try {
                            val userData = Gson().fromJson(questionAnswers, UserData::class.java)
                            binding.name.text = userData.name
                            binding.age.text = userData.age.toString()
                            binding.weight.text = userData.weight.toString()
                            binding.height.text = userData.height.toString()
                            Util.log("goalList set " + goalList.indexOf(userData.goal))
                            binding.loopview.setInitPosition(goalList.indexOf(userData.goal))
                        } catch (e: Exception) {

                        }
                    }
            }
        }
    }

    fun updateGoalInJson(newGoal: String) {
        val jsonObject = Gson().fromJson(questionAnswers, JsonObject::class.java)
        if (jsonObject == null)
            return
        jsonObject.addProperty(Constants.GOAL, newGoal)
        questionAnswers = Gson().toJson(jsonObject)

        val qaDataDao = getDB().qaDao()

        CoroutineScope(Dispatchers.IO).launch {
            qaDataDao.insertQAData(QAData(1, jsonData = questionAnswers.toString()))
        }
    }

    override fun onImagePick(uri: Uri?) {
        uri ?: return

        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val outputFile = getAvatarFile()
            val outputStream = FileOutputStream(outputFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            Util.toast(ToastType.SUCCESS, "Image saved Successfully")
            loadAvatarFile()

        } catch (e: Exception) {
            Util.toast(ToastType.ERROR, "Error While Picking Image!")
        }
    }

    private fun loadAvatarFile() {
        val avatarFile = getAvatarFile()
        val bitmap = BitmapFactory.decodeFile(avatarFile.absolutePath)
        if (bitmap != null)
            binding.avatar.setImageBitmap(bitmap)
    }

    private fun getAvatarFile(): File {
        val storageDir = File(requireContext().filesDir, "avatar")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File(storageDir, "avatar.jpg")
    }

    override fun onMultiImagePick(uris: List<Uri>?) {

    }

    companion object {
        val goalList = arrayListOf(
            "Energizer",
            "Fat Burner",
            "Muscle Builder",
            "Joint Saver",
            "Deep Sleeper",
            "Heart Guard",
            "Immune Shield",
            "Mind Booster",
            "Gut Fixer",
            "Sugar Balancer"
        )
    }
}
