package my.edu.tarc.epf.ui.profile

import android.app.Instrumentation.ActivityResult
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import my.edu.tarc.epf.R
import my.edu.tarc.epf.databinding.FragmentProfileBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream

class ProfileFragment : Fragment(), MenuProvider {
    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val getPhoto = registerForActivityResult(ActivityResultContracts.GetContent()){uri ->
        if(uri != null)
        {
            binding.imageViewProfile.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater,container,false)

        //Add support to Menu
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner,Lifecycle.State.RESUMED)

        return binding.root
    }

    lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageViewProfile.setOnClickListener{
            //call any application that can handle images
            getPhoto.launch("image/*")
        }
        //read profile picture
        val image = readProfilePicture()
        if(image == null){
            binding.imageViewProfile.setImageResource(R.drawable.baseline_account_box_24)
        }else{
            binding.imageViewProfile.setImageBitmap(image)
        }
        //Read profile info
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)

        //default value
        val name = sharedPref.getString(getString(R.string.name),getString(R.string.nav_header_title))
        val email = sharedPref.getString(getString(R.string.email),getString(R.string.nav_header_subtitle))

        binding.editTextName.setText(name)
        binding.editTextEmailAddress.setText(email)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.profile_menu, menu)
        menu.findItem(R.id.action_about).isVisible = false
        menu.findItem(R.id.action_settings).isVisible = false
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if(menuItem.itemId == R.id.action_save)
        {
            //TODO: Save profile info
            //pop up
            saveProfilePicture(binding.imageViewProfile)

            //save profile info
            val name = binding.editTextName.text.toString()
            val email = binding.editTextEmailAddress.text.toString()
            with(sharedPref.edit()){
                putString(getString(R.string.name),name)
                putString(getString(R.string.email),email)
                apply()
            }

            val navigationView = requireActivity().findViewById<View>(R.id.nav_view) as NavigationView

            val view = navigationView.getHeaderView(0)
            val profPic = view.findViewById<ImageView>(R.id.profilePicture)
            val textViewName = view.findViewById<TextView>(R.id.textViewName)
            val textViewEmail = view.findViewById<TextView>(R.id.textViewEmail)

            profPic.setImageBitmap(readProfilePicture())
            textViewName.text = name
            textViewEmail.text = email

            Toast.makeText(requireContext(), getString(R.string.profile_save),Toast.LENGTH_SHORT).show()
        } else if(menuItem.itemId == android.R.id.home)
        {
            //Handling the Up button
            //Return to previous menu
            findNavController().navigateUp()
        }
        return true
    }

    private fun saveProfilePicture(view: View) {
        val filename = "profile.png"
        val file = File(this.context?.filesDir, filename)
        val image = view as ImageView

        val bd = image.drawable as BitmapDrawable
        val bitmap = bd.bitmap
        val outputStream: OutputStream

        try{
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            outputStream.flush()
            outputStream.close()
        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }
    }

    private fun readProfilePicture(): Bitmap? {
        val filename = "profile.png"
        val file = File(this.context?.filesDir, filename)

        if(file.isFile){
            try{
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                return bitmap
            }catch (e: FileNotFoundException){
                e.printStackTrace()
            }
        }
        return null
    }

}