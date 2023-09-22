package br.senai.sp.jandira.upload_firebase

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import br.senai.sp.jandira.upload_firebase.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {
    //Declaração dos atributos:

    //activity main binding - manipulação dos elementos gráficos do material design
    private lateinit var binding : ActivityMainBinding

    //storage reference - permite a manipulação do cloud storage (armazena arquivos)
    private lateinit var storageRef : StorageReference

    //firebase firestore - permite a manipulação no banco de dados noSQL
    private lateinit var firebaseFireStore : FirebaseFirestore

    //uri - permite a manipulação de arquivos através do seu endereçamento
    private var imageUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //layout montado/inflado
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root) //procurar a contraparte de MainAcitivity lá na raiz

        initVars()
        registerClickEvents()
    }

    // função de inicialização dos recursos do firebase
    private fun initVars(){
        storageRef = FirebaseStorage.getInstance().reference.child("images")
        firebaseFireStore = FirebaseFirestore.getInstance()
    }

    //função para o lançador de recuperação de imagens da galeria
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()
    ){
        imageUri = it
        binding.imageView.setImageURI(it)
    }

    //função de tratamento de cliques

    private fun registerClickEvents(){
        //trata o evento de clique do componente imageView
        binding.imageView.setOnClickListener{
            resultLauncher.launch("image/*")
        }
        //trata o evento de clique do botão de upload
        binding.uploadBtn.setOnClickListener {
            uploadImage()
        }
    }

    //função de upload
    private fun uploadImage(){

        binding.progressBar.visibility = View.VISIBLE

        //define um nome unico para a imagem com o uso de um valor timestamp
        storageRef = storageRef.child(System.currentTimeMillis().toString())

        //executa o processo de upload da imagem
        imageUri?.let {
            storageRef.putFile(it).addOnCompleteListener{
               task-> //o que veio da tarefa
                    if (task.isSuccessful){
                        Toast.makeText(this, "Upload concluído!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Erro ao realizar o upload", Toast.LENGTH_LONG).show()
                    }

                binding.progressBar.visibility = View.GONE

            }
        }

    }

}